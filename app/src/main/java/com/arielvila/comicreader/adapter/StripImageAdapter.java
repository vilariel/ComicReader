package com.arielvila.comicreader.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.arielvila.comicreader.R;
import com.arielvila.comicreader.helper.TouchImageView;

import java.util.ArrayList;

public class StripImageAdapter extends PagerAdapter {

    private final static String TAG = "StripImageAdapter";
    private IStripImageFragment mStripImageFragment;
    private ArrayList<String> mImagePaths;

    // constructor
    public StripImageAdapter(IStripImageFragment caller, ArrayList<String> imagePaths) {
        this.mStripImageFragment = caller;
        this.mImagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this.mImagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;

        LayoutInflater inflater = (LayoutInflater) mStripImageFragment.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.strip_image, container, false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        String pathName = mImagePaths.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
        imgDisplay.setImageBitmap(bitmap);
        container.addView(viewLayout);
        return viewLayout;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        String pathName = mImagePaths.get(position);
        String stripName = pathName.replaceAll(".*/", "").replaceAll("\\..*", "");
        mStripImageFragment.onPrimaryItemSet(stripName);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);

    }

}
