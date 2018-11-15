package com.tencent.liteav.demo.cap.common;

import android.content.pm.PackageManager;

import java.io.File;

public class CapUtils {
    public static boolean checkExtSdcard() {
        if (new File(CapConfig.PATH_EXT_SDCARD).exists()) {
            return true;
        }
        return false;
    }
}
