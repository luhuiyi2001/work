package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.util.CapRecorderTimer;

import java.io.File;
import java.util.Date;

public class CapVideoRecordImpl {
    private static final String TAG = CapVideoRecordImpl.class.getSimpleName();

    private FrameLayout mRecordLayout;
    private SurfaceView mRecordSV;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private CapRecorderTimer mTimer;

    private Activity mActivity;
    public CapVideoRecordImpl(Activity context) {
        mActivity = context;
    }

    public void initView() {
        CLog.d(TAG, "initView");
        mRecordLayout = (FrameLayout)mActivity.findViewById(R.id.rl_record);
        mRecordSV = (SurfaceView)mActivity.findViewById(R.id.sv_record);
    }

    public void destroy() {
        CLog.d(TAG, "destroy");
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public void startMediaRecorder() {
        CLog.d(TAG, "startMediaRecorder");
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.reset();
            // 设置音频录入源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置视频图像的录入源
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 设置录入媒体的输出格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置音频的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            // 设置视频的编码格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            // 设置视频的采样率，每秒4帧
            mediaRecorder.setVideoFrameRate(4);
            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(getOutputMediaFile().toString());
            // 设置捕获视频图像的预览界面
            mediaRecorder.setPreviewDisplay(mRecordSV.getHolder().getSurface());
            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    // 发生错误，停止录制
                    stopMediaRecorder();
                }
            });

            // 准备、开始
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
//            mRecordLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMediaRecorder() {
        CLog.d(TAG, "stopMediaRecorder");
        if (isRecording) {
            // 如果正在录制，停止并释放资源
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording=false;
//            mRecordLayout.setVisibility(View.GONE);
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
        startMediaRecorder();
        startRecordTimer();
    }

    public void stopRecord() {
        CLog.d(TAG, "stopRecord");
        if (mTimer != null) {
            mTimer.exit();
        }
        stopMediaRecorder();
    }

    private void startRecordTimer() {
        CLog.d(TAG, "startRecordTimer");
        if (!isRecording) {
            return;
        }
        if (mTimer == null) {
            mTimer = new CapRecorderTimer();
        }
        mTimer.setOnScheduleListener(new CapRecorderTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
                CLog.d(TAG, "onSchedule");
                try {
                    stopMediaRecorder();
                    startMediaRecorder();
                } catch (Exception e) {
                    CLog.e(TAG, e.getMessage());
                }
            }

        });
        mTimer.startTimer(CapConfig.TIME_CAMERA_RECORD, CapConfig.TIME_CAMERA_RECORD);
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

}
