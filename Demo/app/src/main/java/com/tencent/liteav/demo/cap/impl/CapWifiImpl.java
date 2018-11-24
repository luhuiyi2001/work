package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.tencent.liteav.demo.cap.CapActivity;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.socket.WifiInfo;
import com.tencent.liteav.demo.cap.wifi.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

public class CapWifiImpl implements CapSocketManager.OnResponseCallback{
    private static final String TAG = CapWifiImpl.class.getSimpleName();

    private Activity mActivity;
    private WifiAdmin mWifiMgr;
    public CapWifiImpl(Activity context) {
        mActivity = context;
        mWifiMgr = new WifiAdmin(context);
    }

    public void create() {
        registerIntentReceivers();
    }

    public void destroy() {
        unregisterIntentReceivers();
    }

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CLog.d(TAG, "onReceive : " + action);
            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
//
                String ssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
                CLog.e(TAG, "ssid : " + ssid);
                SupplicantState state = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                setSupplicantState(state);

                boolean isAuthError = intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR);
                if (isAuthError) {
                    CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getWifiConnStateReqMsg(ssid, false));
                }
//                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
//                CLog.e(TAG, "linkWifiResult : " + linkWifiResult);
//                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
//                    CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getWifiConnStateReqMsg());
//                }
            }
        }
    };

    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mActivity.registerReceiver(mWifiReceiver, filter);
    }

    private void unregisterIntentReceivers() {
        mActivity.unregisterReceiver(mWifiReceiver);
    }

    @Override
    public void onResponse(CapInfoResponse resp) {
        if (CapConstants.RES_CMD_SERVER_PULL_WIFI_LIST.equals(resp.cmd)) {
            CLog.d(TAG, "onPullWifiList");
            CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getWifiListReqMsg(getWifiList()));
        } else if (CapConstants.RES_CMD_SERVER_PUSH_CONNECT_WIFI.equals(resp.cmd)) {
            CLog.d(TAG, "onConnWifi = [ " + resp.spot + ", " + resp.pwd + " ]");
            mWifiMgr.openWifi();
            mWifiMgr.addNetwork(mWifiMgr.CreateWifiInfo(resp.spot, resp.pwd, 3));
            mWifiMgr.saveConfiguration();
        }
    }

    private List<WifiInfo> getWifiList() {
        mWifiMgr.openWifi();
        mWifiMgr.startScan();
        CLog.i(TAG, "getWifiList = " + mWifiMgr.lookUpScan().toString());
        List<ScanResult> scanWifiList = mWifiMgr.getWifiList();
        if (scanWifiList.size() == 0) {
            return null;
        }
        List<WifiInfo> wifiList = new ArrayList<WifiInfo>();
        for (int i = 0; i < scanWifiList.size(); i++) {
            ScanResult curResult = scanWifiList.get(i);
            WifiInfo wifiInfo = new WifiInfo();
            wifiInfo.spot = curResult.SSID;
            wifiInfo.pwd = curResult.capabilities.contains("WPA") ? 1 : 0;
            wifiInfo.intensity = WifiManager.calculateSignalLevel(curResult.level, 5);
            wifiInfo.status = mWifiMgr.isExsits(curResult.SSID) != null ? 1 : 0;
            wifiList.add(wifiInfo);
        }
        return wifiList;
    }

    private void setSupplicantState(SupplicantState supplicantState) {
        if (SupplicantState.FOUR_WAY_HANDSHAKE.equals(supplicantState)) {
            CLog.d(TAG, "FOUR WAY HANDSHAKE");
        } else if (SupplicantState.ASSOCIATED.equals(supplicantState)) {
            CLog.d(TAG, "ASSOCIATED");
        } else if (SupplicantState.ASSOCIATING.equals(supplicantState)) {
            CLog.d(TAG, "ASSOCIATING");
        } else if (SupplicantState.COMPLETED.equals(supplicantState)) {
            CLog.d(TAG, "COMPLETED");
        } else if (SupplicantState.DISCONNECTED.equals(supplicantState)) {
            CLog.d(TAG, "DISCONNECTED");
        } else if (SupplicantState.DORMANT.equals(supplicantState)) {
            CLog.d(TAG, "DORMANT");
        } else if (SupplicantState.GROUP_HANDSHAKE.equals(supplicantState)) {
            CLog.d(TAG, "GROUP HANDSHAKE");
        } else if (SupplicantState.INACTIVE.equals(supplicantState)) {
            CLog.d(TAG, "INACTIVE");
        } else if (SupplicantState.INVALID.equals(supplicantState)) {
            CLog.d(TAG, "INVALID");
        } else if (SupplicantState.SCANNING.equals(supplicantState)) {
            CLog.d(TAG, "SCANNING");
        } else if (SupplicantState.UNINITIALIZED.equals(supplicantState)) {
            CLog.d(TAG, "UNINITIALIZED");
        } else {
            CLog.e(TAG, "supplicant state is bad");
        }
    }
}
