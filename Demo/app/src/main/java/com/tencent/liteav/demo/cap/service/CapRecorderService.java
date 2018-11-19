package com.tencent.liteav.demo.cap.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.cap.record.CapCameraThread;
import com.tencent.liteav.demo.cap.util.CapRecorderTimer;

import java.io.File;
import java.util.Date;

/**
 * Created by XingYue on 2018/11/17.
 */

public class CapRecorderService extends Service {
    public static final String TAG = CapRecorderService.class.getSimpleName();

    private RecordBinder mBinder = new RecordBinder();
    private SurfaceView mSVRecorder;// 视频预览控件
//    private SurfaceHolder surfaceHolder; // //和surfaceView相关的
    WindowManager wm;
    LinearLayout mLLRecorder;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private CapRecorderTimer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        CLog.d(TAG, "onCreate");
        if (!checkCameraHardware(this)) {
            CLog.e(TAG, "checkCameraHardware = false");
            stopSelf();
            return;
        }
        // 设置悬浮窗体属性
        // 1.得到WindoeManager对象：
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        // 2.得到WindowManager.LayoutParams对象，为后续设置相关参数做准备：
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        // 3.设置相关的窗口布局参数，要实现悬浮窗口效果，要需要设置的参数有
        // 3.1设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 3.2设置图片格式，效果为背景透明 //wmParams.format = PixelFormat.RGBA_8888;
        wmParams.format = 1;
        // 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 4.// 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 5. 调整悬浮窗口至中间
        wmParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER;
        // 6. 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;
        // 7.将需要加到悬浮窗口中的View加入到窗口中了：
        // 如果view没有被加入到某个父组件中，则加入WindowManager中
        mSVRecorder = new SurfaceView(this);
//        surfaceHolder = mSVRecorder.getHolder();
        WindowManager.LayoutParams params_sur = new WindowManager.LayoutParams();
        params_sur.width = 240;
        params_sur.height = 240;
        params_sur.alpha = 255;
        mSVRecorder.setLayoutParams(params_sur);

        mSVRecorder.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // surface.getHolder().setFixedSize(800, 1024);
        //mSVRecorder.getHolder().addCallback((SurfaceHolder.Callback) this);

        mLLRecorder = new LinearLayout(this);
        WindowManager.LayoutParams params_rel = new WindowManager.LayoutParams();
        params_rel.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params_rel.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLLRecorder.setLayoutParams(params_rel);
        mLLRecorder.addView(mSVRecorder);
        wm.addView(mLLRecorder, wmParams); // 创建View
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CLog.d(TAG, "onStartCommand");

        String extraValue = intent.getStringExtra(CapConstants.EXTRA_CMD_RECORDER);
        if (CapConstants.CMD_START.equals(extraValue)) {
            startRecord();
        } else {
            stopRecord();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.d(TAG, "onDestroy");
        destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class RecordBinder extends Binder {

    }

    public void destroy() {
        CLog.d(TAG, "destroy");
        stopRecord();
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
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置音频的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            // 设置视频的编码格式
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            // 设置视频的采样率，每秒4帧
            mediaRecorder.setVideoFrameRate(4);
//            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(getOutputMediaFile().toString());
            mediaRecorder.setMaxDuration(CapConfig.TIME_CAMERA_RECORD);
            // 设置捕获视频图像的预览界面
            mediaRecorder.setPreviewDisplay(mSVRecorder.getHolder().getSurface());
            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    // 发生错误，停止录制
                    stopRecord();
                }
            });

            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                    CLog.d( TAG, "MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
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
        }
    }

    private void stopMediaRecorder() {
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
        if (!checkCameraHardware(this)) {
            CLog.e(TAG, "checkCameraHardware = false");
            return;
        }
        startMediaRecorder();
//        new Thread() {
//            public void run() {
//                startMediaRecorder();
//            }
//        }.start();
        //startRecordTimer();
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
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }
}
