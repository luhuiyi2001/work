package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapTimer;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.manager.CapAudioManager;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;

public class CapProxSensorImpl implements SensorEventListener {
    private static final String TAG = CapProxSensorImpl.class.getSimpleName();
    private static final int STATE_NEAR = 1;
    private static final int STATE_FAR= 0;
    private static final int STATE_UNKNOWN= -1;
    private Activity mActivity;
    private SensorManager  mSensorManager;
    private Sensor mSensor;
    private CapTimer mTimer;
    private int mCurState = STATE_UNKNOWN;

    public CapProxSensorImpl(Activity context) {
        mActivity = context;
    }

    public void create() {
        CLog.d(TAG, "create");
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mSensor != null)
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mTimer = new CapTimer();
        mTimer.setOnScheduleListener(new CapTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
                CLog.d(TAG, "onSchedule");
                CapAudioManager.getInstance().playNotWearingAlarm();
                CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getNotWearingReqMsg());
            }
        });
    }

    public void destroy() {
        CLog.d(TAG, "destroy");
        mSensorManager.unregisterListener(this);
        mTimer.exit();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int newState = getState(event.values[0]);
        CLog.d(TAG, "onSensorChanged : " + event.values[0] + ", newState = " + newState + ", mCurState = " + mCurState);
        if (newState == mCurState) {
            return;
        }
        mCurState = newState;
        if (mCurState == STATE_NEAR) {
            //贴近手机
            mTimer.exit();
        } else if (mCurState == STATE_FAR) {
            //离开手机
            mTimer.startTimer(CapConfig.TIME_THIRTY_SECONDS, CapConfig.TIME_TEN_SECONDS);
        } else {
            CLog.d(TAG, "do nothing!");
        }
    }

    private int getState(float sensorValue) {
        if (sensorValue == 0) {
            return STATE_NEAR;
        } else if (sensorValue > 0) {
            return STATE_FAR;
        }
        return STATE_UNKNOWN;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
