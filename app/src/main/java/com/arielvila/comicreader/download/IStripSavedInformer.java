package com.arielvila.comicreader.download;

import android.content.Context;

public interface IStripSavedInformer {
    void onFileSaved(String fileName);

    void onDownloadGroupsEnd();

    void onDownloadGroupError(String error);

    Context getContext();

}
