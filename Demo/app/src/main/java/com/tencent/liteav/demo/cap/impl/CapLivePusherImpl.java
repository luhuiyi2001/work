package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.home.Home;
import com.tencent.liteav.demo.cap.listener.OnReceiveEventListener;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.wifi.WifiAdmin;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.ref.WeakReference;

public class CapLivePusherImpl implements ITXLivePushListener {
    private static final String TAG = CapLivePusherImpl.class.getSimpleName();
    private static final int VIDEO_SRC_CAMERA = 0;
    private static final int VIDEO_SRC_SCREEN = 1;

    private TXLivePushConfig mLivePushConfig;
    private TXLivePusher mLivePusher;
    private TXCloudVideoView mCaptureView;
    private boolean          mVideoPublish;
    private int              mVideoSrc = VIDEO_SRC_CAMERA;
    private boolean          mHWVideoEncode = true;
    private boolean          mFrontCamera = true;

    private Activity mActivity;
    public CapLivePusherImpl(Activity context) {
        mActivity = context;
}

    public void initView() {
        mLivePusher     = new TXLivePusher(mActivity);
        mLivePushConfig = new TXLivePushConfig();
        mLivePushConfig.setVideoEncodeGop(5);
//		mLivePushConfig.setBeautyFilter(mBeautyLevel, mWhiteningLevel, mRuddyLevel);
        mLivePusher.setConfig(mLivePushConfig);
        mVideoPublish = false;

        mCaptureView = (TXCloudVideoView) mActivity.findViewById(R.id.video_view);
        if (mCaptureView != null) {
            mCaptureView.setLogMargin(12, 12, 110, 60);
        }

        mPhoneListener = new TXPhoneStateListener(mLivePusher);
        TelephonyManager tm = (TelephonyManager) mActivity.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void resume() {
        if (mCaptureView != null) {
            mCaptureView.onResume();
        }

        if (mVideoPublish && mLivePusher != null && mVideoSrc == VIDEO_SRC_CAMERA) {
            mLivePusher.resumePusher();
            mLivePusher.resumeBGM();
        }
    }

    public void stop() {
        if (mCaptureView != null) {
            mCaptureView.onPause();
        }

        if (mVideoPublish && mLivePusher != null && mVideoSrc == VIDEO_SRC_CAMERA) {
            mLivePusher.pausePusher();
            mLivePusher.pauseBGM();
        }
    }

    public void destroy() {
        stopPublishRtmp();
        if (mCaptureView != null) {
            mCaptureView.onDestroy();
        }

        TelephonyManager tm = (TelephonyManager) mActivity.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
    }

    public void stopPublishRtmp() {
        CLog.d(TAG, "stopPublishRtmp");
        mVideoPublish = false;
        mLivePusher.stopBGM();
        mLivePusher.stopCameraPreview(true);
        mLivePusher.stopScreenCapture();
        mLivePusher.setPushListener(null);
        mLivePusher.stopPusher();
        mCaptureView.setVisibility(View.GONE);

//		if(mBtnHWEncode != null) {
//			//mHWVideoEncode = true;
//			mLivePushConfig.setHardwareAcceleration(mHWVideoEncode ? TXLiveConstants.ENCODE_VIDEO_HARDWARE : TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
//			mBtnHWEncode.setBackgroundResource(R.drawable.quick);
//			mBtnHWEncode.getBackground().setAlpha(mHWVideoEncode ? 255 : 100);
//		}

//		enableQRCodeBtn(true);
//		mBtnPlay.setBackgroundResource(R.drawable.play_start);

        if(mLivePushConfig != null) {
            mLivePushConfig.setPauseImg(null);
        }
    }

    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TXLivePusher> mPusher;
        public TXPhoneStateListener(TXLivePusher pusher) {
            mPusher = new WeakReference<TXLivePusher>(pusher);
        }
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXLivePusher pusher = mPusher.get();
            switch(state){
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    if (pusher != null) pusher.pausePusher();
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (pusher != null) pusher.pausePusher();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    if (pusher != null) pusher.resumePusher();
                    break;
            }
        }
    };
    private PhoneStateListener mPhoneListener = null;

    public void doPlay(String rtmpUrl) {
//        rtmpUrl = "rtmp://aqm.runde.pro:1935/live/36147_1";
        CLog.d(TAG, "doPlay = " + rtmpUrl);
        if (mVideoPublish) {
            stopPublishRtmp();
        } else {
//			if(mVideoSrc == VIDEO_SRC_CAMERA){
            //FixOrAdjustBitrate();  //根据设置确定是“固定”还是“自动”码率
//			} else{
            //录屏横竖屏采用两种分辨率，和摄像头推流逻辑不一样
//			}
            mVideoPublish = startPublishRtmp(rtmpUrl);
        }
    }

    private  boolean startPublishRtmp(String rtmpUrl) {
        CLog.d(TAG, "startPublishRtmp = " + rtmpUrl);

        if (TextUtils.isEmpty(rtmpUrl) || (!rtmpUrl.trim().toLowerCase().startsWith("rtmp://"))) {
            //Toast.makeText(mActivity, "推流地址不合法，目前支持rtmp推流!", Toast.LENGTH_SHORT).show();
            CLog.e(TAG,"推流地址不合法，目前支持rtmp推流!");
            return false;
        }

        if(mVideoSrc != VIDEO_SRC_SCREEN){
            mCaptureView.setVisibility(View.VISIBLE);
        }
        // demo默认不加水印
        //mLivePushConfig.setWatermark(mBitmap, 0.02f, 0.05f, 0.2f);

        int customModeType = 0;

//		if (isActivityCanRotation()) {
//			onActivityRotation();
//		}
        mLivePushConfig.setCustomModeType(customModeType);
        mLivePusher.setPushListener(this);
        mLivePushConfig.setPauseImg(300,5);
//		Bitmap bitmap = decodeResource(getResources(),R.drawable.pause_publish);
//		mLivePushConfig.setPauseImg(bitmap);
        mLivePushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
        if(mVideoSrc != VIDEO_SRC_SCREEN){
            mLivePushConfig.setFrontCamera(mFrontCamera);
//			mLivePushConfig.setBeautyFilter(mBeautyLevel, mWhiteningLevel, mRuddyLevel);
            mLivePusher.setConfig(mLivePushConfig);
            mLivePusher.startCameraPreview(mCaptureView);
        }
        else{
            mLivePusher.setConfig(mLivePushConfig);
            mLivePusher.startScreenCapture();
        }

        mLivePusher.startPusher(rtmpUrl.trim());

//		enableQRCodeBtn(false);

//		mBtnPlay.setBackgroundResource(R.drawable.play_pause);

        return true;
    }

    @Override
    public void onPushEvent(int event, Bundle param) {
//        Log.e("NotifyCode","LivePublisherActivity :" + event);
        String msg = param.getString(TXLiveConstants.EVT_DESCRIPTION);
        String pushEventLog = "receive event: " + event + ", " + msg;
        Log.d(TAG, pushEventLog);
//        if (mLivePusher != null) {
//            mLivePusher.onLogRecord("[event:" + event + "]" + msg + "\n");
//        }
        //错误还是要明确的报一下
        if (event < 0) {
            //Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
            CLog.d(TAG, param.getString(TXLiveConstants.EVT_DESCRIPTION));
            if(event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL || event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL){
                stopPublishRtmp();
            }
        }

        if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT || event == TXLiveConstants.PUSH_ERR_INVALID_ADDRESS) {
            stopPublishRtmp();
        }
        else if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
            CLog.d(TAG, param.getString(TXLiveConstants.EVT_DESCRIPTION));
            //Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
            mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
