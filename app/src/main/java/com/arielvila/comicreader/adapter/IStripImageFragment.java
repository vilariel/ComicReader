package com.arielvila.comicreader.adapter;

import android.content.Context;

public interface IStripImageFragment {

    Context getContext();

    void onPrimaryItemSet(String stripName);
}
