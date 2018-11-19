package com.tencent.liteav.demo.cap.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
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
        return "255533366988887";
        //return "123450123456789";
    }
}
