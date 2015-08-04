package com.arielvila.comicreader.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arielvila.comicreader.BuildConfig;
import com.arielvila.comicreader.R;
import com.arielvila.comicreader.StripGridFragment;
import com.arielvila.comicreader.helper.PicassoCache;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private static final String TAG = "GripAdapter";
    private StripGridFragment.StripGridCallbacks mCallback;
    private ArrayList<String> mFilePaths = new ArrayList<>();
    private SparseBooleanArray mSelectedItems;
    private int mChoiceMode;
    private View mGridView = null;
    private LayoutInflater mInflater;
    private ViewGroup mContainer;
    private Resources mResources;
    private Context mContext;
    private int mCardWidth;
    private int mLayoutHeight;
    private int mImageHeight;
    private int mTextHeight;
    private int mTextSize;
    private int mLoadingBitmapResId;
    private boolean mAllowsClick;

    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    public static final int CHOICE_MODE_MULTIPLE = 2;

    public GridAdapter(StripGridFragment.StripGridCallbacks callback, ArrayList<String> filePaths,
                       LayoutInflater inflater, ViewGroup container, int width, int columns, float density) {
        super();
        mInflater = inflater;
        mContainer = container;
        mCallback = callback;
        mFilePaths = filePaths;
        mContext = callback.getContext();
        mResources = mContext.getResources();
        mSelectedItems = new SparseBooleanArray();
        mChoiceMode = CHOICE_MODE_NONE;
        mAllowsClick = true;
        mCardWidth = Math.round(new Float(width / columns * 0.95));
        mLayoutHeight = Math.round(new Float(width / columns * 1.0708));
        mImageHeight = Math.round(new Float(width / columns * 0.8075));
        mTextHeight = Math.round(new Float(width / columns * 0.2635));
        // X = [1 100 2; 1 170 2; 1 480 4; 1 478 4; 1 160 1.5; 1 160 1.5]
        // y = [7; 13; 18; 18; 15; 15]
        // theta = inv(X.' * X) * X.' * y = [15.613776; 0.067740; -7.502920]
        mTextSize = Math.round(new Float(15.613776 + width / columns * 0.067740 - density * 7.502920));
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "width: " + width + ", columns: " + columns + ", density: " + density + ", mCardWidth: " + mCardWidth + ", mLayoutHeight: " + mLayoutHeight + ", mImageHeight: " + mImageHeight + ", mTextHeight: " + mTextHeight + ", mTextSize: " + mTextSize);
        }
    }

    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View mGridView = mInflater.inflate(R.layout.grid_item, mContainer, false);
        CardView cardView = (CardView) mGridView.findViewById(R.id.img_card);
        ViewGroup.LayoutParams cardParams = cardView.getLayoutParams();
        cardParams.width = mCardWidth;
        cardView.setLayoutParams(cardParams);
        RelativeLayout layoutView = (RelativeLayout) mGridView.findViewById(R.id.img_top_layout);
        ViewGroup.LayoutParams layoutParams = layoutView.getLayoutParams();
        layoutParams.height = mLayoutHeight;
        layoutView.setLayoutParams(layoutParams);
        ImageView imageView = (ImageView) mGridView.findViewById(R.id.img_thumbnail);
        ViewGroup.LayoutParams imageParams = imageView.getLayoutParams();
        imageParams.height = mImageHeight;
        imageView.setLayoutParams(imageParams);
        TextView textView = (TextView) mGridView.findViewById(R.id.img_text);
        ViewGroup.LayoutParams textParams = textView.getLayoutParams();
        textParams.height = mTextHeight;
        textView.setLayoutParams(textParams);
        textView.setTextSize(mTextSize);
        return new ViewHolder(mGridView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.imgText.setText(getName(mFilePaths.get(i)));
        if (isSelected(i)) {
            viewHolder.imgText.setBackgroundColor(mResources.getColor(R.color.grid_item_selected));
        } else {
            viewHolder.imgText.setBackgroundColor(mResources.getColor(R.color.grid_item_unselected));
        }
        // TODO resolve width and height (if necessary)
        loadBitmap(mFilePaths.get(i), viewHolder.imgThumbnail);
        viewHolder.imgThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        viewHolder.imgThumbnail.setImageBitmap(image);
        viewHolder.viewItem.setOnClickListener(new OnImageClickListener(i));
    }

    @Override
    public int getItemCount() {
        return mFilePaths.size();
    }

    public void changeFilePaths(ArrayList<String> filePaths) {
        this.mFilePaths = filePaths;
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View viewItem;
        public ImageView imgThumbnail;
        public TextView imgText;

        public ViewHolder(View itemView) {
            super(itemView);
            viewItem = itemView;
            imgThumbnail = (ImageView)itemView.findViewById(R.id.img_thumbnail);
            imgText = (TextView)itemView.findViewById(R.id.img_text);
        }
    }

    private String getName(String filePath) {
        String result;
        int len = filePath.length();
        result = filePath.substring(len - 14, len - 4);
        return result;
    }

    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    public void itemClick(String filePath) {
        int ind = mFilePaths.indexOf(filePath);
        if (ind >= 0) {
            itemClick(ind);
        }
    }

    public void itemClick(int position) {
        switch (mChoiceMode) {
            case CHOICE_MODE_MULTIPLE:
                toggleSelection(position);
                break;
            case CHOICE_MODE_SINGLE:
                clearSelection();
                mSelectedItems.put(position, true);
                notifyItemChanged(position);
                break;
            default:
                break;
        }
    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void setItemChecked(int position, boolean value) {
        mSelectedItems.put(position, value);
        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        mSelectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmapResId = resId;
    }

    public void setAllowsClick(boolean allowsClick) {
        this.mAllowsClick = allowsClick;
    }

    class OnImageClickListener implements OnClickListener {

        int mPostion;

        // constructor
        public OnImageClickListener(int position) {
            this.mPostion = position;
        }

        @Override
        public void onClick(View v) {
            if (mAllowsClick) {
                itemClick(mPostion);
                mCallback.selectItem(String.valueOf(mPostion));
            }
        }
    }

    /*
     * Resizing image size
     */
    public void loadBitmap(String filePath, ImageView imageView) {
        Picasso.with(mContext).load("file://" + filePath).resize(500, 200)
                .placeholder(mLoadingBitmapResId).centerCrop().into(imageView);
    }

}
