package com.arielvila.dilbert;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.MenuItem;

import com.arielvila.dilbert.helper.FavoriteMenuItem;


/**
 * An activity representing a single Strip detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link StripGridActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link StripDetailFragment}.
 */
public class StripDetailActivity extends ActionBarActivity {

    StripDetailFragment mStripDetailFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
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
}
