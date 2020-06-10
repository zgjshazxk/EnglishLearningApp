package com.usts.englishlearning.config;

import java.util.ArrayList;
import java.util.List;

public class ServerData {

    public final static String SERVER_ADDRESS = "http://139.196.255.54/New";

    public final static String SERVER_LOGIN_ADDRESS = SERVER_ADDRESS + "/Login.php";
    public final static String SERVER_UPLOAD_RECORD_ADDRESS = SERVER_ADDRESS + "/upload/Record.php";
    public final static String SERVER_UPLOAD_INFO_ADDRESS = SERVER_ADDRESS + "/upload/InforFiles.php";
    public final static String SERVER_RETURN_BOOKS_ADDRESS = SERVER_ADDRESS + "/upload/GetAllFiles.php";

    public final static String LOGIN_SINA_NUM = "sinaNum";
    public final static String LOGIN_SINA_NAME = "sinaName";
    public final static String TYPE_NAME = "updateType";
    public final static String UPLOAD_FILE = "uploadedFile";
    public final static String UPLOAD_TYPE = "1";
    public final static String RECOVER_TYPE = "2";

}
