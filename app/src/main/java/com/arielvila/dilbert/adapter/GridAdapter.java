package com.arielvila.dilbert.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arielvila.dilbert.FullScreenViewActivity;
import com.arielvila.dilbert.R;

import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Edwin on 28/02/2015.
 */
public class GridAdapter  extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<String> mFilePaths = new ArrayList<String>();

    public GridAdapter(Activity activity, ArrayList<String> filePaths) {
        super();

        mActivity = activity;
        mFilePaths = filePaths;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
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
        String result = "";
        int len = filePath.length();
        result = filePath.substring(len - 14, len - 4);
        return result;
    }

    class OnImageClickListener implements OnClickListener {

        int _postion;

        // constructor
        public OnImageClickListener(int position) {
            this._postion = position;
        }

        @Override
        public void onClick(View v) {
            // on selecting grid view image
            // launch full screen activity
            Intent i = new Intent(mActivity, FullScreenViewActivity.class);
            i.putExtra("position", _postion);
            mActivity.startActivity(i);
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