//			mBtnHWEncode.setBackgroundResource(R.drawable.quick2);
            mLivePusher.setConfig(mLivePushConfig);
            mHWVideoEncode = false;
        }
        else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_UNSURPORT) {
            stopPublishRtmp();
        }
        else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
            stopPublishRtmp();
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_RESOLUTION) {
            Log.d(TAG, "change resolution to " + param.getInt(TXLiveConstants.EVT_PARAM2) + ", bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_BITRATE) {
            Log.d(TAG, "change bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_WARNING_NET_BUSY) {
//			++mNetBusyCount;
//			Log.d(TAG, "net busy. count=" + mNetBusyCount);
//			showNetBusyTips();
        } else if (event == TXLiveConstants.PUSH_EVT_START_VIDEO_ENCODER) {
            int encType = param.getInt(TXLiveConstants.EVT_PARAM1);
            mHWVideoEncode = (encType == 1);
//			mBtnHWEncode.getBackground().setAlpha(mHWVideoEncode ? 255 : 100);
        }
    }

    @Override
    public void onNetStatus(Bundle status) {
//		String str = getNetStatusString(status);
//		Log.d(TAG, "Current status, CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)+
//				", RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)+
//				", SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps"+
//				", FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS)+
//				", ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps"+
//				", VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps");
//        if (mLivePusher != null){
//            mLivePusher.onLogRecord("[net state]:\n"+str+"\n");
//        }
    }
}
