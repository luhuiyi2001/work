package com.tencent.liteav.demo.cap.listener;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.home.Home;
import com.tencent.liteav.demo.cap.listener.OnReceiveEventListener;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.wifi.WifiAdmin;
import com.tencent.liteav.demo.push.LivePublisherActivity;

public class CapReceiveEventImpl implements OnReceiveEventListener {
    private static final String TAG = "CapReceiveEventImpl";
    private Context mContext;
    public CapReceiveEventImpl(Context context) {
        mContext = context;
    }

    @Override
    public void onLogin(CapInfoResponse resp) {
        CLog.d(TAG, "onLogin = [ " + resp.status + ", " + resp.msg + ", " + resp.data + " ]");
        if (resp.status == null || !resp.status) {
            return;
        }
        CapClientManager.getInstance().startHeartbeatTimer();
        //TODO do login info
    }

    @Override
    public void onSOS(CapInfoResponse resp) {
        CLog.d(TAG, "onSOS = [ " + resp.status + ", " + resp.msg + " ]");
    }

    @Override
    public void onLocation(CapInfoResponse resp) {
        CLog.d(TAG, "onLocation = [ " + resp.status + ", " + resp.msg + " ]");
    }

    @Override
    public void onOpenRTSP(CapInfoResponse resp) {
        CLog.d(TAG, "onOpenRTSP = [ " + resp.push_url + " ]");
//        Intent intent = new Intent(mContext, LivePublisherActivity.class);
//        intent.putExtra("TITLE", LivePublisherActivity.class.getName());
//        intent.putExtra("PUSH_URL", resp.push_url);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mContext.startActivity(intent);
        Intent intent = new Intent(CapConstants.ACTION_START_PUBLISH);
        intent.putExtra(CapConstants.EXTRA_PUSH_URL, resp.push_url);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void onStopRTSP(CapInfoResponse resp) {
        CLog.d(TAG, "onStopRTSP");
//        Intent intent = new Intent(mContext, Home.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mContext.startActivity(intent);
        Intent intent = new Intent(CapConstants.ACTION_STOP_PUBLISH);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void onPullWifiList(CapInfoResponse resp) {
        CLog.d(TAG, "onPullWifiList");
//        CapClientManager.getInstance().onSend(CapInfoManager.getInstance().getWifiListReqMsg(mContext));
    }

    @Override
    public void onConnWifi(CapInfoResponse resp) {
        CLog.d(TAG, "onConnWifi = [ " + resp.spot + ", " + resp.pwd + " ]");
        WifiAdmin wifiAdmin = new WifiAdmin(mContext);
        wifiAdmin.openWifi();
        wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(resp.spot, resp.pwd, 3));
        wifiAdmin.saveConfiguration();
    }
}
