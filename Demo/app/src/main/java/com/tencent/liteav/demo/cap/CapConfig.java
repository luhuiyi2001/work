package com.tencent.liteav.demo.cap;

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

    /**
     * 错误处理
     */
    public static class ErrorCode {

        public static final int CREATE_TCP_ERROR = 1;

        public static final int PING_TCP_TIMEOUT = 2;
    }

}
