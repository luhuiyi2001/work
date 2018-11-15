/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.liteav.demo.cap.home;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.listener.CapReceiveEventImpl;
import com.tencent.liteav.demo.cap.record.CapRecordSerivice;
import com.tencent.liteav.demo.push.LivePublisherActivity;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

public class Home extends Activity implements ITXLivePushListener {
	private static final String TAG = Home.class.getSimpleName();
	private static final String KEY_SAVE_GRID_OPENED = "grid.opened";
	private static final int VIDEO_SRC_CAMERA = 0;
	private static final int VIDEO_SRC_SCREEN = 1;

	private static ArrayList<ApplicationInfo> mApplications;

	private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
	}

	private GridView mGrid;

	private View mShowApplications;
	private CheckBox mShowApplicationsCheck;

	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mStrings = new ArrayList<String>(10);
    public static final int LOG_COUNT = 18;
    private CapReceiveEventImpl mReceiveEventImpl;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					break;

				case 2:
					break;
			}
		}
	};

	private class LoginBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			CLog.d(TAG, "action = " + action);
			if (CapConstants.ACTION_UPDATE_STATE_INFO.equals(action)) {
				String msg = intent.getStringExtra(CapConstants.EXTRA_UPDATE_STATE_INFO);
				updateStateInfo(msg);
			} else if (CapConstants.ACTION_START_PUBLISH.equals(action)) {
				String url = intent.getStringExtra(CapConstants.EXTRA_PUSH_URL);
				doPlay(url);
			} else if (CapConstants.ACTION_STOP_PUBLISH.equals(action)) {
				stopPublishRtmp();
			}
		}
	}
	private LoginBroadcastReceiver mReceiver = new LoginBroadcastReceiver();


	private TXLivePushConfig mLivePushConfig;
	private TXLivePusher mLivePusher;
	private TXCloudVideoView mCaptureView;
	private boolean          mVideoPublish;
	private int              mVideoSrc = VIDEO_SRC_CAMERA;
	private boolean          mHWVideoEncode = true;
	private boolean          mFrontCamera = true;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mLivePusher     = new TXLivePusher(this);
		mLivePushConfig = new TXLivePushConfig();
		mLivePushConfig.setVideoEncodeGop(5);
//		mLivePushConfig.setBeautyFilter(mBeautyLevel, mWhiteningLevel, mRuddyLevel);
		mLivePusher.setConfig(mLivePushConfig);
		mVideoPublish = false;
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		setContentView(R.layout.home);

		registerIntentReceivers();

		loadApplications(true);

		bindApplications();
		bindButtons();

		mCaptureView = (TXCloudVideoView) findViewById(R.id.video_view);
		mCaptureView.setLogMargin(12, 12, 110, 60);

		mListView = (ListView) findViewById(R.id.list_info);
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrings);
		mListView.setAdapter(mAdapter);
		mReceiveEventImpl =  new CapReceiveEventImpl(this);
		CapClientManager.getInstance().setOnReceiveEventListener(mReceiveEventImpl);

		checkPublishPermission();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPhoneListener = new Home.TXPhoneStateListener(mLivePusher);
		TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// Close the menu
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			getWindow().closeAllPanels();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Remove the callback for the cached drawables or we leak
		// the previous Home screen on orientation change
		final int count = mApplications.size();
		for (int i = 0; i < count; i++) {
			mApplications.get(i).icon.setCallback(null);
		}

		unregisterReceiver(mApplicationsReceiver);

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

		stopPublishRtmp();
		if (mCaptureView != null) {
			mCaptureView.onDestroy();
		}

