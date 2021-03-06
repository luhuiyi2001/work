package com.tencent.liteav.demo.cap.common;

import android.os.Build;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by melo on 2017/11/27.
 */

public class CapConfig {
	public static final String SERVER_IP = "47.106.114.236";
    public static final int SERVER_PORT = 9510;
    public static final int SDK_APP_ID = 1400160417;
    public static final String ACC_TYPE = "36862";

    public static final boolean USE_WEB_SOCKECT = CapConstants.NAME_WEB.equals(CapSystemProperties.getSocketType());

    public static final int DURATION_SEND_LOCATION_MSG = 10000;// 10s
    public static final int TIMER_IMEI_MONITOR = 5000;// 5s
    public static final int DURATION_RELOGIN_SERVER = 30000;
    public static final int DURATION_RECONNECT_SERVER = 30000;
    public static final int DURATION_RESPONSE_MSG= 5000;
    public static final int DURATION_CHECK_EXT_SDCARD= 5000;
    public static final int TIME_OUT_CHECK_EXT_SDCARD= 60 * 1000;

    public static final int TIME_THIRTY_SECONDS= 30 * 1000;
    public static final int TIME_TEN_SECONDS= 10 * 1000;

    // 单个CPU线程池大小
    public static final int POOL_SIZE = 5;


    public static final boolean IS_TEST = Build.BRAND.equals("HUAWEI") || Build.BRAND.equals("samsung") ? true : false;

    public static final String FILE_NAME_VIDEO_RECORD = "CapCamera";
    public static final String FILE_NAME_PICTURE = "CapPicture";

    public static final int TIME_CAMERA_RECORD =300000;// 10s
    public static final int MIN_STORAGE_SIZE = 104857600 * 20;//100M : 52428800(50M)

    public static final String PATH_EXT_SDCARD = IS_TEST ? "/mnt/sdcard" : "/mnt/m_external_sd";//"/mnt/sdcard2";//"/mnt/m_external_sd";
    public static final String PATH_VIDEO_RECORD = PATH_EXT_SDCARD + File.separator + FILE_NAME_VIDEO_RECORD;
    public static final String PATH_PHOTO = PATH_EXT_SDCARD + File.separator + FILE_NAME_PICTURE;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static final String URL_REQ_USER_SIG = "https://live.runde.pro/WebRtcSignApi.php?user_id=";
    public static final String URL_LOGIN_RTC_ROOM = CapConstants.NAME_RUNDE.equals(CapSystemProperties.getRtcUrlType()) ? "http://aqm.runde.pro:5757/weapp/multi_room" : "https://room.qcloud.com/weapp/multi_room";
}
