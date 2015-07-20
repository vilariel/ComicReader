package com.arielvila.dilbert.helper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;

public class RetainMemoryCacheFragment extends Fragment {
    private static final String TAG = "RetainMemoryCacheFragment";
    private LruCache<String, Bitmap> mRetainedCache;

    public RetainMemoryCacheFragment() {}

    public static RetainMemoryCacheFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainMemoryCacheFragment fragment = (RetainMemoryCacheFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new RetainMemoryCacheFragment();
            fm.beginTransaction().add(fragment, TAG).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public LruCache<String, Bitmap> getRetainedCache() {
        if (mRetainedCache == null) {
            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMemory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;

            mRetainedCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        return mRetainedCache;
    }
}