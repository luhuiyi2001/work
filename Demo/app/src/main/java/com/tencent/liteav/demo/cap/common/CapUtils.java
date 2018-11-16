package com.tencent.liteav.demo.cap.common;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.StatFs;

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
}
