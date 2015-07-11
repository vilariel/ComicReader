package com.arielvila.dilbert.download;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.arielvila.dilbert.helper.AppConstant;
import com.arielvila.dilbert.helper.DirContents;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DownloadService extends IntentService {
    public DownloadService() {
        super("Dilbert Download Service");
    }

    public static final String TAG = "Download Service";

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String dataDir = prefs.getString("datadir", "");
        DirContents.getIntance().refreshDataDir(dataDir);
        String lastDataFile = DirContents.getIntance().getLastDataFile();
        String lastDay = lastDataFile.replaceAll(".*/", "").replaceAll("\\..*", "");
        String firstDay = prefs.getString("firstday", "");
        if (lastDay.equals("") || firstDay.compareTo(lastDay) < 0) {
            lastDay = firstDay;
        }
        Log.i(TAG, "lastDay: " + lastDay);
        getAllStrips(dataDir, lastDay);
        AlarmReceiver.completeWakefulIntent(intent);
    }

    private final static String URL_HOST = "http://dilbert.com";
    private static ArrayList<String> links = new ArrayList<>();
    private static ArrayList<String> names = new ArrayList<>();

    public void getAllStrips(String dataDir, String lastDay) {
        getPages(lastDay);
        saveImages(dataDir);
    }

    private void getPages(String lastDay) {
        boolean found = false;
        String pageReference = "";
        String stripHdr = "js_comic_container_";
        String lastStrip = stripHdr + lastDay;
        try {
            while (!found) {
                StringBuffer page;
                page = getPage(pageReference);
                Log.i(TAG, "page size: " + page.length());
                int indexLastDay = page.indexOf(lastStrip);
                found = (indexLastDay >= 0);
                int index = 0;
                while (index >= 0) {
                    index = page.indexOf(stripHdr, index + 1);
                    if (index >= 0) {
                        int indexImg = page.indexOf("<img alt=\"", index);
                        if (!found || (index < indexLastDay)) {
                            if (indexImg >= 0) {
                                int indexGraphIni = page.indexOf("src=\"http://assets.amuniversal", index);
                                int indexGraphEnd = page.indexOf("\"", indexGraphIni + 5);
                                String graphUrl = page.substring(indexGraphIni + 5, indexGraphEnd);
                                links.add(0, graphUrl);
                                // TODO: Correct
                                String graphName = page.substring(index + 19, index + 29);
                                names.add(0, graphName);
                            }
                        }
                    }
                }
                if (!found) {
                    index = page.indexOf("<div id=\"infinite-scrolling\"");
                    Log.i(TAG, "index: " + index);
                    if (index >= 0) {
                        int indexPage = page.indexOf("href=\"", index);
                        if (indexPage >= 0) {
                            int indexPageEnd = page.indexOf("\"", indexPage + 6);
                            if (indexPageEnd >= 0) {
                                pageReference = page.substring(indexPage + 6, indexPageEnd);
                                Log.i(TAG, "pageReference: " + pageReference);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: correct
            Log.e(TAG, "Error in getPages(): " + e.toString());
            //callerActivity.showError(e);
        }
        Log.i(TAG, "Finished getPages()");
    }

    private void saveImages(String dataDir) {
        int qtty = links.size();
        try {
            for (int i = 0; i < qtty; i++) {
                String name = names.get(i);
                saveImage(dataDir, name, links.get(i));
                //setLastDate(name.substring(0, 10));
                // TODO: que es Main?
                //Main.normalFiles.add(name);
                //callerActivity.refreshList();
            }
        } catch (Exception e) {
            // TODO: correct
            Log.e(TAG, "Error in saveImages(): " + e.toString());
            //callerActivity.showError(e);
        }
        Log.i(TAG, "Finished saveImagess()");
        Intent localIntent = new Intent(AppConstant.SAVED_ALL_FILES_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private StringBuffer getPage(String pageReference) throws IOException {
        StringBuffer page = new StringBuffer();
        HttpUriRequest request = null;
        HttpResponse resp = null;
        InputStream is = null;
        DefaultHttpClient client = new DefaultHttpClient();
        request = new HttpGet(URL_HOST + pageReference);
        resp = client.execute(request);
        HttpEntity entity = resp.getEntity();
        is = entity.getContent();
        String fileContentPart;
        byte[] contents = new byte[1024];
        int bytesRead;
        BufferedInputStream bis = new BufferedInputStream(is);
        while ((bytesRead = bis.read(contents)) != -1) {
            fileContentPart = new String(contents, 0, bytesRead);
            page.append(fileContentPart);
        }
        return page;
    }

    private void saveImage(String dataDir, String graphName, String graphUrl) throws IOException {
        HttpUriRequest request = null;
        HttpResponse resp = null;
        InputStream is = null;
        DefaultHttpClient client = new DefaultHttpClient();
        request = new HttpGet(graphUrl);
        resp = client.execute(request);
        HttpEntity entity = resp.getEntity();
        is = entity.getContent();
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File gifFile = new File(dataDir + "/" + graphName + ".gif");
        File jpgFile = new File(dataDir + "/" + graphName + ".jpg");
        if (!gifFile.exists() && !jpgFile.exists()) {
            File prefNew = new File(dataDir + "/" + graphName);
            OutputStream out = new FileOutputStream(prefNew);
            byte[] contents = new byte[1024];
            int bytesRead;
            BufferedInputStream bis = new BufferedInputStream(is);
            String extension = "";
            while ((bytesRead = bis.read(contents)) != -1) {
                if (extension.equals("")) {
                    extension = (contents[0] == 'G') ? ".gif" : ".jpg";
                }
                out.write(contents, 0, bytesRead);
            }
            out.close();
            prefNew.renameTo(new File(dataDir + "/" + graphName + extension));
            Log.i(TAG, "saved file: " + dataDir + "/" + graphName + extension);
            Intent localIntent = new Intent(AppConstant.SAVED_FILE_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
