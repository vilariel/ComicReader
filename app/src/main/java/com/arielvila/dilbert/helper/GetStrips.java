package com.arielvila.dilbert.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.arielvila.dilbert.R;

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

public class GetStrips {
    private final static String URL_HOST = "http://dilbert.com";
    private static String dataDir = "/storage/sdcard/Dilbert";
    private static String lastDay = "2015-05-29";
    private static boolean running = false;
    private static Context callerActivity = null;
    private static ArrayList<String> links = new ArrayList<String>();
    private static ArrayList<String> names = new ArrayList<String>();
    private static StringBuffer log = new StringBuffer();

    public static void getStrips(Context caller) {
        callerActivity = caller;
        //SharedPreferences prefs = callerActivity.getSharedPreferences(Main.PREF_PACKAGE, Context.MODE_PRIVATE);
        //dataDir = prefs.getString("datadir", "");
        //lastDay = prefs.getString("lastday", "");
        if (!running) {
            Toast.makeText(callerActivity, R.string.gettingStrips, Toast.LENGTH_SHORT).show();
            Thread t = new Thread() {
                public void run() {
                    running = true;
                    GetStrips getStrips = new GetStrips();
                    getStrips.getAllStrips();
                    running = false;
                    //callerActivity.refreshList();
                }
            };
            t.start();
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public void getAllStrips() {
        getPages();
        saveImages();
    }

    private void getPages() {
        boolean found = false;
        String pageReference = "";
        String stripHdr = "js_comic_container_";
        String lastStrip = stripHdr + lastDay;
        try {
            while (!found) {
                StringBuffer page;
                page = getPage(pageReference);
                Log.i("GetStrips", "page size: " + page.length());
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
                    Log.i("GetStrips", "index: " + index);
                    if (index >= 0) {
                        int indexPage = page.indexOf("href=\"", index);
                        if (indexPage >= 0) {
                            int indexPageEnd = page.indexOf("\"", indexPage + 6);
                            if (indexPageEnd >= 0) {
                                pageReference = page.substring(indexPage + 6, indexPageEnd);
                                Log.i("GetStrips", "pageReference: " + pageReference);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: correct
            Log.e("GetStrips.getPages()", e.toString());
            //callerActivity.showError(e);
        }
    }

    private void saveImages() {
        int qtty = links.size();
        try {
            for (int i = 0; i < qtty; i++) {
                String name = names.get(i);
                saveImage(name, links.get(i));
                //setLastDate(name.substring(0, 10));
                // TODO: que es Main?
                //Main.normalFiles.add(name);
                //callerActivity.refreshList();
            }
        } catch (Exception e) {
            // TODO: correct
            Log.e("GetStrips.saveImages()", e.toString());
            //callerActivity.showError(e);
        }
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

    private void saveImage(String graphName, String graphUrl) throws IOException {
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
        Log.i("GetStrips.saveImages()", "Saved " + dataDir + "/" + graphName + extension);
    }

    private void setLastDate(String lastDay) {
//        SharedPreferences prefs = callerActivity.getSharedPreferences(Main.PREF_PACKAGE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("lastday", lastDay);
//        editor.commit();
    }
}
