package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;

public class CapDebugImpl {
    private static final String TAG = CapDebugImpl.class.getSimpleName();

    private Activity mActivity;
    private boolean mIsPressWithVolumnDown;
    private boolean mIsPressWithVolumnUp;

    public CapDebugImpl(Activity context) {
        mActivity = context;
    }

    public void create() {
    }

    public void destroy() {
    }

    public void keyDown(int keyCode, KeyEvent event) {
        CLog.d(TAG, "keyDown = " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mIsPressWithVolumnDown = true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mIsPressWithVolumnUp = true;
        }

        if (mIsPressWithVolumnDown && mIsPressWithVolumnUp) {
            switchAdbEnable();
        }
    }

    public void keyUp(int keyCode, KeyEvent event) {
        CLog.d(TAG, "keyUp = " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mIsPressWithVolumnDown = false;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mIsPressWithVolumnUp = false;
        }
    }

    private void switchAdbEnable() {
        CLog.d(TAG, "switchAdbEnable");
        int adbEnabled = Settings.Global.getInt(mActivity.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
        Settings.Global.putInt(mActivity.getContentResolver(), Settings.Global.ADB_ENABLED, adbEnabled == 1 ? 0 : 1);
    }
}
