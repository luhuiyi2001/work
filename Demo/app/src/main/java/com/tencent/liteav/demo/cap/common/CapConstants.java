package com.tencent.liteav.demo.cap.common;

public class CapConstants {

    public static final String RES_CMD_CA_LOGIN = "ca_login";
    public static final String RES_CMD_CA_SOS = "ca_sos";
    public static final String RES_CMD_CA_REPORT_LOCATION = "ca_report_location";
    public static final String RES_CMD_SERVER_PUSH_OPEN_RTSP = "server_push_open_rtsp";
    public static final String RES_CMD_SERVER_PUSH_STOP_RTSP = "server_push_stop_rtsp";
    public static final String RES_CMD_SERVER_PULL_WIFI_LIST = "server_pull_wifi_list";
    public static final String RES_CMD_SERVER_PUSH_CONNECT_WIFI = "server_push_set_wifi";
    public static final String RES_CMD_SERVER_PUSH_OPEN_VIDEO_CALL = "server_push_open_video_call";
    public static final String RES_CMD_SERVER_PUSH_OPEN_AUDIO_CALL = "server_push_open_audio_call";
    public static final String RES_CMD_SERVER_PUSH_APP_ASK_FOR_HELP = "server_push_app_ask_for_help";


    public static final String REQ_ACT_CA_LOGIN = "ca_login";
    public static final String REQ_ACT_CA_SOS = "ca_sos";
    public static final String REQ_ACT_CA_REPORT_LOCATION = "ca_report_location";
    public static final String REQ_ACT_CA_UPLOAD_WIFI_LIST = "ca_upload_wifi_list";
    public static final String REQ_ACT_CA_REPORT_WIFI_CONNECT_STATUS = "ca_report_wifi_connect_status";
    public static final String REQ_ACT_CA_CREATE_ROOM_FOR_HELP = "ca_create_room_for_help";
    public static final String REQ_ACT_CA_REPORT_ROOM_STATUS = "ca_report_room_status";
    public static final String REQ_ACT_CA_CALL_MANAGER = "ca_call_manager";

    public static final String ACT_KEY_APP_SET_WIFI = "app_set_wifi";

    public static final String ACTION_UPDATE_STATE_INFO = "com.android.runde.ACTION_UPDATE_STATE_INFO";
    public static final String ACTION_START_PUBLISH = "com.android.runde.ACTION_START_PUBLISH";
    public static final String ACTION_STOP_PUBLISH = "com.android.runde.ACTION_STOP_PUBLISH";
    public static final String EXTRA_UPDATE_STATE_INFO = "update_info";
    public static final String EXTRA_PUSH_URL = "push_url";
    public static final String EXTRA_CMD_CLIENT = "cmd_client";
    public static final String EXTRA_CMD_RECORDER = "cmd_recorder";


    public static final String CMD_START = "start";
    public static final String CMD_STOP = "stop";

    public static final String CHARSET_NAME_UTF8 = "UTF-8";
    public static final String SHARED_PREFS_NAME = "com.runde.cap";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_SIG = "userSig";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_USER_IMG = "userImg";
    public static final String KEY_URL = "url";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public static final int MODE_IDLE = 0;
    public static final int MODE_RECORDING = 1;
    public static final int MODE_START_PUSHER = 2;
    public static final int MODE_CHAT = 3;

}
