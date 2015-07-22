package com.arielvila.comicreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arielvila.comicreader.adapter.IStripImageFragment;
import com.arielvila.comicreader.adapter.StripImageAdapter;
import com.arielvila.comicreader.animation.DepthPageTransformer;
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
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        mComicsDir = DirContents.getIntance().getCurrDir();

        mAdapter = new StripImageAdapter(this, mComicsDir);

        viewPager.setAdapter(mAdapter);

        // displaying selected image first
        viewPager.setCurrentItem(mInitialPosition);

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
    }

    public void clearCurrentStrip() {
        mCurrentStripName = "";
    }

    public String getCurrentStripName() {
        if (mCurrentStripName == null) mCurrentStripName = "";
        return mCurrentStripName;
    }

    private void updateFavoriteIcon() {
        if (DirContents.getIntance().favDirContains(mCurrentStripName)) {
            StripMenu.getInstance().setFavMenuIcon(R.drawable.ic_star_white_24dp);
        } else {
            StripMenu.getInstance().setFavMenuIcon(R.drawable.ic_star_border_white_24dp);
        }
    }

    public void setFavoriteCurrentStrip() {
        if (mCurrentStripName != null && !mCurrentStripName.equals("")) {
            DirContents.getIntance().toggleFavorite(mCurrentStripName);
            updateFavoriteIcon();
        }
    }

    public String getStripFilePath() {
        return DirContents.getIntance().getFilePath(mCurrentStripName);
    }

    public void shareCurrentStrip() {
        if (mCurrentStripName != null && !mCurrentStripName.equals("")) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("image/*");
            Uri uri = Uri.fromFile(new File(getStripFilePath()));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(shareIntent);
        }
    }

    public interface StripDetailCallbacks {
        void setStripDetailFragment(StripDetailFragment stripDetailFragment);
        void setCurrentStrip(String stripName);
        void setAppTitle(String title);
    }
}
