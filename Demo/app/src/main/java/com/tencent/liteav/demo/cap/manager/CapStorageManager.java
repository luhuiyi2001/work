package com.tencent.liteav.demo.cap.manager;

import android.os.StatFs;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.common.utils.FileUtils;

import java.io.File;


public class CapStorageManager {

	private static final String TAG = CapStorageManager.class.getSimpleName();
	private static CapStorageManager sMgr;

	public static CapStorageManager getInstance() {
		if (sMgr == null) {
			sMgr = new CapStorageManager();
		}
		return sMgr;
	}

	private CapStorageManager() {
	}

	public boolean checkExternalStorageSpaceEnough() {
		if (isExternalStorageSpaceEnough()) {
			return true;
		}
		File videoFolder = new File(CapConfig.PATH_VIDEO_RECORD);
		if (!videoFolder.exists()) {
			return false;
		}
		String[] listFiles = videoFolder.list();
		if (listFiles == null || listFiles.length == 0) {
			return false;
		}

		for (int i = 0; i < listFiles.length; i++) {
			CLog.d(TAG, listFiles[i]);
			FileUtils.deleteFile(videoFolder.getAbsolutePath() + File.separator + listFiles[i]);
			if (isExternalStorageSpaceEnough()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isExternalStorageSpaceEnough() {
		StatFs statFs = new StatFs(CapConfig.PATH_EXT_SDCARD);
		return CapUtils.getAvailableSize(statFs) > CapConfig.MIN_STORAGE_SIZE;
	}

}