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
import android.view.KeyEvent;
import android.widget.TextView;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.callback.CapActivityInterface;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapTimer;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.fragment.CapChatFragment;
import com.tencent.liteav.demo.cap.fragment.CapPusherFragment;
import com.tencent.liteav.demo.cap.fragment.CapRecorderFragment;
import com.tencent.liteav.demo.cap.impl.CapDebugImpl;
import com.tencent.liteav.demo.cap.impl.CapFragmentImpl;
import com.tencent.liteav.demo.cap.impl.CapLoginServerImpl;
import com.tencent.liteav.demo.cap.impl.CapNetWorkImpl;
import com.tencent.liteav.demo.cap.impl.CapProxSensorImpl;
import com.tencent.liteav.demo.cap.impl.CapRTCRoomImpl;
import com.tencent.liteav.demo.cap.impl.CapRecorderImpl;
import com.tencent.liteav.demo.cap.impl.CapTestUIImpl;
import com.tencent.liteav.demo.cap.impl.CapWifiImpl;
import com.tencent.liteav.demo.cap.manager.CapAudioManager;
import com.tencent.liteav.demo.cap.manager.CapExtSdcardManager;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.socket.CapResponse;
import com.tencent.liteav.demo.cap.websocket.AbsWebSocketActivity;
import com.tencent.liteav.demo.cap.websocket.ErrorResponse;
import com.tencent.liteav.demo.cap.websocket.Response;
import com.tencent.liteav.demo.common.misc.CommonAppCompatActivity;
import com.tencent.liteav.demo.common.misc.NameGenerator;
import com.tencent.liteav.demo.rtcroom.RTCRoom;

import java.util.ArrayList;

public class CapActivity extends AbsWebSocketActivity implements CapActivityInterface,CapExtSdcardManager.OnMountedListener {

    private static final String TAG = CapActivity.class.getSimpleName();

    public  Handler         uiHandler  = new Handler();
    private Handler         mMainHandler;

    private CapRTCRoomImpl mRTCRoomImpl;
    private CapLoginServerImpl mLoginServerImpl;
    private CapNetWorkImpl mNetWorkImpl;

    private TextView        titleTextView;

    private CapTestUIImpl mTestUIImpl;
    private CapWifiImpl mWifiImpl;
    private CapFragmentImpl mFragmentImpl;
    private CapProxSensorImpl mProxSensorImpl;
    private CapDebugImpl mDebugImpl;

    private CapSocketManager.OnResponseCallback mPushRtmpRespCallback = new CapSocketManager.OnResponseCallback(){

        @Override
        public void onResponse(CapResponse resp) {
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
                startChat(resp.room_id, null, false);
            } else if (CapConstants.RES_CMD_SERVER_PUSH_APP_ASK_FOR_HELP.equals(resp.cmd)) {
                CLog.d(TAG, "onCreateRoom : " + resp.user_ids);
                startChat(null, resp.user_ids, false);
            }
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

        CapSocketManager.getInstance().addOnResponseCallback(mPushRtmpRespCallback);

        // 获取系统的LocationManager服务
        mLoginServerImpl = new CapLoginServerImpl(this, mMainHandler);
        mWifiImpl = new CapWifiImpl(this);
        CapSocketManager.getInstance().addOnResponseCallback(mWifiImpl);
        CapSocketManager.getInstance().addOnResponseCallback(mLoginServerImpl);
        CapSocketManager.getInstance().addOnConnectStateCallback(mLoginServerImpl);

        mNetWorkImpl = new CapNetWorkImpl(this);
        mNetWorkImpl.create();
        mFragmentImpl = new CapFragmentImpl(this);
        mProxSensorImpl = new CapProxSensorImpl(this);
        mProxSensorImpl.create();
        mDebugImpl = new CapDebugImpl(this);
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
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProxSensorImpl.destroy();
        mRTCRoomImpl.destroy();
        CapSocketManager.getInstance().removeOnResponseCallback(mWifiImpl);
        CapSocketManager.getInstance().removeOnResponseCallback(mPushRtmpRespCallback);
        CapSocketManager.getInstance().removeOnResponseCallback(mLoginServerImpl);
        CapSocketManager.getInstance().removeOnConnectStateCallback(mLoginServerImpl);
        mNetWorkImpl.destroy();
        mTestUIImpl.destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CLog.d(TAG, "onKeyDown = " + keyCode);
        mDebugImpl.keyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_F10) {
            doSos();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_F11) {
            doVideoShot();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_F12) {
            doChat();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            CapAudioManager.getInstance().playVolumnKeyVoice();
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        CLog.d(TAG, "onKeyUp  = " + keyCode);
        mDebugImpl.keyUp(keyCode, event);
        return super.onKeyUp(keyCode, event);
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
        startRecorder();
    }
    public void startPusher(String url) {
        if (TextUtils.isEmpty(url) || (!url.trim().toLowerCase().startsWith("rtmp://"))) {
            CLog.e(TAG, "推流地址不合法，目前支持rtmp推流!");
            return;
        }

        if (mFragmentImpl.isChatUI()) {
            CLog.e(TAG, "正在进行视频通话!");
            return;
        }
        replaceFragement(CapPusherFragment.newInstance(url));
    }

    public void stopPusher() {
        //removeCurFragment();
        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapPusherFragment && fragment.isVisible()){
            ((CapPusherFragment) fragment).onBackPressed();
        }
    }

    public void startRecorder() {
        if (!CapExtSdcardManager.getInstance().isMounted()) {
            CapExtSdcardManager.getInstance().setOnMountedListener(this);
            return;
        }
        replaceFragement(CapRecorderFragment.newInstance());
    }

    public void replaceFragement(Fragment fragment) {
        mFragmentImpl.replaceFragement(fragment);
    }

    public void stopRecorder() {
        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapRecorderFragment && fragment.isVisible()){
            ((CapRecorderFragment) fragment).onBackPressed();
        }
    }

    public void startChat(String roomId, ArrayList<String> userIDs, boolean isBtnCall) {
        if (mFragmentImpl.isChatUI()) {
            CLog.e(TAG, "正在进行视频通话!");
            return;
        }
        mRTCRoomImpl.onStartChat(roomId, userIDs, isBtnCall);
    }

    public void stopChat() {
        mRTCRoomImpl.onStopChat();
    }

    public void doSos() {
        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getSosReqMsg());
        CapAudioManager.getInstance().playSos();
    }

    public void doVideoShot() {
        CapAudioManager.getInstance().playVideoShotVoice();
        Fragment fragment = this.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapRecorderFragment && fragment.isVisible()){
            ((CapRecorderFragment) fragment).takePicture();
        }
    }

    public void doChat() {
        startChat(null, null, true);
        CapAudioManager.getInstance().playWaitReceiveVoice();
    }

    @Override
    public void onMounted() {
        startRecorder();
    }

    @Override
    public void onMessageResponse(Response message) {
        CLog.d(TAG, "onMessageResponse");
    }

    @Override
    public void onSendMessageError(ErrorResponse error) {
        CLog.e(TAG, "onSendMessageError");
    }
}
