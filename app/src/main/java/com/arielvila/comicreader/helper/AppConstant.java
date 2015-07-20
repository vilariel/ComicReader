package com.arielvila.comicreader.helper;

import java.util.Arrays;
import java.util.List;

public class AppConstant {

    // Number of columns of Grid View
    public static final int NUM_OF_COLUMNS = 3;

    // Gridview image padding
    public static final int GRID_PADDING = 8; // in dp

    // SD card image directory
    public static final String DEFAULT_DIR_NAME = "Dilbert";

    public static final String DEFAULT_FAV_NAME = "DilbertFav";

    public static final String STRIP_HOST_ORI_NAME = "remisesramallo.com.ar";

    public static final String STRIP_HOST_DISP_NAME = "(Dilbert Host)";

    public static final String REGISTER_CALL_URL = "http://remisesramallo.com.ar/utils/dilbert/registercall.php";

    public static final String STRIP_DIR_URL = "http://remisesramallo.com.ar/utils/dilbert/strip/";

    public static final int DEFAULT_GROUP_QTTY_TO_DOWNLOAD = 15;

    public static final String DOWNLOAD_EXTRA_ACTION = "DOWNLOAD_EXTRA_ACTION";

    public static final String DOWNLOAD_QTTY = "DOWNLOAD_QTTY";

    public static final int DOWNLOAD_ACTION_FIRSTRUN_OR_SHEDULE = 1;

    public static final int DOWNLOAD_ACTION_GET_PREVIOUS = 2;

    // supported file formats
    public static final List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg", "png", "gif");

    public static final long REFRESH_INTERVAL_MILLISECONDS = 3000;

    // Defines a custom Intent action
    public static final String BROADCAST_SAVED_FILE_ACTION = "com.arielvila.comicreader.BROADCAST_SAVED_FILE_ACTION";

    public static final String BROADCAST_DOWNLOAD_GROUP_END = "com.arielvila.comicreader.BROADCAST_DOWNLOAD_GROUP_END";

    public static final String BROADCAST_DOWNLOAD_GROUP_ERROR = "com.arielvila.comicreader.BROADCAST_DOWNLOAD_GROUP_ERROR";

    public static final String BROADCAST_ACTION = "BROADCAST_ACTION";
}