package com.tencent.liteav.demo.cap.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
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
import com.tencent.liteav.demo.cap.manager.CapStorageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by XingYue on 2018/11/17.
 */

public class CapRecorderService extends Service  implements SurfaceHolder.Callback{
    public static final String TAG = CapRecorderService.class.getSimpleName();
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private RecordBinder mBinder = new RecordBinder();
    private SurfaceView mSVRecorder;// 视频预览控件
//    private SurfaceHolder surfaceHolder; // //和surfaceView相关的
    WindowManager wm;
    LinearLayout mLLRecorder;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private Camera mCamera;
    final Thread mStartRecorderThread = new Thread() {
        @Override public void run() {
            startMediaRecorder();
        }
    };

    final Thread mStopRecorderThread = new Thread() {
        @Override public void run() {
            stopRecord();
        }
    };
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
        params_sur.width = 320;
        params_sur.height = 640;
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

    public class RecordBinder extends Binder {
        public CapRecorderService getService(){
            return CapRecorderService.this;
        }
    }

    public void destroy() {
        CLog.d(TAG, "destroy");
        stopRecord();
    }


    /** * 获取摄像头实例对象 * * @return */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            // 打开摄像头错误
            CLog.e(TAG, "打开摄像头错误");
        }
        return c;
    }

    private synchronized void startMediaRecorder() {
        CLog.d(TAG, "startMediaRecorder : " + isRecording);
        if (mCamera == null) {
            CLog.e(TAG, "mCamera == null");
            return;
        }

        if (isRecording) {
            CLog.e(TAG, "isRecording");
            return;
        }

        try {
            // 准备录制
            mCamera.unlock();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(mCamera); // 设置录制视频源为Camera(相机)
            mediaRecorder.reset();
            // 设置音频录入源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
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
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
            // 设置捕获视频图像的预览界面
            mediaRecorder.setPreviewDisplay(mSVRecorder.getHolder().getSurface());
            mediaRecorder.setMaxDuration(CapConfig.TIME_CAMERA_RECORD);
            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    CLog.e(TAG, "onError = [ " + what + ", " + extra + " ]");
                    // 发生错误，停止录制
                    stopRecord();
                }
            });

            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        CLog.d(TAG, "onInfo = [ " + what + ", " + extra + ", ]");
                        stopMediaRecorder();
                        startMediaRecorder();
                    }
                }
            });

            // 准备、开始
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
            CLog.e(TAG, e.getMessage());
            releaseMediaRecorder();
        }
    }

    private synchronized void stopMediaRecorder() {
        CLog.d(TAG, "stopMediaRecorder");
        if (isRecording) {
            // 如果正在录制，停止并释放资源
            releaseMediaRecorder();
            // 为后续使用锁定摄像头
            if (mCamera != null) {
                mCamera.lock();
            }
            isRecording = false;
        }
    }

    private synchronized void releaseMediaRecorder() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            CLog.e(TAG, e.getMessage());
        }
    }
    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(type == MEDIA_TYPE_VIDEO ? CapConfig.PATH_VIDEO_RECORD : CapConfig.PATH_PHOTO);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                CLog.e(TAG, mediaStorageDir.getAbsolutePath() + " ： failed to create directory");
                return null;
            }
        }
        // 创建媒体文件名
        StringBuilder sb = new StringBuilder();
        sb.append(type == MEDIA_TYPE_VIDEO ? "VID" : "IMG");
        sb.append(CapConfig.DATE_FORMAT.format(new Date()));
        sb.append(type == MEDIA_TYPE_VIDEO ? ".mp4" : ".jpg");
        return new File(mediaStorageDir.getPath() + File.separator + sb.toString());
    }
    public synchronized void startRecord() {
        CLog.d(TAG, "startRecord : " + isRecording);
        if (isRecording) {
            CLog.e(TAG, "isRecording");
            return;
        }
        if (!CapUtils.checkExtSdcard()) {
            CLog.e(TAG, "Ext SDCard isn't exist");
            return;
        }
        if (!checkCameraHardware(this)) {
            CLog.e(TAG, "checkCameraHardware = false");
            return;
        }
        if (!CapStorageManager.getInstance().checkExternalStorageSpaceEnough()) {
            CLog.e(TAG, "checkExternalStorageSpaceEnough = false");
            stopRecord();
            return;
        }
        //startMediaRecorder();
        //startRecordTimer();
//        mHandler.removeCallbacks(mStartRecorderRunable);
//        this.mHandler.post(mStartRecorderRunable);
        mCamera = getCameraInstance(); // 解锁camera

//        mStartRecorderThread.start();
        new Thread() {
            @Override public void run() {
                startMediaRecorder();
            }
        }.start();
    }

    public synchronized void stopRecord() {
        CLog.d(TAG, "stopRecord");
        stopMediaRecorder();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
//        mHandler.removeCallbacks(mStopRecorderRunable);
//        this.mHandler.post(mStopRecorderRunable);
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
//        this.startRecord();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CLog.d(TAG, "surfaceDestroyed");
        // surfaceDestroyed的时候同时对象设置为null
        destroy();
        mSVRecorder = null;
    }

    public void takePicture() {
        mCamera.takePicture(null, null, null, new JpegPictureCallback());
    }

    public final class JpegPictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] jpegData, android.hardware.Camera camera) {
            CLog.d(TAG, "[onPictureTaken]");
            if (jpegData == null) {
                CLog.i(TAG, "[onPictureTaken],data is null,return");
                return;
            }
            File pictureFile = new File(getOutputMediaFile(MEDIA_TYPE_IMAGE).toString());
            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(jpegData);
                fos.close();
            }catch (Exception e){
                CLog.d("takePhoto", "File not found: " + e.getMessage());
            }
        }
    }
}
