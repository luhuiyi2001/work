package com.tencent.liteav.demo.cap.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;
import android.telephony.TelephonyManager;

import com.tencent.liteav.demo.DemoApplication;
import com.tencent.liteav.demo.common.utils.VideoUtil;

import java.io.File;

public class CapUtils {
    public static boolean checkExtSdcard() {
        if (new File(CapConfig.PATH_EXT_SDCARD).exists()) {
            return true;
        }
        return false;
    }

    @TargetApi(18)
    public static long getAvailableSize(StatFs statFs) {
        long availableBytes;
        if(VideoUtil.hasJellyBeanMR2()) {
            availableBytes = statFs.getAvailableBytes();
        } else {
            availableBytes = (long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize();
        }

        return availableBytes;
    }

    //IMEI：
    public static String getImei() {
        String imei = "";
        try {
            TelephonyManager tm = (TelephonyManager) DemoApplication.getApplication().getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null)  imei = tm.getDeviceId();
            if (imei == null) imei = "";
        } catch (Exception e) {
        }
        return imei;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivity == null) {
            return false;
        } else {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
    }
}
