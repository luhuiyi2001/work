package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.socket.CapResponse;
import com.tencent.liteav.demo.cap.manager.CapWifiManager;
import com.tencent.liteav.demo.cap.socket.CapWifi;

import java.util.ArrayList;
import java.util.List;

public class CapWifiImpl implements CapSocketManager.OnResponseCallback{
    private static final String TAG = CapWifiImpl.class.getSimpleName();

    private Activity mActivity;
    private CapWifiManager mWifiMgr;
    public CapWifiImpl(Activity context) {
        mActivity = context;
        mWifiMgr = new CapWifiManager(context);
    }
/**
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
*/
    @Override
    public void onResponse(CapResponse resp) {
        if (CapConstants.RES_CMD_SERVER_PULL_WIFI_LIST.equals(resp.cmd)) {
            CLog.d(TAG, "onPullWifiList");
            CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getWifiListReqMsg(getWifiList()));
        } else if (CapConstants.RES_CMD_SERVER_PUSH_CONNECT_WIFI.equals(resp.cmd)) {
            if (resp.data == null) {
                CLog.d(TAG, "onConnWifi : null");
                return;
            }

            if (!CapConstants.ACT_KEY_APP_SET_WIFI.equals(resp.data.act)) {
                return;
            }
            int wifiEnabled = resp.data.wifi_enabled;
            if (wifiEnabled == 0) {
                mWifiMgr.closeWifi();
            } else if (wifiEnabled == 1) {
                String ssid = resp.data.spot;
                String pwd = resp.data.pwd;
                CLog.d(TAG, "onConnWifi = [ " + ssid + ", " + pwd + " ]");
                mWifiMgr.openWifi();
                boolean isSuccess = mWifiMgr.addNetwork(mWifiMgr.CreateWifiInfo(ssid, pwd, 3));
                if (isSuccess) {
                    mWifiMgr.saveConfiguration();
                }
                CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getWifiConnStateReqMsg(ssid, isSuccess));
            }
        }
    }

    private List<CapWifi> getWifiList() {
        mWifiMgr.openWifi();
        mWifiMgr.startScan();
        CLog.i(TAG, "getWifiList = " + mWifiMgr.lookUpScan().toString());
        List<ScanResult> scanWifiList = mWifiMgr.getWifiList();
        if (scanWifiList.size() == 0) {
            return null;
        }
        String curSSID = mWifiMgr.getCurSSID().replace("\"","");
        CLog.i(TAG, "curSSID = " + curSSID);
        List<CapWifi> wifiList = new ArrayList<CapWifi>();
        for (int i = 0; i < scanWifiList.size(); i++) {
            ScanResult curResult = scanWifiList.get(i);
            CapWifi wifiInfo = new CapWifi();
            wifiInfo.spot = curResult.SSID;
            wifiInfo.pwd = curResult.capabilities.contains("WPA") ? 1 : 0;
            wifiInfo.intensity = WifiManager.calculateSignalLevel(curResult.level, 5);
            wifiInfo.status = curResult.SSID.equals(curSSID) ? 1 : 0;
            wifiList.add(wifiInfo);
        }
        return wifiList;
    }
/**
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
    */
}
