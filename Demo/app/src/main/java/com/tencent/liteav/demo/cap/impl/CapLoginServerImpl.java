package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.os.Handler;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.util.HeartbeatTimer;

public class CapLoginServerImpl implements CapSocketManager.OnResponseCallback, CapSocketManager.OnConnectStateCallback {
    private static final String TAG = CapLoginServerImpl.class.getSimpleName();
    private long mLastReceiveTime = 0;
    private HeartbeatTimer mBeatTimer;
    final Runnable mReloginTimeout = new Runnable() {
        @Override public void run() {
            CLog.d(TAG, "mReloginTimeout");
            CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLoginReqMsg());
        }
    };
    private CapLocationManager mLocationMgr;
    private Activity mActivity;
    private Handler mMainHandler;
    public CapLoginServerImpl(Activity context, Handler handler) {
        mActivity = context;
        mMainHandler = handler;
        // 获取系统的LocationManager服务
        mLocationMgr = new CapLocationManager(context);
    }

    /**
     * 启动心跳
     */
    public void startHeartbeatTimer() {
        CLog.d(TAG, "startHeartbeatTimer");
        stopHeartbeatTimer();
        mLastReceiveTime = System.currentTimeMillis();
        mBeatTimer = new HeartbeatTimer();
        mBeatTimer.setOnScheduleListener(new HeartbeatTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
                long receiveDuration = System.currentTimeMillis() - mLastReceiveTime;
//                CLog.d(TAG, "mBeatTimer onSchedule:" + receiveDuration);
                if (receiveDuration > CapConfig.TIME_OUT) {// 若超过三十秒都没收到我的心跳包，则认为对方不在线。
                    CLog.d(TAG, "TIME_OUT");
                    CapSocketManager.getInstance().reconnect();
                } else {
                    CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLocationReqMsg());
                }
            }

        });
        mBeatTimer.startTimer(CapConfig.HEARTBEAT_MSG_DURATION, CapConfig.HEARTBEAT_MSG_DURATION);
    }

    private void stopHeartbeatTimer() {
        CLog.d(TAG, "stopHeartbeatTimer");
        if (mBeatTimer != null) {
            mBeatTimer.exit();
            mBeatTimer = null;
        }
    }

    @Override
    public void onResponse(CapInfoResponse resp) {
        if (resp == null) {
            return;
        }
        if (CapConstants.RES_CMD_CA_LOGIN.equals(resp.cmd)) {
            CLog.d(TAG, "onResponse = [ " + resp.status + ", " + resp.msg + ", " + resp.data + " ]");
            if (resp.status == null || !resp.status) {
                mMainHandler.postDelayed(mReloginTimeout, CapConfig.DURATION_LOGIN_SERVER);
                return;
            }
            login();
        } else if(CapConstants.RES_CMD_CA_REPORT_LOCATION.equals(resp.cmd)) {
            CLog.d(TAG, "onResponse = [ " + resp.status + ", " + resp.msg + " ]");
            mLastReceiveTime = System.currentTimeMillis();
        }
    }

    private void login() {
        mMainHandler.removeCallbacks(mReloginTimeout);
        startHeartbeatTimer();
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mLocationMgr.start();
            }
        });
    }

    private void logout() {
        mMainHandler.removeCallbacks(mReloginTimeout);
        stopHeartbeatTimer();
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mLocationMgr.stop();
            }
        });
    }

    @Override
    public void notifyConnected() {
        CLog.d(TAG, "notifyConnected");
        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLoginReqMsg());
    }

    @Override
    public void notifyDisconnected() {
        CLog.d(TAG, "notifyDisconnected");
        logout();
    }
}
