package com.arielvila.comicreader.helper;

import android.view.MenuItem;

public class StripMenu {
    private static StripMenu instance = null;

    private MenuItem mFavMenuItem = null;
    private MenuItem mShareMenuItem = null;

    public static StripMenu getInstance() {
        if (instance == null) {
            instance = new StripMenu();
        }
        return instance;
    }

    public void setFavMenuItem(MenuItem favMenuItem) {
        this.mFavMenuItem = favMenuItem;
    }

    public void setFavMenuIcon(int iconRes) {
        if (mFavMenuItem != null) {
            mFavMenuItem.setIcon(iconRes);
        }
    }

    public void setShareMenuItem(MenuItem shareMenuItem) {
        this.mShareMenuItem = shareMenuItem;
    }

    public void setShareMenuIcon(int iconRes) {
        if (mShareMenuItem != null) {
            mShareMenuItem.setIcon(iconRes);
        }
    }
}
