package com.arielvila.dilbert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.arielvila.dilbert.adapter.GridAdapter;
import com.arielvila.dilbert.helper.AppConstant;
import com.arielvila.dilbert.helper.GetStrips;
import com.arielvila.dilbert.helper.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DilbertMainActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private Utils mUtils;
    private ArrayList<String> mImagePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO ******** CORREGIR **********
        //GetStrips.getStrips(this);
        setPreferencesDefaultValues();

        mUtils = new Utils(this);

        // Calling the RecyclerView
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // The number of Columns
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // loading all image paths from SD card
        mImagePaths = mUtils.getFilePaths();

        mAdapter = new GridAdapter(this, mImagePaths);
        mRecyclerView.setAdapter(mAdapter);
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
        if (prefs.getString("lastday", "").equals("")) {
            SimpleDateFormat dateFormatShort = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            Date newNow = new Date(now.getTime() - 432000000); // 5 days earlier
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(newNow.getTime());
            prefs.edit().putString("lastday", dateFormatShort.format(calendar.getTime())).apply();
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
}
