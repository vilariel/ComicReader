package com.arielvila.dilbert.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
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

import com.arielvila.dilbert.R;
import com.arielvila.dilbert.StripGridFragment;
import com.arielvila.dilbert.imgutil.ImageFetcherFile;
import com.arielvila.dilbert.imgutil.RecyclingImageView;

import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private static final String TAG = "GripAdapter";
    private StripGridFragment.StripGridCallbacks mCallback;
    private ArrayList<String> mFilePaths = new ArrayList<>();
    private SparseBooleanArray mSelectedItems;
    private int mChoiceMode;
    private View mGridView = null;
    LayoutInflater mInflater;
    ViewGroup mContainer;
    private int mCardWidth;
    private int mLayoutHeight;
    private int mImageHeight;
    private int mTextHeight;
    private int mTextSize;
    private ImageFetcherFile mImageFetcher;
    private long mOnBindCalled = 0;

    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    public static final int CHOICE_MODE_MULTIPLE = 2;

    public GridAdapter(StripGridFragment.StripGridCallbacks callback, ArrayList<String> filePaths, LayoutInflater inflater,
                       ViewGroup container, int width, int columns, float density, ImageFetcherFile imageFetcher) {
        super();
        super.setHasStableIds(true);
        mInflater = inflater;
        mContainer = container;
        mCallback = callback;
        mFilePaths = filePaths;
        mSelectedItems = new SparseBooleanArray();
        mChoiceMode = CHOICE_MODE_NONE;
        mImageFetcher = imageFetcher;
        mCardWidth = Math.round(new Float(width / columns * 0.95));
        mLayoutHeight = Math.round(new Float(width / columns * 1.0708));
        mImageHeight = Math.round(new Float(width / columns * 0.8075));
        mTextHeight = Math.round(new Float(width / columns * 0.2635));
        // X = [1 100 2; 1 170 2; 1 480 4; 1 478 4; 1 160 1.5; 1 160 1.5]
        // y = [7; 13; 18; 18; 15; 15]
        // theta = inv(X.' * X) * X.' * y = [15.613776; 0.067740; -7.502920]
        mTextSize = Math.round(new Float(15.613776 + width / columns * 0.067740 - density * 7.502920));
        Log.i(TAG, "width: " + width + ", columns: " + columns + ", density: " + density + ", mCardWidth: " + mCardWidth + ", mLayoutHeight: " + mLayoutHeight + ", mImageHeight: " + mImageHeight + ", mTextHeight: " + mTextHeight + ", mTextSize: " + mTextSize);
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
        RecyclingImageView imageView = (RecyclingImageView) mGridView.findViewById(R.id.img_thumbnail);
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
        mOnBindCalled++;
        viewHolder.imgText.setText(getName(mFilePaths.get(i)));
        // TODO resolve width and height (if necessary)

        viewHolder.imgThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // Load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
        mImageFetcher.loadImage(mFilePaths.get(i), viewHolder.imgThumbnail);
        viewHolder.viewItem.setOnClickListener(new OnImageClickListener(i));
        Log.i(TAG, "onBindViewHolder " + mFilePaths.get(i));
    }

    @Override
    public int getItemCount() {
        return mFilePaths.size();
    }

    @Override
    public long getItemId(int position) {
        return getNameAsId(mFilePaths.get(position));
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
        result = filePath.substring(len - 6, len - 4) + "." + mOnBindCalled;
        return result;
    }

    private Long getNameAsId(String filePath) {
        int len = filePath.length();
        String name = filePath.substring(len - 14, len - 10) + filePath.substring(len - 9, len - 7) +
                filePath.substring(len - 6, len - 4);
        return Long.valueOf(name);
    }

    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
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

    class OnImageClickListener implements OnClickListener {

        int _postion;

        // constructor
        public OnImageClickListener(int position) {
            this._postion = position;
        }

        @Override
        public void onClick(View v) {
            itemClick(_postion);
            mCallback.onItemSelected(String.valueOf(_postion));
//            Intent intent = new Intent(mActivity, FullScreenViewActivity.class);
//            intent.putExtra("position", _postion);
//            mActivity.startActivity(intent);
//            mActivity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);

        }
    }
}
