package com.arielvila.comicreader.helper;

import android.util.Log;

import com.arielvila.comicreader.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DirContents {
    private static final String TAG = "DirContents";
    private static DirContents instance = null;

    private static final int CURR_DIR_DATA = 1;
    private static final int CURR_DIR_FAV = 2;
    private int mCurrDirId = CURR_DIR_DATA;
    private ArrayList<String> mDataDir = new ArrayList<>();
    private ArrayList<String> mFavDir = new ArrayList<>();
    private ArrayList<String> mCurrDir = mDataDir;
    private String mDataPath = null;
    private String mFavPath = null;
    private SimpleDateFormat mDateFormatShort = new SimpleDateFormat("yyyy-MM-dd");

    public static DirContents getInstance() {
        if (instance == null) {
            instance = new DirContents();
        }
        return instance;
    }

    public ArrayList<String> getCurrDir() {
        return mCurrDir;
    }

    public void setCurrDirData() {
        mCurrDir = mDataDir;
        mCurrDirId = CURR_DIR_DATA;
    }

    public void setCurrDirFav() {
        mCurrDir = mFavDir;
        mCurrDirId = CURR_DIR_FAV;
    }

    public boolean isCurrDirData() {
        return (mCurrDirId == CURR_DIR_DATA);
    }

    public boolean isCurrDirFav() {
        return (mCurrDirId == CURR_DIR_FAV);
    }

    public ArrayList<String> getDataDir() {
        return mDataDir;
    }

    public ArrayList<String> getFavDir() {
        return mFavDir;
    }

    public void refreshDataDir(String directoryName) {
        mDataPath = directoryName;
        refreshListDir(mDataDir, directoryName);
    }

    public void refreshFavDir(String directoryName) {
        mFavPath = directoryName;
        refreshListDir(mFavDir, directoryName);
    }

    public void refreshDataDir() {
        refreshListDir(mDataDir, mDataPath);
    }

    public void refreshFavDir() {
        refreshListDir(mFavDir, mFavPath);
    }

    public String getLastDataFile() {
        String result = "";
        int last = mDataDir.size();
        if (last > 0) {
            result = mDataDir.get(last - 1);
        }
        return result;
    }

    public String getFirstDataFile() {
        String result = "";
        if (mDataDir.size() > 0) {
            result = mDataDir.get(0);
        }
        return result;
    }

    public boolean isDataDirUpToDate() {
        String lastStripName = getLastDataFile().replaceAll(".*/", "").replaceAll("\\..*", "");
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(((Date) new Date()).getTime());
        String todayStr = mDateFormatShort.format(calendar.getTime());
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "isDataDirUpToDate() - lastStripName: " + lastStripName + ", todayStr: " + todayStr);
        }
        return (lastStripName != null && todayStr != null && lastStripName.equals(todayStr));
    }

    public void addDataFile(String fileName) {
        if (!mDataDir.contains(fileName)) {
            mDataDir.add(fileName);
            Collections.sort(mDataDir);
        }
    }

    public boolean favDirContains(String stripName) {
        return (indexContains(mFavDir, stripName) >= 0);
    }

    public String getFilePath(String stripName) {
        int dataInd = indexContains(mDataDir, stripName);
        if (dataInd >= 0) {
            return mDataDir.get(dataInd);
        }
        return "";
    }

    public int getDataFilePosition(String stripName) {
        return indexContains(mDataDir, stripName);
    }

    public int getFavFilePosition(String stripName) {
        return indexContains(mFavDir, stripName);
    }

    public void toggleFavorite(String stripName) {
        int favInd = indexContains(mFavDir, stripName);
        if (favInd >= 0) {
            File favFile = new File(mFavDir.get(favInd));
            favFile.delete();
            mFavDir.remove(favInd);
        } else {
            int dataInd = indexContains(mDataDir, stripName);
            if (dataInd >= 0) {
                String fileName = mDataDir.get(dataInd).replaceAll(".*/", "");
                copyFileToFav(fileName);
                refreshFavDir();
            }
        }
    }

    private void copyFileToFav(String fileName) {
        File directory = new File(mFavPath);
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        File stripOri = new File(mDataPath + "/" + fileName);
        File stripNew = new File(mFavPath + "/" + fileName);
        InputStream in;
        OutputStream out;
        try {
            in = new FileInputStream(stripOri);
            out = new FileOutputStream(stripNew);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "Error - Cannot copy file " + fileName + " to " + mFavPath);
        }
    }

    // Only for testing
    public void removeContent(String directoryName) {
        File directory = new File(directoryName);
        if (directory.isDirectory()) {
            File[] listFiles = directory.listFiles();
            if (listFiles.length > 0) {
                for (File listFile : listFiles) {
                    String filePath = listFile.getAbsolutePath();
                    if (IsSupportedFile(filePath)) {
                        listFile.delete();
                    }
                }
            }
        } else {
            Log.e(TAG, "Error - Not a directory: " + directoryName);
        }
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
            Log.e(TAG, "Error - Not a directory: " + directoryName);
        }
    }

    private int indexContains(ArrayList<String> listDir, String stripName) {
        for (int i = 0; i < listDir.size(); i++) {
            if (listDir.get(i).contains(stripName)) {
                return i;
            }
        }
        return -1;
    }

    // Check supported file extensions
    private boolean IsSupportedFile(String filePath) {
        String ext = filePath.substring((filePath.lastIndexOf(".") + 1), filePath.length());
        return AppConstant.FILE_EXTN.contains(ext.toLowerCase(Locale.getDefault()));
    }
}
