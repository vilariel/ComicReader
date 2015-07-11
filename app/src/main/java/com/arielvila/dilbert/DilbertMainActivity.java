package com.arielvila.dilbert;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
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
    private boolean mDownloadAndSetAlermAfterPreference = false;
    private DownloadStateReceiver mDownloadStateReceiver;
    AlarmReceiver mAlarm = new AlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPreferencesDefaultValues();
        DirContents.getIntance().refreshDataDir(PreferenceManager.getDefaultSharedPreferences(this).getString("datadir", ""));

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GridAdapter(this, DirContents.getIntance().getDataDir());
        mRecyclerView.setAdapter(mAdapter);
//        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("firstRun", true).apply();

        mDownloadStateReceiver = new DownloadStateReceiver();
        // The filter's action is BROADCAST_ACTION
        IntentFilter savedFileIntentFilter = new IntentFilter(AppConstant.SAVED_FILE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, savedFileIntentFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
        controlFirstRun();
        if (mDownloadAndSetAlermAfterPreference) {
            downloadAndSetAlarm();
        }
    }

    private void controlFirstRun() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("firstRun", true)) {
            prefs.edit().putBoolean("firstRun", false).apply();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.firstRunTitle)
                    .setMessage(String.format(getString(R.string.firstRunMessage), prefs.getString("firstday", "")))
                    .setIcon(R.mipmap.icon)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            DilbertMainActivity.this.downloadAndSetAlarm();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mDownloadAndSetAlermAfterPreference = true;
                            Intent intent = new Intent(DilbertMainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    private void downloadAndSetAlarm() {
        Toast.makeText(DilbertMainActivity.this, R.string.gettingStrips, Toast.LENGTH_SHORT).show();
        Intent downloadIntent = new Intent(this, DownloadService.class);
        startService(downloadIntent);
        mAlarm.setAlarm(this);
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
        if (prefs.getString("firstday", "").equals("")) {
            SimpleDateFormat dateFormatShort = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            Date newNow = new Date(now.getTime() - 432000000); // 5 days earlier
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(newNow.getTime());
            prefs.edit().putString("firstday", dateFormatShort.format(calendar.getTime())).apply();
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
                case AppConstant.SAVED_FILE_ACTION:
                    DirContents.getIntance().refreshDataDir(
                            PreferenceManager.getDefaultSharedPreferences(DilbertMainActivity.this).getString("datadir", ""));
                    mAdapter.notifyDataSetChanged();
                    break;
                case AppConstant.SAVED_ALL_FILES_ACTION:
                    Toast.makeText(DilbertMainActivity.this, R.string.downloadComplete, Toast.LENGTH_SHORT).show();
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
