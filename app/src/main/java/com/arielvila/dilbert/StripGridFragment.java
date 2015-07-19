package com.arielvila.dilbert;

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
import android.widget.GridView;
import android.widget.Toast;


import com.arielvila.dilbert.adapter.GridAdapter;
import com.arielvila.dilbert.download.AlarmReceiver;
import com.arielvila.dilbert.download.DownloadService;
import com.arielvila.dilbert.helper.AppConstant;
import com.arielvila.dilbert.helper.DirContents;
import com.arielvila.dilbert.imgutil.ImageCache;
import com.arielvila.dilbert.imgutil.ImageFetcherFile;
import com.arielvila.dilbert.imgutil.Utils;

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

    private static final String IMAGE_CACHE_DIR = "thumbs";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private GridAdapter mAdapter;
    private DownloadStateReceiver mDownloadStateReceiver;
    private AlarmReceiver mAlarm = new AlarmReceiver();
    private SwipeRefreshLayout mSwipeLayout;
    private long mLastTimeRefreshed;
    private ImageFetcherFile mImageFetcher;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = GridView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StripGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_strip_grid, container, false);

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();

        mSwipeLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });

        Resources res = getResources();
        int columns = res.getInteger(R.integer.grid_columns);
        int fragmentWidthDp = res.getInteger(R.integer.grid_width_dp);
        int fragmentWidthPixels = (fragmentWidthDp > 0) ? Math.round(fragmentWidthDp * metrics.density) : metrics.widthPixels;

//        DirContents.getIntance().removeContent(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("datadir", ""));
        DirContents.getIntance().refreshDataDir(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("datadir", ""));
        DirContents.getIntance().refreshFavDir(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("favdir", ""));

        mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), columns);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcherFile(getActivity(), Math.round(new Float(fragmentWidthPixels / columns * 0.95)));
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

        mAdapter = new GridAdapter((StripGridCallbacks) getActivity(), DirContents.getIntance().getDataDir(), inflater,
                container, fragmentWidthPixels, columns, metrics.density, mImageFetcher);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // Pause fetcher to ensure smoother scrolling when flinging
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }
        });

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

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean("firstRun", true)) {
            prefs.edit().putBoolean("firstRun", false).apply();
            mAlarm.setAlarm(getActivity());
        }
        if (DirContents.getIntance().getDataDir().size() == 0) {
            downloadNow();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != GridView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    private void downloadNow() {
        // Workaround as mSwipeLayout.setRefreshing(true); doesn't work
        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(true);
            }
        });
        mLastTimeRefreshed = (new Date()).getTime();
        Intent downloadIntent = new Intent(getActivity(), DownloadService.class);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, AppConstant.DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_QTTY, getResources().getInteger(R.integer.download_qtty));
        getActivity().startService(downloadIntent);
    }

    private void initiateRefresh() {
        mLastTimeRefreshed = (new Date()).getTime();
        Intent downloadIntent = new Intent(getActivity(), DownloadService.class);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, AppConstant.DOWNLOAD_ACTION_GET_PREVIOUS);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_QTTY, getResources().getInteger(R.integer.download_qtty));
        getActivity().startService(downloadIntent);
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

    private void setActivatedPosition(int position) {
        if (position == GridView.INVALID_POSITION) {
            mAdapter.setItemChecked(mActivatedPosition, false);
        } else {
            mAdapter.setItemChecked(position, true);
        }
        mActivatedPosition = position;
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
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case AppConstant.BROADCAST_DOWNLOAD_GROUP_END:
                    mSwipeLayout.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
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
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(String id);

    }

}
