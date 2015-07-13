package com.arielvila.dilbert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.arielvila.dilbert.adapter.GridAdapter;
import com.arielvila.dilbert.download.AlarmReceiver;
import com.arielvila.dilbert.download.DownloadService;
import com.arielvila.dilbert.helper.AppConstant;
import com.arielvila.dilbert.helper.DirContents;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DilbertMainActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private DownloadStateReceiver mDownloadStateReceiver;
    AlarmReceiver mAlarm = new AlarmReceiver();
    SwipeRefreshLayout mSwipeLayout;
    private int mCountNewSavedFiles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });

        setPreferencesDefaultValues();

//        DirContents.getIntance().removeContent(PreferenceManager.getDefaultSharedPreferences(this).getString("datadir", ""));
        DirContents.getIntance().refreshDataDir(PreferenceManager.getDefaultSharedPreferences(this).getString("datadir", ""));

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GridAdapter(this, DirContents.getIntance().getDataDir());
        mRecyclerView.setAdapter(mAdapter);

        mDownloadStateReceiver = new DownloadStateReceiver();
        // The filter's action is BROADCAST_ACTION
        IntentFilter intentFilter = new IntentFilter(AppConstant.BROADCAST_SAVED_FILE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, intentFilter);
        intentFilter = new IntentFilter(AppConstant.BROADCAST_DOWNLOAD_GROUP_END);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, intentFilter);
        intentFilter = new IntentFilter(AppConstant.BROADCAST_DOWNLOAD_GROUP_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, intentFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("firstRun", true)) {
            prefs.edit().putBoolean("firstRun", false).apply();
            mAlarm.setAlarm(this);
        }
        if (DirContents.getIntance().getDataDir().size() == 0) {
            downloadNow();
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
        Intent downloadIntent = new Intent(this, DownloadService.class);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, AppConstant.DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE);
        startService(downloadIntent);
    }

    private void initiateRefresh() {
        Intent downloadIntent = new Intent(this, DownloadService.class);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, AppConstant.DOWNLOAD_ACTION_GET_PREVIOUS);
        startService(downloadIntent);
    }

    private void setPreferencesDefaultValues() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("datadir", "").equals("")) {
            prefs.edit().putString("datadir", android.os.Environment.getExternalStorageDirectory() + File.separator + AppConstant.DEFAULT_DIR_NAME).apply();
        }
        if (prefs.getString("favdir", "").equals("")) {
            prefs.edit().putString("favdir", android.os.Environment.getExternalStorageDirectory() + File.separator + AppConstant.DEFAULT_FAV_NAME).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadStateReceiver extends BroadcastReceiver {

        private DownloadStateReceiver() {
            // prevents instantiation by other packages.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppConstant.BROADCAST_SAVED_FILE_ACTION:
                    mCountNewSavedFiles++;
                    if (mCountNewSavedFiles % 5 == 0) {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case AppConstant.BROADCAST_DOWNLOAD_GROUP_END:
                    mSwipeLayout.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
                    break;
                case AppConstant.BROADCAST_DOWNLOAD_GROUP_ERROR:
                    String error = intent.getStringExtra(AppConstant.BROADCAST_ACTION);
                    Toast.makeText(DilbertMainActivity.this, error, Toast.LENGTH_LONG).show();
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
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadStateReceiver);
            mDownloadStateReceiver = null;
        }
        // Must always call the super method at the end.
        super.onDestroy();
    }
}
