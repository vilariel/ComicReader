package com.arielvila.dilbert;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arielvila.dilbert.adapter.IStripImageFragment;
import com.arielvila.dilbert.adapter.StripImageAdapter;
import com.arielvila.dilbert.animation.DepthPageTransformer;
import com.arielvila.dilbert.helper.DirContents;
import com.arielvila.dilbert.helper.FavoriteMenuItem;

import java.util.ArrayList;

/**
 * A fragment representing a single Strip detail screen.
 * This fragment is either contained in a {@link StripGridActivity}
 * in two-pane mode (on tablets) or a {@link StripDetailActivity}
 * on handsets.
 */
public class StripDetailFragment extends Fragment implements IStripImageFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private int mInitialPosition;

    private String mCurrentStripName;

    private ArrayList<String> mDataDir;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StripDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mInitialPosition = Integer.valueOf(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_strip_detail, container, false);

        ViewPager viewPager = (ViewPager) fragmentView.findViewById(R.id.pager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        mDataDir = DirContents.getIntance().getDataDir();

        StripImageAdapter adapter = new StripImageAdapter(this, mDataDir);

        viewPager.setAdapter(adapter);

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
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(stripName);
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (DirContents.getIntance().favDirContains(mCurrentStripName)) {
            FavoriteMenuItem.getInstance().setIcon(R.drawable.ic_star_white_24dp);
        } else {
            FavoriteMenuItem.getInstance().setIcon(R.drawable.ic_star_border_white_24dp);
        }
    }

    public void setFavoriteCurrentStrip() {
        DirContents.getIntance().toggleFavorite(mCurrentStripName);
        updateFavoriteIcon();
    }
}
