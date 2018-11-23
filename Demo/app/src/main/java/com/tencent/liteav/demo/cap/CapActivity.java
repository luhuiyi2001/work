package com.tencent.liteav.demo.cap;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.TextView;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.callback.OnImeiCallback;
import com.tencent.liteav.demo.cap.callback.OnSocketCallback;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.fragment.CapPusherFragment;
import com.tencent.liteav.demo.cap.fragment.CapRecorderFragment;
import com.tencent.liteav.demo.cap.manager.CapImeiMonitor;
import com.tencent.liteav.demo.cap.impl.CapLoginServerImpl;
import com.tencent.liteav.demo.cap.impl.CapNetWorkImpl;
import com.tencent.liteav.demo.cap.impl.CapRTCRoomImpl;
import com.tencent.liteav.demo.cap.impl.CapTestUIImpl;
import com.tencent.liteav.demo.cap.inter.CapActivityInterface;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.wifi.WifiAdmin;
import com.tencent.liteav.demo.common.misc.CommonAppCompatActivity;
import com.tencent.liteav.demo.common.misc.NameGenerator;
import com.tencent.liteav.demo.rtcroom.RTCRoom;

public class CapActivity extends CommonAppCompatActivity implements CapActivityInterface {

    private static final String TAG = CapActivity.class.getSimpleName();

    public  Handler         uiHandler  = new Handler();
    private Handler         mMainHandler;

    private CapRTCRoomImpl mRTCRoomImpl;
    private CapLoginServerImpl mLoginServerImpl;
    private CapNetWorkImpl mNetWorkImpl;
    private CapImeiMonitor mImeiMonitor;
    private OnImeiCallback mImeiCallback = new OnImeiCallback() {
        @Override
        public void notifyDataChanged() {
            CapSocketManager.getInstance().connect();
        }
    };

    private TextView        titleTextView;

    final Runnable mRecorderTimeout = new Runnable() {
        @Override public void run() {
            startRecorder();
        }
    };
    private CapTestUIImpl mTestUIImpl;

    private OnSocketCallback mPushRtmpRespCallback = new OnSocketCallback(){

        @Override
        public void onResponse(CapInfoResponse resp) {
            CLog.d(TAG, "onResponse");
            if (resp == null) {
                CLog.d(TAG, "resp == null || mContext == null");
                return;
            }
            if (CapConstants.RES_CMD_SERVER_PUSH_OPEN_RTSP.equals(resp.cmd)) {
                CLog.d(TAG, "onOpenRTSP = [ " + resp.push_url + " ]");
                startPusher(resp.push_url);
            } else if (CapConstants.RES_CMD_SERVER_PUSH_STOP_RTSP.equals(resp.cmd)) {
                CLog.d(TAG, "onStopRTSP");
                stopPusher();
            } else if (CapConstants.RES_CMD_SERVER_PUSH_OPEN_VIDEO_CALL.equals(resp.cmd)) {
                CLog.d(TAG, "onOpenVideo");
                mRTCRoomImpl.onStartChat(resp.room_id, null);
            } else if (CapConstants.RES_CMD_SERVER_PULL_WIFI_LIST.equals(resp.cmd)) {
                CLog.d(TAG, "onPullWifiList");
                CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getWifiListReqMsg(CapActivity.this));
            } else if (CapConstants.RES_CMD_SERVER_PUSH_CONNECT_WIFI.equals(resp.cmd)) {
                CLog.d(TAG, "onConnWifi = [ " + resp.spot + ", " + resp.pwd + " ]");
                WifiAdmin wifiAdmin = new WifiAdmin(CapActivity.this);
                wifiAdmin.openWifi();
                wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(resp.spot, resp.pwd, 3));
                wifiAdmin.saveConfiguration();
            } else if (CapConstants.RES_CMD_SERVER_PUSH_APP_ASK_FOR_HELP.equals(resp.cmd)) {
                CLog.d(TAG, "onCreateRoom : " + resp.user_ids);
                mRTCRoomImpl.onStartChat(null, resp.user_ids);
            }
        }

        @Override
        public void notifyConnected() {

        }

        @Override
        public void notifyDisconnected() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_cap);

        mTestUIImpl = new CapTestUIImpl(this);
        mTestUIImpl.create();

        titleTextView = ((TextView) findViewById(R.id.rtc_mutil_room_title_textview));

        mMainHandler = new Handler(Looper.getMainLooper());

        mRTCRoomImpl = new CapRTCRoomImpl(this);
        mRTCRoomImpl.create();

        CapSocketManager.getInstance().addOnSocketCallback(mPushRtmpRespCallback);

        // 获取系统的LocationManager服务
        mLoginServerImpl = new CapLoginServerImpl(this, mMainHandler);
        CapSocketManager.getInstance().addOnSocketCallback(mLoginServerImpl);

        mNetWorkImpl = new CapNetWorkImpl(this);
        mNetWorkImpl.create();

        mImeiMonitor = new CapImeiMonitor();
        mImeiMonitor.setOnImeiCallback(mImeiCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecorder();
            }
        }, 1000);
        mImeiMonitor.start();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRTCRoomImpl.destroy();
        CapSocketManager.getInstance().removeOnSocketCallback(mPushRtmpRespCallback);
        mNetWorkImpl.destroy();
        mTestUIImpl.destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPermissionDisable() {
        new AlertDialog.Builder(this, R.style.RtmpRoomDialogTheme)
                .setMessage("需要录音和摄像头权限，请到【设置】【应用】打开")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    @Override
    public void onPermissionGranted() {

    }

    @Override
    public RTCRoom getRTCRoom() {
        return mRTCRoomImpl.getRTCRoom();
    }

    @Override
    public void setTitle(final String s) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String ss = NameGenerator.replaceNonPrintChar(s, 20, "...", false);
                titleTextView.setLinksClickable(false);
                titleTextView.setText(ss);
            }
        });
    }

    @Override
    public void backToStartRecord() {
        this.startRecorder();
    }

    public void startPusher(String url) {
        if (TextUtils.isEmpty(url) || (!url.trim().toLowerCase().startsWith("rtmp://"))) {
            CLog.e(TAG, "推流地址不合法，目前支持rtmp推流!");
            return;
        }
        CapPusherFragment pusherFragment = CapPusherFragment.newInstance(url);
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ts = fm.beginTransaction();
        ts.replace(R.id.rtmproom_fragment_container, pusherFragment);
//        ts.addToBackStack(null);
        ts.commit();
    }

    public void stopPusher() {
        //removeCurFragment();
        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapPusherFragment && fragment.isVisible()){
            ((CapPusherFragment) fragment).onBackPressed();
        }
    }

    public void startRecorder() {
        if (!CapUtils.checkExtSdcard()) {
            CLog.e(TAG, CapConfig.PATH_EXT_SDCARD + " isn't exist");
            mMainHandler.removeCallbacks(mRecorderTimeout);
            mMainHandler.postDelayed(mRecorderTimeout, 10000);
            return;
        }
        mMainHandler.removeCallbacks(mRecorderTimeout);
        CapRecorderFragment pusherFragment = CapRecorderFragment.newInstance();
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ts = fm.beginTransaction();
        ts.replace(R.id.rtmproom_fragment_container, pusherFragment);
//        ts.addToBackStack(null);
        ts.commit();
    }

    public void stopRecorder() {
        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapRecorderFragment && fragment.isVisible()){
            ((CapRecorderFragment) fragment).onBackPressed();
        }
    }

    public void startChat() {
        mRTCRoomImpl.onStartChat(null, null);
    }

    public void stopChat() {
        mRTCRoomImpl.onStopChat();
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }
}
