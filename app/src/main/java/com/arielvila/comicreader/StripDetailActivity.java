package com.arielvila.comicreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.MenuItem;

import com.arielvila.comicreader.helper.FavoriteMenuItem;


public class StripDetailActivity extends ActionBarActivity implements StripDetailFragment.StripDetailCallbacks {

    private static final String TAG = "StripDetailActivity";
    StripDetailFragment mStripDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(StripDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(StripDetailFragment.ARG_ITEM_ID));
            mStripDetailFragment = new StripDetailFragment();
            mStripDetailFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.strip_detail_container, mStripDetailFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        FavoriteMenuItem.getInstance().setMenuItem(menu.findItem(R.id.action_favourite));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Home or Up button http://developer.android.com/design/patterns/navigation.html#up-vs-back
            navigateUpTo(new Intent(this, StripGridActivity.class));
            return true;
        } else if (id == R.id.action_favourite) {
            mStripDetailFragment.setFavoriteCurrentStrip();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setStripDetailFragment(StripDetailFragment stripDetailFragment) {
        this.mStripDetailFragment = stripDetailFragment;
    }
}
