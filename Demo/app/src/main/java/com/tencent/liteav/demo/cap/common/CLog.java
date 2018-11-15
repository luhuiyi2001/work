package com.tencent.liteav.demo.cap.common;

import android.util.Log;

public class CLog {

	public static final String TAG = "luhuiyi";
	public static void e(String tag, String msg) {
		Log.e(TAG, toLogMsg(tag, msg));
	}
	
	public static void v(String tag, String msg) {
		Log.v(TAG, toLogMsg(tag, msg));
	}
	
	public static void d(String tag, String msg) {
		Log.d(TAG, toLogMsg(tag, msg));
	}
	
	public static void i(String tag, String msg) {
		Log.i(TAG, toLogMsg(tag, msg));
	}
	
	public static void w(String tag, String msg) {
		Log.w(TAG, toLogMsg(tag, msg));
	}
	
	private static String toLogMsg(String tag, String msg) {
		StringBuffer logMsg = new StringBuffer();
		logMsg.append("{").append(tag).append("} - ").append(msg);
		return logMsg.toString();
	}
}
