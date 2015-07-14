package com.arielvila.dilbert.helper;

import android.view.MenuItem;

public class FavoriteMenuItem {
    private static FavoriteMenuItem instance = null;

    private MenuItem menuItem = null;

    public static FavoriteMenuItem getInstance() {
        if (instance == null) {
            instance = new FavoriteMenuItem();
        }
        return instance;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public void setIcon(int iconRes) {
        if (menuItem != null) {
            menuItem.setIcon(iconRes);
        }
    }
}