//		mRotationObserver.stopObserver();

		TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		CapClientManager.getInstance().onStart();
		if (mCaptureView != null) {
			mCaptureView.onResume();
		}

		if (mVideoPublish && mLivePusher != null && mVideoSrc == VIDEO_SRC_CAMERA) {
			mLivePusher.resumePusher();
			mLivePusher.resumeBGM();
		}
	}

	@Override
	public void onStop(){
		super.onStop();
		if (mCaptureView != null) {
			mCaptureView.onPause();
		}

		if (mVideoPublish && mLivePusher != null && mVideoSrc == VIDEO_SRC_CAMERA) {
			mLivePusher.pausePusher();
			mLivePusher.pauseBGM();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		final boolean opened = state.getBoolean(KEY_SAVE_GRID_OPENED, false);
		if (opened) {
			showApplications(false);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_SAVE_GRID_OPENED,
				mGrid.getVisibility() == View.VISIBLE);
	}

	/**
	 * Registers various intent receivers. The current implementation registers
	 * only a wallpaper intent receiver to let other applications change the
	 * wallpaper.
	 */
	private void registerIntentReceivers() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mApplicationsReceiver, filter);

		IntentFilter intentFilter = new IntentFilter(CapConstants.ACTION_UPDATE_STATE_INFO);
		filter.addAction(CapConstants.ACTION_START_PUBLISH);
		filter.addAction(CapConstants.ACTION_STOP_PUBLISH);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,intentFilter);
	}

	/**
	 * Creates a new appplications adapter for the grid view and registers it.
	 */
	private void bindApplications() {
		if (mGrid == null) {
			mGrid = (GridView) findViewById(R.id.all_apps);
		}
		mGrid.setAdapter(new ApplicationsAdapter(this, mApplications));
		mGrid.setSelection(0);

	}

	/**
	 * Binds actions to the various buttons.
	 */
	private void bindButtons() {
		mShowApplications = findViewById(R.id.show_all_apps);
		mShowApplications.setOnClickListener(new ShowApplications());
		mShowApplicationsCheck = (CheckBox) findViewById(R.id.show_all_apps_check);

		mGrid.setOnItemClickListener(new ApplicationLauncher());
	}

	/**
	 * Loads the list of installed applications in mApplications.
	 */
	private void loadApplications(boolean isLaunching) {
		if (isLaunching && mApplications != null) {
			return;
		}

		PackageManager manager = getPackageManager();

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = manager.queryIntentActivities(
				mainIntent, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

		if (apps != null) {
			final int count = apps.size();

			if (mApplications == null) {
				mApplications = new ArrayList<ApplicationInfo>(count);
			}
			mApplications.clear();

			for (int i = 0; i < count; i++) {
				ApplicationInfo application = new ApplicationInfo();
				ResolveInfo info = apps.get(i);

				application.title = info.loadLabel(manager);
				application.setActivity(new ComponentName(
						info.activityInfo.applicationInfo.packageName,
						info.activityInfo.name), Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				application.icon = info.activityInfo.loadIcon(manager);

				mApplications.add(application);
			}
		}
	}

	/**
	 * Shows all of the applications by playing an animation on the grid.
	 */
	private void showApplications(boolean animate) {

		mShowApplicationsCheck.toggle();

		mGrid.setVisibility(View.VISIBLE);


	}

	/**
	 * Hides all of the applications by playing an animation on the grid.
	 */
	private void hideApplications() {

		mShowApplicationsCheck.toggle();

		mGrid.setVisibility(View.INVISIBLE);
		mShowApplications.requestFocus();
	}

	/**
	 * Receives notifications when applications are added/removed.
	 */
	private class ApplicationsIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			loadApplications(false);
			bindApplications();
		}
	}

	/**
	 * GridView adapter to show the list of all installed applications.
	 */
	private class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
		private Rect mOldBounds = new Rect();

		public ApplicationsAdapter(Context context,
				ArrayList<ApplicationInfo> apps) {
			super(context, 0, apps);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ApplicationInfo info = mApplications.get(position);

			if (convertView == null) {
				final LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.application, parent,
						false);
			}

			Drawable icon = info.icon;

			if (!info.filtered) {
				 final Resources resources = getContext().getResources();
				int width = (int)resources.getDimension(android.R.dimen.app_icon_size);
				int height = (int)resources.getDimension(android.R.dimen.app_icon_size);

				final int iconWidth = icon.getIntrinsicWidth();
				final int iconHeight = icon.getIntrinsicHeight();

				if (icon instanceof PaintDrawable) {
					PaintDrawable painter = (PaintDrawable) icon;
					painter.setIntrinsicWidth(width);
					painter.setIntrinsicHeight(height);
				}

				if (width > 0 && height > 0
						&& (width < iconWidth || height < iconHeight)) {
					final float ratio = (float) iconWidth / iconHeight;

					if (iconWidth > iconHeight) {
						height = (int) (width / ratio);
					} else if (iconHeight > iconWidth) {
						width = (int) (height * ratio);
					}

					final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
							: Bitmap.Config.RGB_565;
					final Bitmap thumb = Bitmap.createBitmap(width, height, c);
					final Canvas canvas = new Canvas(thumb);
					canvas.setDrawFilter(new PaintFlagsDrawFilter(
							Paint.DITHER_FLAG, 0));
					// Copy the old bounds to restore them later
					// If we were to do oldBounds = icon.getBounds(),
					// the call to setBounds() that follows would
					// change the same instance and we would lose the
					// old bounds
					mOldBounds.set(icon.getBounds());
					icon.setBounds(0, 0, width, height);
					icon.draw(canvas);
					icon.setBounds(mOldBounds);
					icon = info.icon = new BitmapDrawable(thumb);
					info.filtered = true;
				}
			}

			final TextView textView = (TextView) convertView
					.findViewById(R.id.label);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null,
					null);
			textView.setText(info.title);

			return convertView;
		}
	}

	/**
	 * Shows and hides the applications grid view.
	 */
	private class ShowApplications implements View.OnClickListener {
		public void onClick(View v) {
			if (mGrid.getVisibility() != View.VISIBLE) {
				showApplications(true);
			} else {
				hideApplications();
			}
		}
	}

	/**
	 * Starts the selected activity/application in the grid view.
	 */
	private class ApplicationLauncher implements
			AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView parent, View v, int position,
				long id) {
			ApplicationInfo app = (ApplicationInfo) parent
					.getItemAtPosition(position);
			startActivity(app.intent);
		}
	}

	public void onClickBtn1(View v) {
		CLog.d(TAG, "onClickBtn1");
		//mReceiveEventImpl.onStopRTSP(null);
		this.doPlay("rtmp://aqm.runde.pro:1935/live/36147_1");
	}

	public void onClickBtn2(View v) {
		CLog.d(TAG, "onClickBtn2");
		//CapInfoManager.getInstance().getWifiListReqMsg(this);
        startService(new Intent(Home.this, CapRecordSerivice.class));
	}

	public void onClickBtn3(View v) {
		CLog.d(TAG, "onClickBtn3");
		//mReceiveEventImpl.onConnWifi(null);
		if (new File("/storage/sdcard1").exists()) {
			CLog.d(TAG, "/storage/sdcard1 is exists");
		} else {
			CLog.d(TAG, "/storage/sdcard1 isn't exists");
		}

		if (new File("/mnt/m_external_sd").exists()) {
			CLog.d(TAG, "/mnt/m_external_sd is exists");
		} else {
			CLog.d(TAG, "/mnt/m_external_sd isn't exists");
		}
	}
	
	public void onClickBtn4(View v) {
		CLog.d(TAG, "onClickBtn4");
		CapClientManager.getInstance().stopConnection();
	}

	private void updateStateInfo(String msg) {
		CLog.d(TAG, "updateStateInfo = " + msg);
		if (mStrings.size() == LOG_COUNT) {
			mStrings.remove(LOG_COUNT - 1);
		}
		mStrings.add(0, msg);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case 100:
				for (int ret : grantResults) {
					if (ret != PackageManager.PERMISSION_GRANTED) {
						return;
					}
				}
				break;
			default:
				break;
		}
	}

	private boolean checkPublishPermission() {
		if (Build.VERSION.SDK_INT >= 23) {
			List<String> permissions = new ArrayList<>();
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			}
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
				permissions.add(Manifest.permission.CAMERA);
			}
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
				permissions.add(Manifest.permission.RECORD_AUDIO);
			}
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
				permissions.add(Manifest.permission.READ_PHONE_STATE);
			}
			if (permissions.size() != 0) {
				ActivityCompat.requestPermissions(this,
						permissions.toArray(new String[0]),
						100);
				return false;
			}
		}

		return true;
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
		rtmpUrl = "rtmp://aqm.runde.pro:1935/live/36147_1";
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
//		String rtmpUrl = getIntent().getStringExtra("PUSH_URL");

		if (TextUtils.isEmpty(rtmpUrl) || (!rtmpUrl.trim().toLowerCase().startsWith("rtmp://"))) {
			Toast.makeText(getApplicationContext(), "推流地址不合法，目前支持rtmp推流!", Toast.LENGTH_SHORT).show();
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

	private void stopPublishRtmp() {
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
			Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
			if(event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL || event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL){
				stopPublishRtmp();
			}
		}

		if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT || event == TXLiveConstants.PUSH_ERR_INVALID_ADDRESS) {
			stopPublishRtmp();
		}
		else if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
			Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
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
