package com.tencent.liteav.demo.cap.record;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.SurfaceHolder;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.manager.CapStorageManager;
import com.tencent.liteav.demo.cap.util.CapRecorderTimer;

public class CapCameraThread extends Thread {
	private static final String TAG = CapCameraThread.class.getSimpleName();
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private MediaRecorder mediarecorder;// 录制视频的类private long
	private SurfaceHolder surfaceHolder;
	private Camera mCamera;
	private CapRecorderTimer mTimer;
 
	public CapCameraThread(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
	}
 
	@Override
	public void run() {
		CLog.d(TAG, "run");
		/** * 开始录像 */
		startRecord();
		startRecordTimer();
	}

	private void startRecordTimer() {
		CLog.d(TAG, "startRecordTimer");
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
					CLog.d(TAG, e.getMessage());
				}
			}

		});
		mTimer.startTimer(CapConfig.TIME_CAMERA_RECORD, CapConfig.TIME_CAMERA_RECORD);
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
 
	/** * 开始录像 */
	public void startRecord() {
		CLog.d(TAG, "startRecord");
		mCamera = getCameraInstance(); // 解锁camera
		startMediaRecorder();
	}

	public void startMediaRecorder() {
		CLog.d(TAG, "startMediaRecorder");
		if (mCamera == null) {
			CLog.d(TAG, "mCamera == null");
			return;
		}
		if (!CapStorageManager.getInstance().checkExternalStorageSpaceEnough()) {
			stopRecord();
		}

		try {
			// 准备录制
			mCamera.unlock();
			mediarecorder = new MediaRecorder();// 创建mediarecorder对象
			mediarecorder.setCamera(mCamera); // 设置录制视频源为Camera(相机)
			mediarecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 设置录制文件质量，格式，分辨率之类，这个全部包括了
			mediarecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
			mediarecorder.setPreviewDisplay(surfaceHolder.getSurface()); // 设置视频文件输出的路径
			mediarecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

			mediarecorder.prepare(); // 开始录制
			mediarecorder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void stopMediaRecorder() {
		CLog.d(TAG, "stopMediaRecorder");
		if (mediarecorder != null) {
			// 清除recorder配置
			//mediarecorder.reset();
			// 停止录制
			mediarecorder.stop();
			// 释放recorder对象
			mediarecorder.release();
			mediarecorder = null;
			// 为后续使用锁定摄像头
			if (mCamera != null) {
				mCamera.lock();
			}
		}
	}
 
 
	/** * 停止录制 */
	public void stopRecord() {
		CLog.d(TAG, "stopRecord");
		if (mTimer != null) {
			mTimer.exit();
		}

		stopMediaRecorder();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	private static File getOutputMediaFile(int type) {
		// 判断SDCard是否存在
//		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//			CLog.d(TAG, "SDCard不存在");
//			return null;
//		}
 	    //Environment.getExternalStorageDirectory()
		File mediaStorageDir = new File(CapConfig.PATH_VIDEO_RECORD);
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdir()) {
				CLog.d(TAG, "failed to create directory");
				return null;
			}
		}
		// 创建媒体文件名
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timestamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timestamp + ".3gp");
		} else {
			CLog.d(TAG, "文件类型有误");
			return null;
		}
 
		return mediaFile;
	}
}

