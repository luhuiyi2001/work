package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.os.Handler;

import com.google.gson.Gson;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.socket.CapUser;
import com.tencent.liteav.demo.cap.common.CapTimer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CapLoginServerImpl implements CapSocketManager.OnResponseCallback, CapSocketManager.OnConnectStateCallback {
    private static final String TAG = CapLoginServerImpl.class.getSimpleName();
//    private long mLastReceiveTime = 0;
    private CapTimer mLocationTimer;
    private final Runnable mReloginRunnable = new Runnable() {
        @Override public void run() {
            CLog.d(TAG, "mReloginRunnable");
            sendLoginMsg();
        }
    };

    private final Runnable mReconnectRunnable = new Runnable() {
        @Override public void run() {
            CLog.d(TAG, "mReconnectRunnable");
            if (CapUtils.isNetworkAvailable(mActivity)) {
                CapSocketManager.getInstance().connect();
            }
        }
    };

    private final Runnable mDisconnectRunnable = new Runnable() {
        @Override public void run() {
            CLog.d(TAG, "mDisconnectRunnable");
            CapSocketManager.getInstance().disconnect();
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
    public void startLocationTimer() {
        CLog.d(TAG, "startLocationTimer");
        stopLocationTimer();
//        mLastReceiveTime = System.currentTimeMillis();
        mLocationTimer = new CapTimer();
        mLocationTimer.setOnScheduleListener(new CapTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
            sendLocationMsg();
//                long receiveDuration = System.currentTimeMillis() - mLastReceiveTime;
////                CLog.d(TAG, "mLocationTimer onSchedule:" + receiveDuration);
//                if (receiveDuration > CapConfig.TIME_OUT) {// 若超过三十秒都没收到我的心跳包，则认为对方不在线。
//                    CLog.d(TAG, "TIME_OUT");
//                    CapSocketManager.getInstance().connect();
//                } else {
//                    CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLocationReqMsg());
//                }
            }

        });
        mLocationTimer.startTimer(CapConfig.DURATION_SEND_LOCATION_MSG, CapConfig.DURATION_SEND_LOCATION_MSG);
    }

    private void stopLocationTimer() {
        CLog.d(TAG, "stopLocationTimer");
        if (mLocationTimer != null) {
            mLocationTimer.exit();
            mLocationTimer = null;
        }
    }

    @Override
    public void onResponse(CapInfoResponse resp) {
        if (resp == null) {
            return;
        }
        if (CapConstants.RES_CMD_CA_LOGIN.equals(resp.cmd)) {
            CLog.d(TAG, "onResponse = [ " + resp.status + ", " + resp.msg + ", " + resp.data + " ]");
            mMainHandler.removeCallbacks(mDisconnectRunnable);
            mMainHandler.removeCallbacks(mReloginRunnable);
            if (resp.status == null || !resp.status) {
                mMainHandler.postDelayed(mReloginRunnable, CapConfig.DURATION_RELOGIN_SERVER);
                return;
            }
            login(resp);
        } else if(CapConstants.RES_CMD_CA_REPORT_LOCATION.equals(resp.cmd)) {
            CLog.d(TAG, "onResponse = [ " + resp.status + ", " + resp.msg + " ]");
            mMainHandler.removeCallbacks(mDisconnectRunnable);
//            mLastReceiveTime = System.currentTimeMillis();
        }
    }

    private void login(CapInfoResponse resp) {
        saveLoginInfo(resp.data);
        requestUserSig(resp.data);
        startLocationTimer();
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mLocationMgr.start();
            }
        });
    }

    private void logout() {
        mMainHandler.removeCallbacks(mDisconnectRunnable);
        mMainHandler.removeCallbacks(mReloginRunnable);
        stopLocationTimer();
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
        mMainHandler.removeCallbacks(mReconnectRunnable);
        sendLoginMsg();
    }

    @Override
    public void notifyUnconnect() {
        reconnectDelayed();
    }

    @Override
    public void notifyDisconnected() {
        CLog.d(TAG, "notifyDisconnected");
        logout();
        reconnectDelayed();
    }

    private void reconnectDelayed() {
        if (CapUtils.isNetworkAvailable(mActivity)) {
            mMainHandler.postDelayed(mReconnectRunnable, CapConfig.DURATION_RECONNECT_SERVER);
        }
    }

    private void sendLoginMsg() {
        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLoginReqMsg());
        mMainHandler.postDelayed(mDisconnectRunnable, CapConfig.DURATION_RESPONSE_MSG);
    }

    private void sendLocationMsg() {
        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLocationReqMsg());
        mMainHandler.postDelayed(mDisconnectRunnable, CapConfig.DURATION_RESPONSE_MSG);
    }

    private void saveLoginInfo(CapUser userInfo) {
        CLog.d(TAG, "saveLoginInfo = [ " + userInfo + " ]");
        if (userInfo == null) {
            return;
        }
        CapSharedPrefMgr.getInstance().putUserID(userInfo.user_id);
        CapSharedPrefMgr.getInstance().putUserName(userInfo.user_name);
        CapSharedPrefMgr.getInstance().putUserImg(userInfo.user_img);
    }

    private void requestUserSig(CapUser userInfo) {
        CLog.d(TAG, "requestUserSig = [ " + userInfo + " ]");
        if (userInfo == null) {
            return;
        }
        final Request request = new Request.Builder()
                .url(CapConfig.URL_REQ_USER_SIG + userInfo.user_id)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                CLog.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(final Call call, final okhttp3.Response response) throws IOException {
                String json = response.body().string();
                CLog.d(TAG, "onResponse = " + json);
                CapInfoResponse resp = new Gson().fromJson(json, CapInfoResponse.class);
                CapSharedPrefMgr.getInstance().putUserSig(resp.userSig);
            }
        });
    }
}
