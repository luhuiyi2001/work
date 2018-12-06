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

    private Activity mActivity;
    private SensorManager  mSensorManager;
    private Sensor mSensor;
    private CapTimer mTimer;

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
        CLog.d(TAG, "onSensorChanged : " + event.values[0]);
        if (event.values[0] == 0) {
            //贴近手机
            mTimer.exit();
        } else {
            //离开手机
            mTimer.exit();
            mTimer.startTimer(CapConfig.TIME_THIRTY_SECONDS, CapConfig.TIME_TEN_SECONDS);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
