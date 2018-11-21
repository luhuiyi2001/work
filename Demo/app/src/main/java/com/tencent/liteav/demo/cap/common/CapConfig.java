package com.tencent.liteav.demo.cap.common;

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

    public static final long TIME_OUT = 30000;
    public static final int HEARTBEAT_MSG_DURATION = 10000;// 10s

    // 单个CPU线程池大小
    public static final int POOL_SIZE = 5;

    public static final String PATH_EXT_SDCARD = "/mnt/sdcard";//"/mnt/sdcard2";//"/mnt/m_external_sd";
    public static final String FILE_NAME_VIDEO_RECORD = "CapCamera";
    public static final String PATH_VIDEO_RECORD = PATH_EXT_SDCARD + File.separator + FILE_NAME_VIDEO_RECORD;

    public static final int TIME_CAMERA_RECORD =300000;// 10s
    public static final int MIN_STORAGE_SIZE = 104857600;//100M : 52428800(50M)

    public static final String TEST_USER_ID = "1004";
    public static final String TEST_IMEI = "123451234512345";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static final String URL_REQ_USER_SIG = "https://live.runde.pro/WebRtcSignApi.php?user_id=";
    public static final String URL_LOGIN_RTC_ROOM = "https://room.qcloud.com/weapp/multi_room";
}
