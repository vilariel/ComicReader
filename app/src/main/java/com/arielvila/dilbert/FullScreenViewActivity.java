package com.arielvila.dilbert;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.arielvila.dilbert.adapter.FullScreenImageAdapter;
import com.arielvila.dilbert.animation.DepthPageTransformer;
import com.arielvila.dilbert.helper.DirContents;

public class FullScreenViewActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_view);

        FullScreenImageAdapter adapter;
        ViewPager viewPager;

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);


        // TODO corregir para que pueda leer favoritos
        adapter = new FullScreenImageAdapter(FullScreenViewActivity.this, DirContents.getIntance().getDataDir());

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }
}
