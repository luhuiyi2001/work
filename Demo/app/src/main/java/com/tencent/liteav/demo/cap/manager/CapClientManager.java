package com.tencent.liteav.demo.cap.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.liteav.demo.cap.listener.OnReceiveEventListener;
import com.tencent.liteav.demo.cap.listener.OnReceiveMsgListener;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.socket.CapSocket;
import com.tencent.liteav.demo.cap.util.HeartbeatTimer;


public class CapClientManager implements OnReceiveMsgListener {

	private static final String TAG = "CapClientManager";
	
	private static CapClientManager sMgr;
	
	private ExecutorService mThreadPool;
	private HeartbeatTimer mBeatTimer;
	private long mLastReceiveTime = 0;
	private CapSocket mClient;
	private OnReceiveEventListener mListener;

	public static CapClientManager getInstance() {
		if (sMgr == null) {
			sMgr = new CapClientManager();
		}
		return sMgr;
	}

	public void setOnReceiveEventListener(OnReceiveEventListener listener) {
		mListener = listener;
	}

	private CapClientManager() {
		CLog.d(TAG, "CapClientManager");
		mClient = new CapSocket();
		mClient.setOnReceiveMsgListener(this);
	}
	
	public void onStart() {
		CLog.d(TAG, "onStart = " + (mClient == null ? "null" : mClient.isConnected()));
		
		if (mClient.isConnected()) {
			return;
		}
		if (mClient.isDisconnected()) {
			onStop();
		}
		startConnectThread();
	}

	@Override
	public void onReceived(String msg) {
		CLog.d(TAG, "onReceived = " + msg);
		if (TextUtils.isEmpty(msg)) {
			return;
		}

		mLastReceiveTime = System.currentTimeMillis();
        try {
			CapInfoResponse resp = new Gson().fromJson(msg, CapInfoResponse.class);
			if (CapConstants.RES_CMD_CA_LOGIN.equals(resp.cmd)) {
				if (mListener != null) {
					mListener.onLogin(resp);
				}
			} else if (CapConstants.RES_CMD_CA_REPORT_LOCATION.equals(resp.cmd)) {
				if (mListener != null) {
					mListener.onLocation(resp);
				}
			} else if (CapConstants.RES_CMD_CA_SOS.equals(resp.cmd)) {
				if (mListener != null) {
					mListener.onSOS(resp);
				}
			} else if (CapConstants.RES_CMD_SERVER_PUSH_OPEN_RTSP.equals(resp.cmd)) {
				if (mListener != null) {
					mListener.onOpenRTSP(resp);
				}
			} else if (CapConstants.RES_CMD_SERVER_PUSH_STOP_RTSP.equals(resp.cmd)) {
				if (mListener != null) {
					mListener.onStopRTSP(resp);
				}
			} else if (CapConstants.RES_CMD_SERVER_PULL_WIFI_LIST.equals(resp.cmd)) {
				if (mListener != null) {
					mListener.onPullWifiList(resp);
				}
			} else if (CapConstants.RES_CMD_SERVER_PUSH_CONNECT_WIFI.equals(resp.cmd)) {
				if (mListener != null) {
					mListener.onConnWifi(resp);
				}
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			CLog.e(TAG, e.getMessage());
		}
	}
	
	public void onSend(String req) {
		CLog.d(TAG, "onSend = " + req);
		if (TextUtils.isEmpty(req)) {
			return;
		}
		mClient.send(req);
	}

	private void startConnectThread() {
		CLog.d(TAG, "startConnectThread");
		// 根据CPU数目初始化线程池
		mThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * CapConfig.POOL_SIZE);
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (mClient.connect()) {
					CLog.d(TAG, "Connect Success!");
					mClient.send(CapInfoManager.getInstance().getLoginReqMsg());
					startReceiveThread();
				} else {
					CLog.d(TAG, "Connect Failed!");
				}
			}
		});
	}

	/**
	 * 创建接收线程
	 */
	private void startReceiveThread() {
		CLog.d(TAG, "startReceiveThread");
		// 记录创建对象时的时间
		mLastReceiveTime = System.currentTimeMillis();
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				while(true) {
					mClient.receive();
				}
			}
		});
	}

	/**
	 * 启动心跳
	 */
	public void startHeartbeatTimer() {
		CLog.d(TAG, "startHeartbeatTimer");
		if (mBeatTimer == null) {
			mBeatTimer = new HeartbeatTimer();
		}
		mBeatTimer.setOnScheduleListener(new HeartbeatTimer.OnScheduleListener() {
			@Override
			public void onSchedule() {
				long receiveDuration = System.currentTimeMillis() - mLastReceiveTime;
				CLog.d(TAG, "mBeatTimer onSchedule:" + receiveDuration);
				if (receiveDuration > CapConfig.TIME_OUT) {// 若超过十五秒都没收到我的心跳包，则认为对方不在线。
					CLog.d(TAG, "TIME_OUT");
					onStop();
					startConnectThread();
				} else {
					mClient.send(CapInfoManager.getInstance().getLocationReqMsg());
				}
			}

		});
		mBeatTimer.startTimer(CapConfig.HEARTBEAT_MSG_DURATION, CapConfig.HEARTBEAT_MSG_DURATION);
	}

	private void stopHeartbeatTimer() {
		CLog.d(TAG, "stopHeartbeatTimer");
		if (mBeatTimer != null) {
			mBeatTimer.exit();
			mBeatTimer = null;
		}
	}

	public void onStop() {
		CLog.d(TAG, "stopConnection");
		try {
			stopHeartbeatTimer();
			if (mThreadPool != null) {
				mThreadPool.shutdown();
				mThreadPool = null;
			}
			mClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
