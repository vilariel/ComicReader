package com.arielvila.comicreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.arielvila.comicreader.adapter.DrawerListAdapter;
import com.arielvila.comicreader.helper.AppConstant;
import com.arielvila.comicreader.helper.DrawerItem;
import com.arielvila.comicreader.helper.StripMenu;

import java.io.File;
import java.util.ArrayList;


public class StripGridActivity extends ActionBarActivity implements StripGridFragment.StripGridCallbacks,
        StripDetailFragment.StripDetailCallbacks {

    // Whether or not the activity is in two-pane mode, i.e. running on a tablet
    private boolean mTwoPane;
    private ActionBarDrawerToggle mDrawerToggle;
    StripDetailFragment mStripDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_grid);
        setTitle(getString(R.string.app_name) + "               "); // Workaround for the title truncation issue
        setPreferencesDefaultValues();

        DrawerLayout drawerLayout;
        ListView drawerList;

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ArrayList<DrawerItem> items = new ArrayList<>();
        items.add(new DrawerItem(R.string.favTitle, R.drawable.ic_star_white_18dp));
        items.add(new DrawerItem(R.string.prefTitle, R.drawable.ic_settings_white_18dp));

        drawerList.setAdapter(new DrawerListAdapter(this, items));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        if (findViewById(R.id.strip_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((StripGridFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.strip_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (mTwoPane) {
            StripMenu.getInstance().setFavMenuItem(menu.findItem(R.id.action_favourite));
            StripMenu.getInstance().setShareMenuItem(menu.findItem(R.id.action_share));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favourite && mStripDetailFragment != null) {
            mStripDetailFragment.setFavoriteCurrentStrip();
        } else if (id == R.id.action_share && mStripDetailFragment != null) {
            mStripDetailFragment.shareCurrentStrip();
        }
        mDrawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        mDrawerToggle.onConfigurationChanged(newConfig);
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

    /**
     * Callback method from {@link StripGridFragment.StripGridCallbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(StripDetailFragment.ARG_ITEM_ID, id);
            StripDetailFragment fragment = new StripDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.strip_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, StripDetailActivity.class);
            detailIntent.putExtra(StripDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    public Context getContext() {
        return this;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectDrawerItem(position);
        }
    }

    private void selectDrawerItem(int position) {
        if (position == 1) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    public void setStripDetailFragment(StripDetailFragment stripDetailFragment) {
        this.mStripDetailFragment = stripDetailFragment;
    }
}