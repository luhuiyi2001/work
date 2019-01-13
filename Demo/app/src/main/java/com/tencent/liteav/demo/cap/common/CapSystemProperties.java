package com.tencent.liteav.demo.cap.common;

import java.lang.reflect.Method;

public class CapSystemProperties {
    private static final String TAG = CapSystemProperties.class.getSimpleName();
    private static Class<?> mClassType = null;
    private static Method mGetMethod = null;

    public static String getRtcUrlType() {
		return CapSystemProperties.get("persist.rd.config.rtc_url_type", CapConstants.NAME_RUNDE);
	}

	public static String getSocketType() {
		return CapSystemProperties.get("persist.rd.config.socket_type", CapConstants.NAME_WEB);
	}

	public static String get(String key, String defValue) {
		CLog.d(TAG, "get : [ " + key + ", " + defValue + " ]");
		init();
	    try {
	    	String value = (String)mGetMethod.invoke(mClassType, new Object[] { key });
	    	if (value != null) {
				CLog.d(TAG, "key = "+ value);
	    		return value;
	    	}
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return defValue;
    }
  
    private static void init() {
		CLog.d(TAG, "init : " + mClassType);
        try {
            if (mClassType == null) {
                mClassType = Class.forName("android.os.SystemProperties");
                mGetMethod = mClassType.getDeclaredMethod("get", new Class[] { String.class });
            }
        } catch (Exception e) {
        	e.printStackTrace();
	    }
	}
}