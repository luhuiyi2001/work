package com.tencent.liteav.demo.cap.manager;

import android.os.Handler;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapTimer;
import com.tencent.liteav.demo.cap.common.CapUtils;


public class CapExtSdcardManager {

	private static final String TAG = CapExtSdcardManager.class.getSimpleName();
	private static CapExtSdcardManager sMgr;
	private boolean mHasExtSdcard;
	private CapTimer mCheckTimer;
	private OnMountedListener mListener;
	private Handler mHandler = new Handler();

	public static CapExtSdcardManager getInstance() {
		if (sMgr == null) {
			sMgr = new CapExtSdcardManager();
		}
		return sMgr;
	}

	private CapExtSdcardManager() {
		init();
	}

	public void setOnMountedListener(OnMountedListener listener) {
		mListener = listener;
	}

	private void init() {
		mHasExtSdcard = CapUtils.checkExtSdcard();
		if (!mHasExtSdcard) {
			startCheckTimer();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					stopCheckTimer();
				}
			}, CapConfig.TIME_OUT_CHECK_EXT_SDCARD);
		}
	}

	public boolean isMounted() {
		return mHasExtSdcard;
	}


    /**
     * launcher timer
     */
    private void startCheckTimer() {
        CLog.d(TAG, "startRecorderTimer");
        if (mCheckTimer == null) {
            mCheckTimer = new CapTimer();
        }
        mCheckTimer.setOnScheduleListener(new CapTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
            	CLog.d(TAG, "onSchedule : " + mHasExtSdcard);
				mHasExtSdcard = CapUtils.checkExtSdcard();
				if (mHasExtSdcard) {
					if (mListener != null) {
						mListener.onMounted();
					}
					stopCheckTimer();
				}
            }
        });
        mCheckTimer.startTimer(CapConfig.DURATION_CHECK_EXT_SDCARD, CapConfig.DURATION_CHECK_EXT_SDCARD);
    }

    private void stopCheckTimer() {
        CLog.d(TAG, "stopRecorderTimer");
        if (mCheckTimer != null) {
            mCheckTimer.exit();
            mCheckTimer = null;
        }
    }

    public interface OnMountedListener {
    	void onMounted();
	}
}
