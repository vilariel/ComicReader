package com.arielvila.comicreader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arielvila.comicreader.helper.AppConstant;
import com.arielvila.comicreader.helper.DirContents;

import java.io.File;

public class StartActivity extends Activity {
    private static final String TAG = "StartActivity";
    public final static String START_PARAMETER_OPEN_LAST = "com.arielvila.comicreader.START_PARAMETER_OPEN_LAST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPreferencesDefaultValues();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        DirContents.getInstance().removeContent(prefs.getString("datadir", ""));
        DirContents.getInstance().refreshDataDir(prefs.getString("datadir", ""));
        DirContents.getInstance().refreshFavDir(prefs.getString("favdir", ""));
        Intent intent = new Intent(this, StripGridActivity.class);
        String mustOpen = "";
        if (prefs.getBoolean("openlast", false)) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onCreate() must open last");
            }
            mustOpen = prefs.getString("lastviewed", "");
        }
        intent.putExtra(START_PARAMETER_OPEN_LAST, mustOpen);
        startActivity(intent);
        finish();
    }

    private void setPreferencesDefaultValues() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("datadir", "").equals("")) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "DataDir set to: " + getFilesDir() + File.separator + AppConstant.DEFAULT_DIR_NAME);
            }
            prefs.edit().putString("datadir", getFilesDir() + File.separator + AppConstant.DEFAULT_DIR_NAME).apply();
        }
        if (prefs.getString("favdir", "").equals("")) {
            prefs.edit().putString("favdir", android.os.Environment.getExternalStorageDirectory() + File.separator + AppConstant.DEFAULT_FAV_NAME).apply();
        }
    }

}
