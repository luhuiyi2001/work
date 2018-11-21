package com.tencent.liteav.demo.cap;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.fragment.CapChatFragment;
import com.tencent.liteav.demo.cap.fragment.CapPusherFragment;
import com.tencent.liteav.demo.cap.fragment.CapRecorderFragment;
import com.tencent.liteav.demo.cap.home.AllAppActivity;
import com.tencent.liteav.demo.cap.impl.CapLivePusherImpl;
import com.tencent.liteav.demo.cap.impl.CapRTCRoomImpl;
import com.tencent.liteav.demo.cap.inter.CapActivityInterface;
import com.tencent.liteav.demo.cap.inter.CapGetLocationInterface;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.cap.receiver.CapNetWorkStateReceiver;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.common.misc.CommonAppCompatActivity;
import com.tencent.liteav.demo.common.misc.NameGenerator;
import com.tencent.liteav.demo.rtcroom.RTCRoom;
import com.tencent.liteav.demo.rtcroom.ui.multi_room.fragment.RTCMultiRoomChatFragment;

public class CapActivity extends CommonAppCompatActivity implements CapActivityInterface,CapGetLocationInterface {

    private static final String TAG = CapActivity.class.getSimpleName();

    public  Handler         uiHandler  = new Handler();
    private Handler         mMainHandler;
    private TextView        titleTextView;
    private TextView        globalLogTextview;
    private ScrollView      globalLogTextviewContainer;
    private OnResponseCallback mPushRtmpRespCallback;

    private CapRTCRoomImpl mRTCRoomImpl;
    private CapNetWorkStateReceiver mNetStateReceiver = new CapNetWorkStateReceiver();

    private LocationManager mLocationManager;
    private Location mLocation;

    // 定义一个LocationListener来响应定位更新
    LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            CLog.d(TAG, "onStatusChanged = [ " + location.getLatitude() + ", " + location.getLongitude() + " ]");
            // 当地理位置信息有变化的时候回调
            makeUseOfNewLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            CLog.d(TAG, "onStatusChanged = [ " + provider + " ]");
        }

        public void onProviderEnabled(String provider) {
            CLog.d(TAG, "onProviderEnabled = [ " + provider + " ]");
        }

        public void onProviderDisabled(String provider) {
            CLog.d(TAG, "onProviderDisabled = [ " + provider + " ]");
        }
    };

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

        mMainHandler = new Handler(Looper.getMainLooper());

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
        CapClientManager.getInstance().setLocationGetInterface(this);
        registerIntentReceivers();

        // 获取系统的LocationManager服务
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // 向Location Manager注册LocationListener监听定位更新
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mLivePusherImpl.resume();
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecorder();
            }
        }, 1000);
        if (CapUtils.isNetworkAvailable(this)) {
            CapClientManager.getInstance().onStart();
        }
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
        unregisterIntentReceivers();
        mLocationManager.removeUpdates(mLocationListener);
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

    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetStateReceiver, filter);
    }

    private void unregisterIntentReceivers() {
        unregisterReceiver(mNetStateReceiver);
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


    private void makeUseOfNewLocation(Location location) {
        mLocation = location;
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
            ((Button)v).setText("StartRecorder");
        } else {
            startRecorder();
//            CapClientManager.getInstance().onStart();
            ((Button)v).setText("StopRecorder");
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
            startPusher("rtmp://aqm.runde.pro:1935/live/36147_" + CapConfig.TEST_USER_ID);
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

    @Override
    public Location getLocation() {
        if (mLocation == null) {
            return mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return mLocation;
    }
}
