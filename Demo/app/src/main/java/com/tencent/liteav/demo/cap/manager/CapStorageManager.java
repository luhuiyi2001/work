package com.tencent.liteav.demo.cap.manager;

import android.os.StatFs;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.common.utils.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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

		List<String> fileList = Arrays.asList(listFiles);
		Collections.sort(fileList);
		for (int i = 0; i < fileList.size(); i++) {
			CLog.d(TAG, "file [ " + i + " ] = " + fileList.get(i));
		}
		for (int i = 0; i < fileList.size(); i++) {
			String deletePath = videoFolder.getAbsolutePath() + File.separator + fileList.get(i);
			CLog.d(TAG, "deleteFile : " + deletePath);
			FileUtils.deleteFile(deletePath);
			if (isExternalStorageSpaceEnough()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isExternalStorageSpaceEnough() {
		try {
			StatFs statFs = new StatFs(CapConfig.PATH_EXT_SDCARD);
			long availableSize = CapUtils.getAvailableSize(statFs);
			CLog.d(TAG, "availableSize : " + availableSize);
			return availableSize > CapConfig.MIN_STORAGE_SIZE;
		} catch (Exception e) {
			CLog.e(TAG, "isExternalStorageSpaceEnough : " + e.getMessage());
			return false;
		}
	}

}
