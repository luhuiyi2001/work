package com.tencent.liteav.demo.cap.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.inter.CapActivityInterface;
import com.tencent.liteav.demo.cap.manager.CapStorageManager;
import com.tencent.liteav.demo.cap.util.CapRecorderTimer;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;

public class CapRecorderFragment extends Fragment implements SurfaceHolder.Callback {

    private static final String TAG = CapRecorderFragment.class.getSimpleName();

    private Activity mActivity;
    private CapActivityInterface mActivityInterface;

    private SurfaceView mRecordSV;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;

    public static CapRecorderFragment newInstance() {
        CLog.d(TAG,"newInstance = ");
        CapRecorderFragment fragment = new CapRecorderFragment();
        Bundle bundle = new Bundle();
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
        View view = inflater.inflate(R.layout.fragment_recorder, container, false);

        mRecordSV = (SurfaceView) view.findViewById(R.id.sv_recorder);
        mRecordSV.getHolder().addCallback(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CLog.d(TAG,"onActivityCreated");

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mActivityInterface.setTitle("Video Recording");

    }

    @Override
    public void onStop() {
        super.onStop();
        CLog.d(TAG,"onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        CLog.d(TAG,"onResume");

//        Handler mainHandler = new Handler(Looper.getMainLooper());
//        mainHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startRecord();
//            }
//        }, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        CLog.d(TAG,"onPause");
//        this.stopRecord();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CLog.d(TAG,"onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.d(TAG,"onDestroy");
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
    private void destroy() {
        CLog.d(TAG, "destroy");
        stopMediaRecorder();
    }

    private void backStack(){
        CLog.d(TAG, "destroy");
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mActivity != null) {
                        FragmentManager fm = mActivity.getFragmentManager();
                        FragmentTransaction ts = fm.beginTransaction();
                        ts.remove(CapRecorderFragment.this);
                        ts.commit();
                    }
                }
            });
        }
    }

    private void startMediaRecorder() {
        CLog.d(TAG, "startMediaRecorder");
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.reset();
            // 设置音频录入源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置视频图像的录入源
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 设置录入媒体的输出格式
            //            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置音频的编码格式
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            // 设置视频的编码格式
//            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            // 设置视频的采样率，每秒4帧
//            mediaRecorder.setVideoFrameRate(4);
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(getOutputMediaFile().toString());
            // 设置捕获视频图像的预览界面
            mediaRecorder.setPreviewDisplay(mRecordSV.getHolder().getSurface());
            mediaRecorder.setMaxDuration(CapConfig.TIME_CAMERA_RECORD);
            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    CLog.e(TAG, "onError = [ " + what + ", " + extra + ", ]");
                    // 发生错误，停止录制
                    stopRecord();
                }
            });

            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        CLog.d(TAG, "onInfo = [ " + what + ", " + extra + ", ]");
                        stopRecord();
                        startRecord();
                    }
                }
            });

            // 准备、开始
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
//            mRecordLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            CLog.e(TAG, e.getMessage());
            isRecording = false;
        }
    }

    private void stopMediaRecorder() {
        CLog.d(TAG, "stopMediaRecorder");
        if (isRecording) {
            // 如果正在录制，停止并释放资源
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording=false;
            } catch (Exception e) {
                e.printStackTrace();
                CLog.e(TAG, e.getMessage());
                isRecording = false;
            }
        }
    }
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(CapConfig.PATH_VIDEO_RECORD);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                CLog.e(TAG, "failed to create directory");
                return null;
            }
        }
        // 创建媒体文件名
        String timestamp = CapConfig.DATE_FORMAT.format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "RD_" + timestamp + ".mp4");
    }
    public void startRecord() {
        CLog.d(TAG, "startRecord");
        if (!CapUtils.checkExtSdcard()) {
            CLog.e(TAG, "Ext SDCard isn't exist");
            return;
        }
        if (!checkCameraHardware(mActivity)) {
            CLog.e(TAG, "checkCameraHardware = false");
            return;
        }
        if (!CapStorageManager.getInstance().checkExternalStorageSpaceEnough()) {
            CLog.e(TAG, "checkExternalStorageSpaceEnough = false");
            stopRecord();
        }
        startMediaRecorder();
        //startRecordTimer();
    }

    public void stopRecord() {
        CLog.d(TAG, "stopRecord");
        stopMediaRecorder();
    }

    /**
     * 检测摄像头硬件 如果应用程序未使用manifest声明对摄像头需求进行特别指明，则应该在运行时检查一下摄像头是否可用
     */
    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
        CLog.d(TAG, "surfaceChanged = [ " + arg1 + ", " + arg2 + ", " + arg3 + " ]");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CLog.d(TAG, "surfaceCreated");
        CLog.i("process", Thread.currentThread().getName());
        // //录像线程，当然也可以在别的地方启动，但是一定要在onCreate方法执行完成以及surfaceHolder被赋值以后启动
        this.startRecord();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CLog.d(TAG, "surfaceDestroyed");
        // surfaceDestroyed的时候同时对象设置为null
        destroy();
        mRecordSV = null;
    }
}
