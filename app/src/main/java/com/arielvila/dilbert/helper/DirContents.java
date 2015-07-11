package com.arielvila.dilbert.helper;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class DirContents {
    private static DirContents intance = null;

    private ArrayList<String> dataDir = new ArrayList<>();
    private ArrayList<String> favDir = new ArrayList<>();

    public static DirContents getIntance() {
        if (intance == null) {
            intance = new DirContents();
        }
        return intance;
    }

    public ArrayList<String> getDataDir() {
        return dataDir;
    }

    public ArrayList<String> getFavDir() {
        return favDir;
    }

    public void refreshDataDir(String directoryName) {
        refreshListDir(dataDir, directoryName);
    }

    public void refreshFavDir(String directoryName) {
        refreshListDir(favDir, directoryName);
    }

    public String getLastDataFile() {
        String result = "";
        int last = dataDir.size();
        if (last > 0) {
            result = dataDir.get(last - 1);
        }
        return result;
    }

    private void refreshListDir(ArrayList<String> listDir, String directoryName) {
        listDir.clear();
        File directory = new File(directoryName);
        // check for directory
        if (directory.isDirectory()) {
            // getting list of file paths
            File[] listFiles = directory.listFiles();
            // Check for count
            if (listFiles.length > 0) {
                // loop through all files
                for (File listFile : listFiles) {
                    // get file path
                    String filePath = listFile.getAbsolutePath();
                    // check for supported file extension
                    if (IsSupportedFile(filePath)) {
                        // Add image path to array list
                        listDir.add(filePath);
                    }
                }
            }
            Collections.sort(listDir);
        } else {
            Log.e("DirContents", "Error - Not a directory: " + directoryName);
        }
    }

    // Check supported file extensions
    private boolean IsSupportedFile(String filePath) {
        String ext = filePath.substring((filePath.lastIndexOf(".") + 1), filePath.length());
        return AppConstant.FILE_EXTN.contains(ext.toLowerCase(Locale.getDefault()));
    }
}
