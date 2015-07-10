package com.arielvila.dilbert.helper;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class DirContents {
    private static DirContents intance = null;

    private ArrayList<String> dataDir;
    private ArrayList<String> favDir;

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
        dataDir = getFilePaths(directoryName);
    }

    public void refreshFavDir(String directoryName) {
        favDir = getFilePaths(directoryName);
    }

    private ArrayList<String> getFilePaths(String directoryName) {
        ArrayList<String> filePaths = new ArrayList<>();
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
                        filePaths.add(filePath);
                    }
                }
            }
        } else {
            Log.e("DirContents", "Error - Not a directory: " + directoryName);
        }
        return filePaths;
    }

    // Check supported file extensions
    private boolean IsSupportedFile(String filePath) {
        String ext = filePath.substring((filePath.lastIndexOf(".") + 1), filePath.length());
        if (AppConstant.FILE_EXTN.contains(ext.toLowerCase(Locale.getDefault()))) {
            return true;
        } else {
            return false;
        }
    }



}
