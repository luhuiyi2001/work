package com.tencent.liteav.demo.cap;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.liteav.demo.DemoApplication;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.callback.OnResponseCallback;
import com.tencent.liteav.demo.cap.callback.PushRtmpRespCallback;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.fragment.CapChatFragment;
import com.tencent.liteav.demo.cap.fragment.CapPusherFragment;
import com.tencent.liteav.demo.cap.fragment.CapRecorderFragment;
import com.tencent.liteav.demo.cap.home.AllAppActivity;
import com.tencent.liteav.demo.cap.impl.CapLivePusherImpl;
import com.tencent.liteav.demo.cap.impl.CapRTCRoomImpl;
import com.tencent.liteav.demo.cap.inter.CapActivityInterface;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.common.misc.CommonAppCompatActivity;
import com.tencent.liteav.demo.common.misc.NameGenerator;
import com.tencent.liteav.demo.rtcroom.RTCRoom;
import com.tencent.liteav.demo.rtcroom.ui.multi_room.fragment.RTCMultiRoomChatFragment;

public class CapActivity extends CommonAppCompatActivity implements CapActivityInterface {

    private static final String TAG = CapActivity.class.getSimpleName();

    public  Handler         uiHandler  = new Handler();
    
    private TextView        titleTextView;
    private TextView        globalLogTextview;
    private ScrollView      globalLogTextviewContainer;
    private OnResponseCallback mPushRtmpRespCallback;

    private CapRTCRoomImpl mRTCRoomImpl;
//    private CapLivePusherImpl mLivePusherImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_cap);

        titleTextView = ((TextView) findViewById(R.id.rtc_mutil_room_title_textview));

        globalLogTextview = ((TextView) findViewById(R.id.rtc_multi_room_global_log_textview));
        globalLogTextview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                 globalLogTextview.setText("");
                 return true;
            }
        });

        globalLogTextviewContainer = ((ScrollView) findViewById(R.id.rtc_mutil_room_global_log_container));

        mRTCRoomImpl = new CapRTCRoomImpl(this);
        mRTCRoomImpl.create();
//        mLivePusherImpl = new CapLivePusherImpl(this);
//        mLivePusherImpl.initView();
        mPushRtmpRespCallback = new OnResponseCallback(){

            @Override
            public void onResponse(CapInfoResponse resp) {
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
                    mRTCRoomImpl.onStartChat(resp.room_id);
                }
            }
        };
        CapClientManager.getInstance().addOnResponseCallback(mPushRtmpRespCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mLivePusherImpl.resume();
    }

    @Override
    public void onStop(){
        super.onStop();
//        mLivePusherImpl.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRTCRoomImpl.destroy();
        CapClientManager.getInstance().removeOnResponseCallback(mPushRtmpRespCallback);
//        mLivePusherImpl.destroy();
    }

    @Override
    public void onBackPressed() {
//        Fragment fragment = getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
//        if (fragment instanceof RTCMultiRoomChatFragment){
//            ((RTCMultiRoomChatFragment) fragment).onBackPressed();
//        }
//        else {
            super.onBackPressed();
//        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
//    }

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

    private void startPusher(String url) {
        if (TextUtils.isEmpty(url) || (!url.trim().toLowerCase().startsWith("rtmp://"))) {
            CLog.e(TAG, "推流地址不合法，目前支持rtmp推流!");
            return;
        }
        //removeCurFragment();
        CapPusherFragment pusherFragment = CapPusherFragment.newInstance(url);
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ts = fm.beginTransaction();
        ts.replace(R.id.rtmproom_fragment_container, pusherFragment);
//        ts.addToBackStack(null);
        ts.commit();
    }

    private void stopPusher() {
        //removeCurFragment();
        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapPusherFragment && fragment.isVisible()){
            ((CapPusherFragment) fragment).onBackPressed();
        }
    }

    private void startRecorder() {
        CapRecorderFragment pusherFragment = CapRecorderFragment.newInstance();
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ts = fm.beginTransaction();
        ts.replace(R.id.rtmproom_fragment_container, pusherFragment);
//        ts.addToBackStack(null);
        ts.commit();
    }

    private void stopRecorder() {
        //removeCurFragment();
        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapRecorderFragment && fragment.isVisible()){
            ((CapRecorderFragment) fragment).onBackPressed();
        }
    }

//    public boolean removeCurFragment() {
//        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
//        if (fragment == null) {
//            return false;
//        }
//
//        FragmentManager fm = this.getFragmentManager();
//        FragmentTransaction ts = fm.beginTransaction();
//        ts.remove(fragment);
//        ts.commit();
//
//        return true;
//    }


    private boolean isBtn1Start = false;
    public void onClickBtn1(View v) {
        CLog.d(TAG, "onClickBtn1");
        //mReceiveEventImpl.onStopRTSP(null);
        //mLivePusherImpl.doPlay("rtmp://aqm.runde.pro:1935/live/36147_1");
        if (isBtn1Start) {
            stopRecorder();
//            CapClientManager.getInstance().onStop();
            ((Button)v).setText("StartSocket");
        } else {
            startRecorder();
//            CapClientManager.getInstance().onStart();
            ((Button)v).setText("StopSocket");
        }
        isBtn1Start = !isBtn1Start;
    }

    private boolean isBtn2Start = false;
    public void onClickBtn2(View v) {
        CLog.d(TAG, "onClickBtn2");
        //CapInfoManager.getInstance().getWifiListReqMsg(this);
        //startService(new Intent(Home.this, CapRecordSerivice.class));
        if (isBtn2Start) {
            stopPusher();
//            this.mRecorderImpl.stopRecord();
            ((Button)v).setText("StartPusher");
        } else {
            startPusher("rtmp://aqm.runde.pro:1935/live/36147_1000");
//            this.mRecorderImpl.startRecord();
            ((Button)v).setText("StopPusher");
        }
        isBtn2Start = !isBtn2Start;
    }

    private boolean isBtn3Start = false;
    public void onClickBtn3(View v) {
        CLog.d(TAG, "onClickBtn3");
        mRTCRoomImpl.onStopChat();
        if (isBtn3Start) {
            mRTCRoomImpl.onStopChat();
            ((Button)v).setText("StartRTCRoom");
        } else {
            mRTCRoomImpl.onStartChat(null);
            ((Button)v).setText("StopRTCRoom");
        }
        isBtn3Start = !isBtn3Start;
    }

    public void onShowApps(View v) {
        CLog.d(TAG, "onClickBtn4");
        //CapClientManager.getInstance().stopConnection();
        Intent intent = new Intent(this, AllAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
