package com.arielvila.comicreader;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.arielvila.comicreader.adapter.DrawerListAdapter;
import com.arielvila.comicreader.download.DownloadService;
import com.arielvila.comicreader.helper.AppConstant;
import com.arielvila.comicreader.helper.DirContents;
import com.arielvila.comicreader.helper.DrawerItem;
import com.arielvila.comicreader.helper.StripMenu;

import java.util.ArrayList;


public class StripGridActivity extends ActionBarActivity implements StripGridFragment.StripGridCallbacks,
        StripDetailFragment.StripDetailCallbacks {

    private static final String TAG = "StripGridActivity";
    private boolean mTwoPane; // Whether or not the activity is in two-pane mode, i.e. running on a tablet
    private ActionBarDrawerToggle mDrawerToggle;
    private StripDetailFragment mStripDetailFragment = null;
    private StripGridFragment mStripGridFragment = null;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerItem mFavoriteDrawerItem;
    private DrawerItem mFavoriteDrawerItemFav;
    private DrawerItem mFavoriteDrawerItemData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_grid);
        setAppTitle("");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ArrayList<DrawerItem> items = new ArrayList<>();
        mFavoriteDrawerItemFav = new DrawerItem(R.string.favoritesTitleFav, R.drawable.ic_star_white_18dp);
        mFavoriteDrawerItemData = new DrawerItem(R.string.favoritesTitleData, R.drawable.ic_star_half_white_18dp);
        mFavoriteDrawerItem = (DirContents.getInstance().isCurrDirData()) ? mFavoriteDrawerItemFav : mFavoriteDrawerItemData;
        items.add(mFavoriteDrawerItem);
        items.add(new DrawerItem(R.string.downloadTitle, R.drawable.ic_cloud_download_white_18dp));
        items.add(new DrawerItem(R.string.prefTitle, R.drawable.ic_settings_white_18dp));

        mDrawerList.setAdapter(new DrawerListAdapter(this, items));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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

    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String openLast = intent.getStringExtra(StartActivity.START_PARAMETER_OPEN_LAST);
        if (openLast != null && !openLast.equals("")) {
            int position = DirContents.getInstance().getDataFilePosition(openLast);
            if (position >= 0) {
                selectItem(String.valueOf(position));
            }
        }
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
        if (id == R.id.action_favourite && mStripDetailFragment != null && !mStripDetailFragment.getCurrentStripName().equals("")) {
            mStripDetailFragment.setFavoriteCurrentStrip();
        } else if (id == R.id.action_share && mStripDetailFragment != null && !mStripDetailFragment.getCurrentStripName().equals("")) {
            mStripDetailFragment.shareCurrentStrip();
        }
        mDrawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Callback method from {@link StripGridFragment.StripGridCallbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void selectItem(String id) {
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

    @Override
    public void setStripGridFragment(StripGridFragment stripGridFragment) {
        mStripGridFragment = stripGridFragment;
    }

    @Override
    public void startDownload(int mode) {
        if (DirContents.getInstance().isCurrDirFav()) {
            toggleFavorites();
        } else {
            //clearSecondPaneFragment();
        }
        setDownloadingMode();
        Intent downloadIntent = new Intent(this, DownloadService.class);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, mode);
        downloadIntent.putExtra(AppConstant.DOWNLOAD_QTTY, getResources().getInteger(R.integer.download_qtty));
        startService(downloadIntent);
    }

    @Override
    public void setDownloadingMode() {
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setHomeButtonEnabled(false);
//        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onDownloadEnded() {
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        mDrawerToggle.setDrawerIndicatorEnabled(true);
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
        mDrawerLayout.closeDrawers();
        if (position == 0) {
            toggleFavorites();
        } else if (position == 1) {
            mStripGridFragment.downloadPrevious();
        } else if (position == 2) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    private void toggleFavorites() {
        DrawerListAdapter drawerListAdapter = (DrawerListAdapter) mDrawerList.getAdapter();
        drawerListAdapter.remove(mFavoriteDrawerItem);
        if (DirContents.getInstance().isCurrDirData()) {
            mFavoriteDrawerItem = mFavoriteDrawerItemData;
            DirContents.getInstance().setCurrDirFav();
        } else {
            mFavoriteDrawerItem = mFavoriteDrawerItemFav;
            DirContents.getInstance().setCurrDirData();
        }
        mStripGridFragment.updateDirectory();
        drawerListAdapter.insert(mFavoriteDrawerItem, 0);
        setAppTitle("");
        clearSecondPaneFragment();
    }

    private void clearSecondPaneFragment() {
        if (mTwoPane) {
            mStripGridFragment.clearCurrentStrip();
            StripMenu.getInstance().setShareMenuIcon(R.drawable.ic_star_invisible_24dp);
            StripMenu.getInstance().setFavMenuIcon(R.drawable.ic_star_invisible_24dp);
            if (mStripDetailFragment != null) {
                mStripDetailFragment.clearCurrentStrip();
                getSupportFragmentManager().beginTransaction()
                        .remove(mStripDetailFragment)
                        .commit();
            }
        }
    }

    public void setStripDetailFragment(StripDetailFragment stripDetailFragment) {
        this.mStripDetailFragment = stripDetailFragment;
    }

    @Override
    public void setCurrentStrip(String stripName) {
        if (mStripGridFragment != null) {
            mStripGridFragment.selectItem(stripName);
        }
    }

    @Override
    public void setAppTitle(String title) {
        String newTitle = getString(R.string.app_name);
        if (DirContents.getInstance().isCurrDirFav()) {
            newTitle = getString(R.string.favoritesTitleFav);
        }
        if (title != null && !title.equals("")) {
            newTitle += " - " + title;
        }
        newTitle += "                                      "; // Workaround for the title truncation issue
        getSupportActionBar().setTitle(newTitle);
    }
}