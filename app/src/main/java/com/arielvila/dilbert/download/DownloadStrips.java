package com.arielvila.dilbert.download;

import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;

import com.arielvila.dilbert.helper.AppConstant;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DownloadStrips {
    public static final String TAG = "Download Strip";

    private static DownloadStrips intance = null;

    public static DownloadStrips getIntance() {
        if (intance == null) {
            intance = new DownloadStrips();
        }
        return intance;
    }

    private SimpleDateFormat mDateFormatShort = new SimpleDateFormat("yyyy-MM-dd");

    public void downloadGroupPrevious(IStripSavedInformer informer, String dataDir, int qtty, String previousTo) {
        try {
            String androidId = Settings.Secure.getString(informer.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            getPage(AppConstant.REGISTER_CALL_URL + "?DownloadGroupPrevious&id=" + androidId + "&qtty=" + qtty + "&previousTo=" + previousTo);
            String currDay;
            if (previousTo != null && !previousTo.equals("")) {
                currDay = getPreviousDay(previousTo);
            } else {
                currDay = getPage(AppConstant.STRIP_DIR_URL + "_last.txt").toString();
            }
            for (int i = 0; i < qtty; i++) {
                saveImage(dataDir, currDay, informer);
                currDay = getPreviousDay(currDay);
            }
        } catch (IOException e) {
            informer.onDownloadGroupError(e.getMessage());
            Log.e(TAG, "Error Downloading Group: " + e.getMessage());
        } catch (Resources.NotFoundException e) {
            informer.onDownloadGroupError(e.getMessage());
            Log.e(TAG, "Error Downloading Group: " + e.getMessage());
        } catch (ParseException e) {
            informer.onDownloadGroupError(e.getMessage());
            Log.e(TAG, "Error Parsing Date: " + e.getMessage());
        }
        informer.onDownloadGroupsEnd();
    }

    public void downloadGroupPrevious(IStripSavedInformer informer, String dataDir, String lastDownloaded, String previousTo) {
        try {
            String androidId = Settings.Secure.getString(informer.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            getPage(AppConstant.REGISTER_CALL_URL + "?DownloadGroupPrevious&id=" + androidId + "&lastDonwloaded=" + lastDownloaded + "&previousTo=" + previousTo);
            String currDay;
            if (previousTo != null && !previousTo.equals("")) {
                currDay = getPreviousDay(previousTo);
            } else {
                currDay = getPage(AppConstant.STRIP_DIR_URL + "_last.txt").toString();
            }
            while (currDay.compareTo(lastDownloaded) > 0) {
                saveImage(dataDir, currDay, informer);
                currDay = getPreviousDay(currDay);
            }
        } catch (IOException e) {
            informer.onDownloadGroupError(e.getMessage());
            Log.e(TAG, "Error Downloading Group: " + e.getMessage());
        } catch (Resources.NotFoundException e) {
            informer.onDownloadGroupError(e.getMessage());
            Log.e(TAG, "Error Downloading Group: " + e.getMessage());
        } catch (ParseException e) {
            informer.onDownloadGroupError(e.getMessage());
            Log.e(TAG, "Error Parsing Date: " + e.getMessage());
        }
        informer.onDownloadGroupsEnd();
    }

    private StringBuffer getPage(String url) throws IOException {
        StringBuffer page = new StringBuffer();
        HttpUriRequest request;
        HttpResponse resp;
        InputStream is;
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

    private void saveImage(String dataDir, String graphName, IStripSavedInformer informer) throws IOException, Resources.NotFoundException {
        HttpUriRequest request;
        HttpResponse resp;
        InputStream is;
        DefaultHttpClient client = new DefaultHttpClient();
        request = new HttpGet(AppConstant.STRIP_DIR_URL + graphName + ".gif");
        resp = client.execute(request);
        int code = resp.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK) {
            throw new Resources.NotFoundException("Not found " + graphName);
        }
        HttpEntity entity = resp.getEntity();
        is = entity.getContent();
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File gifFile = new File(dataDir + "/" + graphName + ".gif");
        try {
            if (!gifFile.exists()) {
                File prefNew = new File(dataDir + "/" + graphName + ".gif");
                OutputStream out = new FileOutputStream(prefNew);
                byte[] contents = new byte[1024];
                int bytesRead;
                BufferedInputStream bis = new BufferedInputStream(is);
                while ((bytesRead = bis.read(contents)) != -1) {
                    out.write(contents, 0, bytesRead);
                }
                out.close();
                informer.onFileSaved(dataDir + "/" + graphName + ".gif");
                Log.i(TAG, "saved file: " + dataDir + "/" + graphName);
            }
        } catch (IOException e) {
            gifFile.delete();
            throw e;
        }
    }

    private String getPreviousDay(String dayString) throws ParseException {
        String result;
        Date day = mDateFormatShort.parse(dayString);
        Date prev = new Date(day.getTime() - 86400000);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(prev.getTime());
        result = mDateFormatShort.format(calendar.getTime());
        return result;
    }

}
