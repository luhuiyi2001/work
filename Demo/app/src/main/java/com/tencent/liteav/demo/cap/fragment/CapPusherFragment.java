package com.tencent.liteav.demo.cap.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.inter.CapActivityInterface;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;
import com.tencent.liteav.demo.common.misc.AndroidPermissions;
import com.tencent.liteav.demo.push.LivePublisherActivity;
import com.tencent.liteav.demo.roomutil.commondef.PusherInfo;
import com.tencent.liteav.demo.roomutil.commondef.RoomInfo;
import com.tencent.liteav.demo.rtcroom.IRTCRoomListener;
import com.tencent.liteav.demo.rtcroom.RTCRoom;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CapPusherFragment extends Fragment implements ITXLivePushListener {

    private static final String TAG = CapPusherFragment.class.getSimpleName();

    private Activity mActivity;
    private CapActivityInterface mActivityInterface;

    private TXLivePushConfig mLivePushConfig;
    private TXLivePusher mLivePusher;
    private TXCloudVideoView mCaptureView;

    private boolean mVideoPublish;
    private String mUrl;


    public static CapPusherFragment newInstance(String url) {
        CapPusherFragment fragment = new CapPusherFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CapConstants.KEY_URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CLog.d(TAG,"onAttach");
        mActivity = ((Activity) context);
        mActivityInterface = ((CapActivityInterface) context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        CLog.d(TAG,"onAttach");
        mActivity = ((Activity) activity);
        mActivityInterface = ((CapActivityInterface) activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        CLog.d(TAG,"onCreateView");
        View view = inflater.inflate(R.layout.activity_publish, container, false);

        mCaptureView = (TXCloudVideoView) view.findViewById(R.id.video_view);
        mCaptureView.setLogMargin(12, 12, 110, 60);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CLog.d(TAG,"onActivityCreated");
        mLivePusher     = new TXLivePusher(mActivity);
        mLivePushConfig = new TXLivePushConfig();
        mLivePushConfig.setVideoEncodeGop(5);
//        mLivePushConfig.setBeautyFilter(mBeautyLevel, mWhiteningLevel, mRuddyLevel);
        mLivePusher.setConfig(mLivePushConfig);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getArguments();
        mUrl = bundle.getString(CapConstants.KEY_URL);

        mActivityInterface.setTitle("Publish RTMP");

        mPhoneListener = new TXPhoneStateListener(mLivePusher);
        TelephonyManager tm = (TelephonyManager) mActivity.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        CLog.d(TAG,"onStop");
        if (mCaptureView != null) {
            mCaptureView.onPause();
        }

        if (mVideoPublish && mLivePusher != null) {
            mLivePusher.pausePusher();
            mLivePusher.pauseBGM();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CLog.d(TAG,"onResume");
        if (mCaptureView != null) {
            mCaptureView.onResume();
        }

        if (mVideoPublish && mLivePusher != null) {
            mLivePusher.resumePusher();
            mLivePusher.resumeBGM();
        }
        if (!mVideoPublish) {
            startPublishRtmp();
        }
//        mMainHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                doRtmp();
//            }
//        }, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        CLog.d(TAG,"onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CLog.d(TAG,"onDestroyView");
        stopPublishRtmp();
        if (mCaptureView != null) {
            mCaptureView.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.d(TAG,"onDestroy");
        TelephonyManager tm = (TelephonyManager) mActivity.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        CLog.d(TAG,"onDetach");
        mActivity = null;
        mActivityInterface = null;
    }

    public void onBackPressed() {
        CLog.d(TAG,"onBackPressed");
        backStack();
    }

    private void backStack(){
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mActivity != null) {
                        FragmentManager fm = mActivity.getFragmentManager();
                        FragmentTransaction ts = fm.beginTransaction();
                        ts.remove(CapPusherFragment.this);
                        ts.commit();
//                        FragmentManager fragmentManager = mActivity.getFragmentManager();
//                        fragmentManager.popBackStack();
//                        fragmentManager.beginTransaction().commit();
                    }
                }
            });
        }
    }

    public  void startPusher() {
        if (!mVideoPublish) {
//            stopPublishRtmp();
//        } else {
//            if(mVideoSrc == VIDEO_SRC_CAMERA){
//                FixOrAdjustBitrate();  //根据设置确定是“固定”还是“自动”码率
//            }
//            else{
//                //录屏横竖屏采用两种分辨率，和摄像头推流逻辑不一样
//            }
            mVideoPublish = startPublishRtmp();
        }
    }

    private  boolean startPublishRtmp() {
        mCaptureView.setVisibility(View.VISIBLE);

        int customModeType = 0;

        mLivePushConfig.setCustomModeType(customModeType);
        mLivePusher.setPushListener(this);
        mLivePushConfig.setPauseImg(300,5);
        mLivePushConfig.setPauseImg(null);
        mLivePushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
        mLivePushConfig.setFrontCamera(false);
        mLivePusher.setConfig(mLivePushConfig);
        mLivePusher.startCameraPreview(mCaptureView);

        mLivePusher.startPusher(mUrl.trim());

        return true;
    }

    private void stopPublishRtmp() {
        mVideoPublish = false;
        mLivePusher.stopBGM();
        mLivePusher.stopCameraPreview(true);
        mLivePusher.stopScreenCapture();
        mLivePusher.setPushListener(null);
        mLivePusher.stopPusher();
        mCaptureView.setVisibility(View.GONE);

        if(mLivePushConfig != null) {
            mLivePushConfig.setPauseImg(null);
        }
    }

    @Override
    public void onPushEvent(int event, Bundle param) {
//        Log.e("NotifyCode","LivePublisherActivity :" + event);
        String msg = param.getString(TXLiveConstants.EVT_DESCRIPTION);
        String pushEventLog = "receive event: " + event + ", " + msg;
        CLog.d(TAG, pushEventLog);
//        if (mLivePusher != null) {
//            mLivePusher.onLogRecord("[event:" + event + "]" + msg + "\n");
//        }
        //错误还是要明确的报一下
        if (event < 0) {
            CLog.d(TAG, param.getString(TXLiveConstants.EVT_DESCRIPTION));
            if(event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL || event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL){
//                stopPublishRtmp();
                onBackPressed();
            }
        }

        if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT || event == TXLiveConstants.PUSH_ERR_INVALID_ADDRESS) {
//            stopPublishRtmp();
            onBackPressed();
        }
        else if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
            CLog.d(TAG, param.getString(TXLiveConstants.EVT_DESCRIPTION));
            mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
            mLivePusher.setConfig(mLivePushConfig);
        }
        else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_UNSURPORT) {
//            stopPublishRtmp();
            onBackPressed();
        }
        else if (event == TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
//            stopPublishRtmp();
            onBackPressed();
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_RESOLUTION) {
            Log.d(TAG, "change resolution to " + param.getInt(TXLiveConstants.EVT_PARAM2) + ", bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_BITRATE) {
            Log.d(TAG, "change bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_WARNING_NET_BUSY) {
//            ++mNetBusyCount;
//            Log.d(TAG, "net busy. count=" + mNetBusyCount);
//            showNetBusyTips();
        } else if (event == TXLiveConstants.PUSH_EVT_START_VIDEO_ENCODER) {
            int encType = param.getInt(TXLiveConstants.EVT_PARAM1);
//            mHWVideoEncode = (encType == 1);
//            mBtnHWEncode.getBackground().setAlpha(mHWVideoEncode ? 255 : 100);
        }
    }

    @Override
    public void onNetStatus(Bundle status) {
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
}
