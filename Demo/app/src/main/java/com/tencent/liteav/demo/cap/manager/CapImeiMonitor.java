package com.tencent.liteav.demo.cap.manager;

import android.text.TextUtils;

import com.tencent.liteav.demo.cap.callback.OnImeiCallback;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.util.CapTimer;

public class CapImeiMonitor {
    private static final String TAG = CapImeiMonitor.class.getSimpleName();

    private CapTimer mTimer;
    private OnImeiCallback mCallback;

    public void setOnImeiCallback(OnImeiCallback callback) {
        mCallback = callback;
    }

    public void start(){
        CLog.d(TAG, "start");
        if (!TextUtils.isEmpty(CapUtils.getImei())) {
            CapSocketManager.getInstance().connect();
            return;
        }
        startTimer();
    }
    /**
     * launcher timer
     */
    private void startTimer() {
        CLog.d(TAG, "startTimer");
        if (mTimer == null) {
            mTimer = new CapTimer();
        }
        mTimer.setOnScheduleListener(new CapTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
                String imei = CapUtils.getImei();
                CLog.d(TAG, "onSchedule IMEI = " + imei);
                if (!TextUtils.isEmpty(CapUtils.getImei())) {
                    stopTimer();
                    if (mCallback != null) {
                        mCallback.notifyDataChanged();
                    }
                }
            }
        });
        mTimer.startTimer(1000, CapConfig.TIMER_IMEI_MONITOR);
    }

    private void stopTimer() {
        CLog.d(TAG, "stopTimer");
        if (mTimer != null) {
            mTimer.exit();
            mTimer = null;
        }
    }
}
