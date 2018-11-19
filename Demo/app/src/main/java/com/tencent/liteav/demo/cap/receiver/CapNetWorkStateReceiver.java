package com.tencent.liteav.demo.cap.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.service.CapClientService;
import com.tencent.liteav.demo.cap.common.CLog;

public class CapNetWorkStateReceiver extends BroadcastReceiver {
    private static final String TAG = CapNetWorkStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean networkChange = false;
        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                networkChange = true;
            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                networkChange = true;
            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                networkChange = true;
            }
            //API大于23时使用下面的方式进行网络监听
        } else {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();

            //通过循环将网络信息逐个取出来
            for (int i = 0; i < networks.length; i++) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
                if (networkInfo.isConnected()) {
                    networkChange = true;
                }
            }
        }
        CLog.i(TAG, "networkChange = " + networkChange);
        if (networkChange) {
            startClientService(context,CapConstants.CMD_START);
        } else {
            startClientService(context, CapConstants.CMD_STOP);
        }
    }

    public void startClientService(Context context, String cmd) {
        Intent clientServiceIntent = new Intent(context, CapClientService.class);
        clientServiceIntent.putExtra(CapConstants.EXTRA_CMD_CLIENT, cmd);
        context.startService(clientServiceIntent);
    }
}
