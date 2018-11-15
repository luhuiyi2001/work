package com.tencent.liteav.demo.cap.common;

/**
 * Created by melo on 2017/11/27.
 */

public class CapConfig {
	public static final String SERVER_IP = "47.106.114.236";
    public static final int SERVER_PORT = 9510;

    public static final long TIME_OUT = 30000;
    public static final int HEARTBEAT_MSG_DURATION = 10000;// 10s

    // 单个CPU线程池大小
    public static final int POOL_SIZE = 5;

    public static final String PATH_EXT_SDCARD = "/storage/sdcard1";
    public static final String PATH_VIDEO_RECORD = "CapCamera";

    public static final int TIME_CAMERA_RECORD =300000;// 10s

}
