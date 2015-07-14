package com.arielvila.dilbert.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arielvila.dilbert.R;
import com.arielvila.dilbert.StripGridFragment;

import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private StripGridFragment.StripGridCallbacks mCallback;
    private ArrayList<String> mFilePaths = new ArrayList<>();
    private SparseBooleanArray mSelectedItems;
    private int mChoiceMode;

    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    public static final int CHOICE_MODE_MULTIPLE = 2;

    public GridAdapter(StripGridFragment.StripGridCallbacks callback, ArrayList<String> filePaths) {
        super();

        mCallback = callback;
        mFilePaths = filePaths;
        mSelectedItems = new SparseBooleanArray();
        mChoiceMode = CHOICE_MODE_NONE;
    }

    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.imgText.setText(getName(mFilePaths.get(i)));
        // TODO resolve width and height (if necessary)
        Bitmap image = decodeFile(mFilePaths.get(i), 80, 80);
        viewHolder.imgThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        viewHolder.imgThumbnail.setImageBitmap(image);
        viewHolder.viewItem.setOnClickListener(new OnImageClickListener(i));
    }

    @Override
    public int getItemCount() {
        return mFilePaths.size();
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

    /*
     * Resizing image size
     */
    public static Bitmap decodeFile(String filePath, int width, int height) {
        try {

            File f = new File(filePath);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            int scale = 1;
            while (o.outWidth / scale / 2 >= width
                    && o.outHeight / scale / 2 >= height)
                scale *= 2;

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
