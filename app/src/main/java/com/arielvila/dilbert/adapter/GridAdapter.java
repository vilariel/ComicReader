package com.arielvila.dilbert.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
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

import com.arielvila.dilbert.BuildConfig;
import com.arielvila.dilbert.R;
import com.arielvila.dilbert.StripGridFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
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
    private int mCardWidth;
    private int mLayoutHeight;
    private int mImageHeight;
    private int mTextHeight;
    private int mTextSize;
    private Bitmap mLoadingBitmap;
    private LruCache<String, Bitmap> mMemoryCache;

    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    public static final int CHOICE_MODE_MULTIPLE = 2;

    public GridAdapter(StripGridFragment.StripGridCallbacks callback, ArrayList<String> filePaths,
                       LayoutInflater inflater, ViewGroup container, int width, int columns, float density,
                       LruCache<String, Bitmap> memoryCache) {
        super();
        mInflater = inflater;
        mContainer = container;
        mCallback = callback;
        mFilePaths = filePaths;
        mResources = callback.getContext().getResources();
        mSelectedItems = new SparseBooleanArray();
        mChoiceMode = CHOICE_MODE_NONE;
        mMemoryCache = memoryCache;
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

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
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
    public void loadBitmap(String filePath, ImageView imageView) {
        final Bitmap bitmap = getBitmapFromMemCache(filePath);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (cancelPotentialWork(filePath, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(filePath);
            }
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.getData();
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> mBitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            mBitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return mBitmapWorkerTaskReference.get();
        }
    }

    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> mImageViewReference;
        private String mData = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mImageViewReference = new WeakReference<ImageView>(imageView);
        }

        public String getData() {
            return mData;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            mData = params[0];
            final Bitmap bitmap = decodeFile(mData, 400, 100);
            if (bitmap != null) {
                addBitmapToMemoryCache(params[0], bitmap);
            }
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (mImageViewReference != null && bitmap != null) {
                final ImageView imageView = mImageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        /*
         * Resizing image size
         */
        public Bitmap decodeFile(String filePath, int width, int height) {
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
}
