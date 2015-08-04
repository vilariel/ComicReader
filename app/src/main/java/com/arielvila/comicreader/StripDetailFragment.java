package com.arielvila.comicreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arielvila.comicreader.adapter.IStripImageFragment;
import com.arielvila.comicreader.adapter.StripImageAdapter;
import com.arielvila.comicreader.animation.ZoomOutPageTransformer;
import com.arielvila.comicreader.helper.AppConstant;
import com.arielvila.comicreader.helper.DirContents;
import com.arielvila.comicreader.helper.ExtendedViewPager;
import com.arielvila.comicreader.helper.StripMenu;

import java.io.File;
import java.util.ArrayList;

/**
 * A fragment representing a single Strip detail screen.
 * This fragment is either contained in a {@link StripGridActivity}
 * in two-pane mode (on tablets) or a {@link StripDetailActivity}
 * on handsets.
 */
public class StripDetailFragment extends Fragment implements IStripImageFragment {
    private static final String TAG = "StripDetailFragment";
    public static final String ARG_ITEM_ID = "item_id";

    private int mInitialPosition;
    private String mCurrentStripName = "";
    private ArrayList<String> mComicsDir;
    private StripImageAdapter mAdapter;
    private DownloadStateReceiver mDownloadStateReceiver = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StripDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (getArguments().containsKey(ARG_ITEM_ID)) {
                mInitialPosition = Integer.valueOf(getArguments().getString(ARG_ITEM_ID));
            }
        }
        // Necessary when attached to StripGridActivity and/or when recreated
        ((StripDetailCallbacks) getActivity()).setStripDetailFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onCreateView called. Initial position: " + mInitialPosition);
        }
        View fragmentView = inflater.inflate(R.layout.fragment_strip_detail, container, false);

        ExtendedViewPager viewPager = (ExtendedViewPager) fragmentView.findViewById(R.id.pager);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        mComicsDir = DirContents.getInstance().getCurrDir();

        mAdapter = new StripImageAdapter(this, mComicsDir);

        viewPager.setAdapter(mAdapter);

        // displaying selected image first
        viewPager.setCurrentItem(mInitialPosition);

        mDownloadStateReceiver = new DownloadStateReceiver();
        // The filter's action is BROADCAST_ACTION
        IntentFilter intentFilter = new IntentFilter(AppConstant.BROADCAST_SAVED_FILE_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDownloadStateReceiver, intentFilter);
        intentFilter = new IntentFilter(AppConstant.BROADCAST_DOWNLOAD_GROUP_END);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDownloadStateReceiver, intentFilter);
        intentFilter = new IntentFilter(AppConstant.BROADCAST_DOWNLOAD_GROUP_ERROR);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDownloadStateReceiver, intentFilter);
        return fragmentView;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onPrimaryItemSet(String stripName) {
        mCurrentStripName = stripName;
        updateFavoriteIcon();
        StripMenu.getInstance().setShareMenuIcon(R.drawable.ic_share_white_24dp);
        ((StripDetailCallbacks) getActivity()).setAppTitle(stripName);
        ((StripDetailCallbacks) getActivity()).setCurrentStrip(stripName);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (DirContents.getInstance().isCurrDirData()) {
            prefs.edit().putString("lastviewed", stripName).apply();
        } else {
            prefs.edit().putString("lastviewedfav", stripName).apply();
        }
    }

    public void clearCurrentStrip() {
        mCurrentStripName = "";
    }

    public String getCurrentStripName() {
        if (mCurrentStripName == null) mCurrentStripName = "";
        return mCurrentStripName;
    }

    private void updateFavoriteIcon() {
        if (DirContents.getInstance().favDirContains(mCurrentStripName)) {
            StripMenu.getInstance().setFavMenuIcon(R.drawable.ic_star_white_24dp);
        } else {
            StripMenu.getInstance().setFavMenuIcon(R.drawable.ic_star_border_white_24dp);
        }
    }

    public void setFavoriteCurrentStrip() {
        if (mCurrentStripName != null && !mCurrentStripName.equals("")) {
            DirContents.getInstance().toggleFavorite(mCurrentStripName);
            updateFavoriteIcon();
        }
    }

    public String getStripFilePath() {
        return DirContents.getInstance().getFilePath(mCurrentStripName);
    }

    public void shareCurrentStrip() {
        if (mCurrentStripName != null && !mCurrentStripName.equals("")) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("image/*");
            Uri uri = FileProvider.getUriForFile(getContext(), "com.arielvila.comicreader.fileprovider",
                    new File(getStripFilePath()));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(shareIntent);
        }
    }

    private class DownloadStateReceiver extends BroadcastReceiver {

        private DownloadStateReceiver() {
            // prevents instantiation by other packages.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppConstant.BROADCAST_SAVED_FILE_ACTION:
                    mAdapter.notifyDataSetChanged();
                    break;
                case AppConstant.BROADCAST_DOWNLOAD_GROUP_END:
                    mAdapter.notifyDataSetChanged();
                    break;
                case AppConstant.BROADCAST_DOWNLOAD_GROUP_ERROR:
                    String error = intent.getStringExtra(AppConstant.BROADCAST_ACTION);
                    Toast.makeText(StripDetailFragment.this.getActivity(), error, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * This callback is invoked when the system is about to destroy the Activity.
     */
    @Override
    public void onDestroy() {
        // If the DownloadStateReceiver still exists, unregister it and set it to null
        if (mDownloadStateReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDownloadStateReceiver);
            mDownloadStateReceiver = null;
        }
        // Must always call the super method at the end.
        super.onDestroy();
    }

    public interface StripDetailCallbacks {
        void setStripDetailFragment(StripDetailFragment stripDetailFragment);
        void setCurrentStrip(String stripName);
        void setAppTitle(String title);
    }
}
