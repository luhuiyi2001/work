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

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.callback.PushRtmpRespCallback;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.impl.CapLivePusherImpl;
import com.tencent.liteav.demo.cap.impl.CapVideoRecordImpl;
import com.tencent.liteav.demo.cap.listener.CapReceiveEventImpl;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.rtcroom.ui.multi_room.RTCMultiRoomActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Home extends Activity {
	private static final String TAG = Home.class.getSimpleName();

	private CapLivePusherImpl mLivePusherImpl;
	private CapVideoRecordImpl mRecorderImpl;
	//private CapNetWorkStateReceiver mNetStateReceiver = new CapNetWorkStateReceiver();
	private PushRtmpRespCallback mPushRtmpRespCallback;


	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mStrings = new ArrayList<String>(10);
    public static final int LOG_COUNT = 18;
    private CapReceiveEventImpl mReceiveEventImpl;
    private LoginBroadcastReceiver mReceiver = new LoginBroadcastReceiver();
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
				mLivePusherImpl.doPlay(url);
			} else if (CapConstants.ACTION_STOP_PUBLISH.equals(action)) {
				mLivePusherImpl.stopPublishRtmp();
			}
		}
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.home);
		registerIntentReceivers();

		mListView = (ListView) findViewById(R.id.list_info);
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrings);
		mListView.setAdapter(mAdapter);
		mReceiveEventImpl =  new CapReceiveEventImpl(this);
		CapClientManager.getInstance().setOnReceiveEventListener(mReceiveEventImpl);

		checkPublishPermission();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mLivePusherImpl = new CapLivePusherImpl(this);
		mLivePusherImpl.initView();
		mRecorderImpl = new CapVideoRecordImpl(this);
		mRecorderImpl.initView();
		mPushRtmpRespCallback = new PushRtmpRespCallback();
		CapClientManager.getInstance().addOnResponseCallback(mPushRtmpRespCallback);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterIntentReceivers();
		mLivePusherImpl.destroy();
		mRecorderImpl.destroy();
		CapClientManager.getInstance().removeOnResponseCallback(mPushRtmpRespCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		CapClientManager.getInstance().onStart();
		mLivePusherImpl.resume();
	}

	@Override
	public void onStop(){
		super.onStop();
		mLivePusherImpl.stop();
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	/**
	 * Registers various intent receivers. The current implementation registers
	 * only a wallpaper intent receiver to let other applications change the
	 * wallpaper.
	 */
	private void registerIntentReceivers() {
		IntentFilter intentFilter = new IntentFilter(CapConstants.ACTION_UPDATE_STATE_INFO);
        intentFilter.addAction(CapConstants.ACTION_START_PUBLISH);
        intentFilter.addAction(CapConstants.ACTION_STOP_PUBLISH);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,intentFilter);

//		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		//registerReceiver(mNetStateReceiver, filter);
	}

	private void unregisterIntentReceivers() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
		//unregisterReceiver(mNetStateReceiver);
	}

	private boolean isBtn1Start = false;
	public void onClickBtn1(View v) {
		CLog.d(TAG, "onClickBtn1");
		//mReceiveEventImpl.onStopRTSP(null);
		//mLivePusherImpl.doPlay("rtmp://aqm.runde.pro:1935/live/36147_1");
		if (isBtn1Start) {
			CapClientManager.getInstance().onStop();
			((Button)v).setText("StartSocket");
		} else {
			CapClientManager.getInstance().onStart();
			((Button)v).setText("StopSocket");
		}
		isBtn1Start = !isBtn1Start;
	}

	private boolean isBtn2Start = false;
	public void onClickBtn2(View v) {
		CLog.d(TAG, "onClickBtn2");
		//CapInfoManager.getInstance().getWifiListReqMsg(this);
        //startService(new Intent(Home.this, CapRecordSerivice.class));
		if (isBtn2Start) {
			this.mRecorderImpl.stopRecord();
			((Button)v).setText("StartRecord");
		} else {
			this.mRecorderImpl.startRecord();
			((Button)v).setText("StopRecord");
		}
		isBtn2Start = !isBtn2Start;
	}

	public void onClickBtn3(View v) {
		CLog.d(TAG, "onClickBtn3 =" + CapUtils.getImei());
		//mReceiveEventImpl.onConnWifi(null);

		if (new File("/mnt/m_external_sd").exists()) {
			CLog.d(TAG, "/mnt/m_external_sd is exists");
		} else {
			CLog.d(TAG, "/mnt/m_external_sd isn't exists");
		}
	}
	
	public void onShowApps(View v) {
		CLog.d(TAG, "onClickBtn4");
		//CapClientManager.getInstance().stopConnection();
        Intent intent = new Intent(Home.this, RTCMultiRoomActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
}
