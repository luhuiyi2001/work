package com.tencent.liteav.demo.cap.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.callback.CapActivityInterface;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.manager.CapStorageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class CapRecorderFragment extends Fragment implements SurfaceHolder.Callback {

    private static final String TAG = CapRecorderFragment.class.getSimpleName();
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Activity mActivity;
    private CapActivityInterface mActivityInterface;

    private SurfaceView mRecordSV;
    private boolean isRecording;
    private MediaRecorder mediaRecorder;
    private Handler mHandler = new Handler();
    private Camera mCamera;
    private boolean isResume;
    final Thread mStartRecorderThread = new Thread() {
        @Override public void run() {
            startRecord();
        }
    };

    final Runnable mStartRecordTimeout = new Runnable() {
        @Override
        public void run() {
            startRecord();
        }
    };

    final Thread mStopRecorderThread = new Thread() {
        @Override public void run() {
            stopRecord();
        }
    };
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
        isResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        CLog.d(TAG,"onPause");
//        this.stopRecord();
        isResume = false;
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

    private  void onRecordError() {
        launchStartRecordTimeout();
    }

    private void launchStartRecordTimeout() {
        if (isResume) {
            mHandler.removeCallbacks(mStartRecordTimeout);
            mHandler.postDelayed(mStartRecordTimeout, 30000);
        }
    }
    private void destroy() {
        CLog.d(TAG, "destroy");
        mHandler.removeCallbacks(mStartRecordTimeout);
        stopRecord();
    }

    private void backStack(){
        CLog.d(TAG, "backStack");
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

    /** * 获取摄像头实例对象 * * @return */
    public Camera getCameraInstance() {
        CLog.d(TAG, "getCameraInstance");
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            // 打开摄像头错误
            CLog.e(TAG, "打开摄像头错误");
            onRecordError();
        }
        return c;
    }

    private synchronized void startMediaRecorder() {
        CLog.d(TAG, "startMediaRecorder");
        if (mCamera == null) {
            CLog.e(TAG, "mCamera == null");
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
            mediaRecorder.setProfile(CamcorderProfile.get(CapConfig.IS_TEST ? CamcorderProfile.QUALITY_LOW : CamcorderProfile.QUALITY_720P));
            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
            // 设置捕获视频图像的预览界面
            mediaRecorder.setPreviewDisplay(mRecordSV.getHolder().getSurface());
            mediaRecorder.setMaxDuration(CapConfig.TIME_CAMERA_RECORD);
            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    CLog.e(TAG, "onError = [ " + what + ", " + extra + " ]");
                    // 发生错误，停止录制
                    stopRecord();
                    onRecordError();
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
            mHandler.removeCallbacks(mStartRecordTimeout);
        } catch (Exception e) {
            e.printStackTrace();
            CLog.e(TAG, e.getMessage());
            releaseMediaRecorder();
            onRecordError();
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
        CLog.d(TAG, "startRecord");
        if (!isResume) {
            CLog.d(TAG, "isResume is false!");
            return;
        }
        if (!CapStorageManager.getInstance().checkExternalStorageSpaceEnough()) {
            CLog.e(TAG, "checkExternalStorageSpaceEnough = false");
            stopRecord();
            return;
        }
        mCamera = getCameraInstance(); // 解锁camera

        if (mCamera == null) {
            return;
        }
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
