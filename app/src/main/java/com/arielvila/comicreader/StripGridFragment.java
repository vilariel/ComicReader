package com.arielvila.comicreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arielvila.comicreader.adapter.GridAdapter;
import com.arielvila.comicreader.download.AlarmReceiver;
import com.arielvila.comicreader.helper.AppConstant;
import com.arielvila.comicreader.helper.DirContents;
import com.arielvila.comicreader.helper.RetainFragment;

import java.util.Date;

/**
 * A list fragment representing a list of Strips. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link StripDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link StripGridCallbacks}
 * interface.
 */
public class StripGridFragment extends Fragment {
    private static final String TAG = "StripGridFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private GridAdapter mAdapter;
    private DownloadStateReceiver mDownloadStateReceiver;
    private AlarmReceiver mAlarm = new AlarmReceiver();
    private SwipeRefreshLayout mSwipeLayout;
    private long mLastTimeRefreshed;
    private RetainFragment mRetainFragment;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StripGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Necessary when attached to StripGridActivity and/or when recreated
        ((StripGridCallbacks) getActivity()).setStripGridFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_strip_grid, container, false);

        mRetainFragment = RetainFragment.findOrCreateRetainFragment(getFragmentManager());

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();

        mSwipeLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadLasts(true);
            }
        });

        Resources res = getResources();
        int columns = res.getInteger(R.integer.grid_columns);
        int fragmentWidthDp = res.getInteger(R.integer.grid_width_dp);
        int fragmentWidthPixels = (fragmentWidthDp > 0) ? Math.round(fragmentWidthDp * metrics.density) : metrics.widthPixels;

        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), columns);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GridAdapter((StripGridCallbacks) getActivity(), DirContents.getInstance().getCurrDir(), inflater,
                container, fragmentWidthPixels, columns, metrics.density);
        mAdapter.setLoadingImage(R.drawable.empty_photo);
        mRecyclerView.setAdapter(mAdapter);

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToLastViewed(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getString("firstRun", "1").equals("1")) {
            prefs.edit().putString("firstRun", "0").apply();
            mAlarm.setAlarm(getActivity());
        }
        if (mRetainFragment.isDownloading()) {
            setDownloadingMode(false);
            ((StripGridCallbacks) getActivity()).setDownloadingMode();
        } else if (DirContents.getInstance().getDataDir().size() == 0 || !DirContents.getInstance().isDataDirUpToDate()) {
            downloadLasts(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DirContents.getInstance().isFavDirPathChanged()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            DirContents.getInstance().refreshFavDir(prefs.getString("favdir", ""));
            if (DirContents.getInstance().isCurrDirFav()) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void downloadLasts(boolean fromSwipeDown) {
        if (fromSwipeDown && DirContents.getInstance().isDataDirUpToDate()) {
            mSwipeLayout.setRefreshing(false);
            Toast.makeText(getActivity(), R.string.mayWantDownloadPrevious, Toast.LENGTH_LONG).show();
        } else {
            setDownloadingMode(fromSwipeDown);
            mLastTimeRefreshed = (new Date()).getTime();
            ((StripGridCallbacks) getActivity()).startDownload(AppConstant.DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE);
        }
    }

    public void downloadPrevious() {
        setDownloadingMode(false);
        mLastTimeRefreshed = (new Date()).getTime();
        ((StripGridCallbacks) getActivity()).startDownload(AppConstant.DOWNLOAD_ACTION_GET_PREVIOUS);
    }

    private void setDownloadingMode(boolean fromSwipeDown) {
        mRetainFragment.setDownloading(true);
        //mAdapter.setAllowsClick(false);
        if (!fromSwipeDown) {
            // Workaround as mSwipeLayout.setRefreshing(true); doesn't work
            mSwipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(true);
                }
            });
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        mAdapter.setChoiceMode(activateOnItemClick ? GridAdapter.CHOICE_MODE_SINGLE : GridAdapter.CHOICE_MODE_NONE);
    }

    public void selectItem(String stripName) {
        String filePath = DirContents.getInstance().getFilePath(stripName);
        mAdapter.itemClick(filePath);
    }

    public void updateDirectory() {
        mAdapter.changeFilePaths(DirContents.getInstance().getCurrDir());
        scrollToLastViewed(false);
    }

    public void scrollToLastViewed(boolean smoothly) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int position;
        if (DirContents.getInstance().isCurrDirData()) {
            position = DirContents.getInstance().getDataFilePosition(prefs.getString("lastviewed", ""));
        } else {
            position = DirContents.getInstance().getDataFilePosition(prefs.getString("lastviewedfav", ""));
        }
        if (position >= 0) {
            mRecyclerView.scrollToPosition(DirContents.getInstance().getCurrDir().size() - 1);
            if (smoothly) {
                mRecyclerView.smoothScrollToPosition(position);
            } else {
                mRecyclerView.scrollToPosition(position);
            }
        }
    }

    public void clearCurrentStrip() {
        mAdapter.clearSelection();
    }

    private class DownloadStateReceiver extends BroadcastReceiver {

        private DownloadStateReceiver() {
            // prevents instantiation by other packages.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppConstant.BROADCAST_SAVED_FILE_ACTION:
                    if ((new Date()).getTime() >= mLastTimeRefreshed + AppConstant.REFRESH_INTERVAL_MILLISECONDS) {
                        mLastTimeRefreshed = (new Date()).getTime();
                        DirContents.getInstance().refreshDataDir();
                        mAdapter.notifyDataSetChanged();
                        if (intent.getIntExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, AppConstant.DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE) ==
                                AppConstant.DOWNLOAD_ACTION_GET_PREVIOUS) {
                            mRecyclerView.smoothScrollToPosition(DirContents.getInstance().getCurrDir().size() - 1);
                        }
                    }
                    break;
                case AppConstant.BROADCAST_DOWNLOAD_GROUP_END:
                    mRetainFragment.setDownloading(false);
                    mSwipeLayout.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
                    //mAdapter.setAllowsClick(true);
                    ((StripGridCallbacks) getActivity()).onDownloadEnded();
                    if (intent.getIntExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, AppConstant.DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE) ==
                            AppConstant.DOWNLOAD_ACTION_GET_PREVIOUS) {
                        mRecyclerView.smoothScrollToPosition(DirContents.getInstance().getCurrDir().size() - 1);
                    }
                    break;
                case AppConstant.BROADCAST_DOWNLOAD_GROUP_ERROR:
                    String error = intent.getStringExtra(AppConstant.BROADCAST_ACTION);
                    Toast.makeText(StripGridFragment.this.getActivity(), error, Toast.LENGTH_LONG).show();
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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface StripGridCallbacks {

        Context getContext();
        // Callback for when an item has been selected.
        void selectItem(String id);
        void setStripGridFragment(StripGridFragment stripGridFragment);
        void startDownload(int mode);
        void setDownloadingMode();
        void onDownloadEnded();
    }

}
