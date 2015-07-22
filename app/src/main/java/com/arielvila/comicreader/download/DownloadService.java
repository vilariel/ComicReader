package com.arielvila.comicreader.download;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.arielvila.comicreader.helper.AppConstant;
import com.arielvila.comicreader.helper.DirContents;

public class DownloadService extends IntentService implements IStripSavedInformer {
    public DownloadService() {
        super("Comic Download Service");
    }

    public static final String TAG = "Download Service";

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String dataDir = prefs.getString("datadir", "");
        DirContents.getInstance().refreshDataDir(dataDir);
        int qttyToDownload = intent.getIntExtra(AppConstant.DOWNLOAD_QTTY, AppConstant.DEFAULT_GROUP_QTTY_TO_DOWNLOAD);
        switch (intent.getIntExtra(AppConstant.DOWNLOAD_EXTRA_ACTION, AppConstant.DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE)) {
            case AppConstant.DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE :
                String lastDataFile = DirContents.getInstance().getLastDataFile();
                String lastDay = lastDataFile.replaceAll(".*/", "").replaceAll("\\..*", "");
                Log.i(TAG, "lastDay: " + lastDay);
                if (lastDay.equals("")) {
                    DownloadStrips.getInstance().downloadGroupPrevious(this, dataDir, qttyToDownload, "");
                } else {
                    DownloadStrips.getInstance().downloadGroupPrevious(this, dataDir, lastDay, "");
                }
                AlarmReceiver.completeWakefulIntent(intent);
                break;
            case AppConstant.DOWNLOAD_ACTION_GET_PREVIOUS :
                String firstDataFile = DirContents.getInstance().getFirstDataFile();
                String firstDay = firstDataFile.replaceAll(".*/", "").replaceAll("\\..*", "");
                DownloadStrips.getInstance().downloadGroupPrevious(this, dataDir, qttyToDownload, firstDay);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFileSaved(String fileName) {
        DirContents.getInstance().addDataFile(fileName);
        Intent localIntent = new Intent(AppConstant.BROADCAST_SAVED_FILE_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onDownloadGroupsEnd() {
        Intent localIntent = new Intent(AppConstant.BROADCAST_DOWNLOAD_GROUP_END);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onDownloadGroupError(String error) {
        Intent localIntent = new Intent(AppConstant.BROADCAST_DOWNLOAD_GROUP_ERROR);
        error = error.replace(AppConstant.STRIP_HOST_ORI_NAME, AppConstant.STRIP_HOST_DISP_NAME);
        localIntent.putExtra(AppConstant.BROADCAST_ACTION, error);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public Context getContext() {
        return this;
    }

    /*
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
            registerCall(lastDay);
            while (!found) {
                StringBuffer page;
                page = getPage(AppConstant.DILBERT_URL + pageReference);
//                FileWriter writer = new FileWriter(dataDir + "/page" + pageReference.replaceAll("[/&?\\*]", ""));
//                writer.write(page.toString());
//                writer.flush();
//                writer.close();
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

    private void registerCall(String lastDay) throws IOException {
        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        getPage(AppConstant.REGISTER_CALL_URL + "?id=" + androidId + "&lastDay=" + lastDay);
    }

    private StringBuffer getPage(String url) throws IOException {
        StringBuffer page = new StringBuffer();
        HttpUriRequest request = null;
        HttpResponse resp = null;
        InputStream is = null;
        DefaultHttpClient client = new DefaultHttpClient();
        request = new HttpGet(url);
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
            Intent localIntent = new Intent(AppConstant.BROADCAST_SAVED_FILE_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
    */
}
