package com.arielvila.dilbert.helper;

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

    // supported file formats
    public static final List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg", "png", "gif");

    // Defines a custom Intent action
    public static final String SAVED_FILE_ACTION = "com.arielvila.dilbert.SAVED_FILE_ACTION";

    public static final String SAVED_ALL_FILES_ACTION = "com.arielvila.dilbert.SAVED_ALL_FILES_ACTION";
}