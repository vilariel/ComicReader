package com.arielvila.dilbert;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.arielvila.dilbert.adapter.FullScreenImageAdapter;
import com.arielvila.dilbert.animation.DepthPageTransformer;
import com.arielvila.dilbert.helper.DirContents;

import java.util.ArrayList;

public class FullScreenViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_view);
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.mipmap.icon);
        actionBar.setDisplayUseLogoEnabled(true);

        FullScreenImageAdapter adapter;
        ViewPager viewPager;

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        // TODO corregir para que pueda leer favoritos
        ArrayList<String> directory = DirContents.getIntance().getDataDir();
        String title = directory.get(position).replaceAll(".*/", "").replaceAll("\\..*", "");
        actionBar.setTitle(title);

        adapter = new FullScreenImageAdapter(FullScreenViewActivity.this, directory);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_strip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
