package com.arielvila.dilbert.download;

import android.content.Context;

public interface IStripSavedInformer {
    void onFileSaved(String fileName);

    void onDownloadGroupsEnd();

    Context getContext();

}
