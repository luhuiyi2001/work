package com.tencent.liteav.demo.cap.manager;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.callback.OnSocketCallback;
import com.tencent.liteav.demo.cap.socket.CapResponse;
import com.tencent.liteav.demo.cap.socket.CapSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CapSocketManager implements OnSocketCallback {

	private static final String TAG = CapSocketManager.class.getSimpleName();
	private static CapSocketManager sMgr;

	private ExecutorService mThreadPool;
	private CapSocket mClient;
	private List<OnResponseCallback> mRespCallbackList = new ArrayList<OnResponseCallback>();
	private List<OnConnectStateCallback> mConnectStateCallbackList = new ArrayList<OnConnectStateCallback>();
	private int mNullDataCount;

//	private Handler mHandler = new Handler(Looper.myLooper());
//	final Runnable mWaitTimeout = new Runnable() {
//		@Override public void run() {
//			CLog.d(TAG, "ReceiveTimeout");
//			reconnect();
//		}
//	};

	public static CapSocketManager getInstance() {
		if (sMgr == null) {
			sMgr = new CapSocketManager();
		}
		return sMgr;
	}

	private CapSocketManager() {
		mClient = new CapSocket();
		mClient.setOnReceiveMsgListener(this);
		// 根据CPU数目初始化线程池
		mThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * CapConfig.POOL_SIZE);
	}

	public void addOnResponseCallback(OnResponseCallback callback) {
		if (callback == null) {
			return;
		}
		mRespCallbackList.add(callback);
	}

	public void removeOnResponseCallback(OnResponseCallback callback) {
		if (callback == null) {
			return;
		}
		mRespCallbackList.remove(callback);
	}

	public void addOnConnectStateCallback(OnConnectStateCallback callback) {
		if (callback == null) {
			return;
		}
		mConnectStateCallbackList.add(callback);
	}

	public void removeOnConnectStateCallback(OnConnectStateCallback callback) {
		if (callback == null) {
			return;
		}
		mConnectStateCallbackList.remove(callback);
	}

//	public void reconnect() {
//		disconnect();
//		connect();
//	}

	public void connect() {
		CLog.d(TAG, "connect = " + (mClient == null ? "null" : mClient.isConnected()));
		startConnectThread();
	}

	public void disconnect() {
		CLog.d(TAG, "disconnect");
		try {
//			if (mThreadPool != null) {
//				mThreadPool.shutdownNow();
//				mThreadPool = null;
//			}
			mClient.close();
		} catch (Exception e) {
			e.printStackTrace();
			CLog.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onReceived(String msg) {
		CLog.d(TAG, "onReceived = " + msg);
		if (TextUtils.isEmpty(msg)) {
			mNullDataCount++;
			if (mNullDataCount > 10) {
				mNullDataCount = 0;
				disconnect();
			}
			return;
		}
//		mHandler.removeCallbacks(mWaitTimeout);
//		mHandler.postDelayed(mWaitTimeout, CapConfig.TIME_OUT);
		mNullDataCount = 0;
        try {
			CapResponse resp = new Gson().fromJson(msg, CapResponse.class);
			if (resp == null) {
				return;
			}
			for (int i = 0; i < mRespCallbackList.size(); i++) {
				mRespCallbackList.get(i).onResponse(resp);
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			CLog.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onConnected() {
		CLog.d(TAG, "onConnected");
		startReceiveThread();
		notifyConnected();
	}

	@Override
	public void onUnconnect() {

	}

	@Override
	public void onClosed() {
		notifyDisconnected();
	}

	public void onSend(final String req) {
		CLog.d(TAG, "onSend = " + req);
		if (TextUtils.isEmpty(req)) {
			return;
		}
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				mClient.send(req);
			}
		});
	}

	private void startConnectThread() {
		CLog.d(TAG, "startConnectThread");

		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				mClient.connect();
//			if (mClient.connect()) {
//				CLog.d(TAG, "Connect Success!");
//				startReceiveThread();
//				notifyConnected();
//			} else {
//				CLog.d(TAG, "Connect Failed!");
//			}
			}
		});
	}

	/**
	 * 创建接收线程
	 */
	private void startReceiveThread() {
		CLog.d(TAG, "startReceiveThread");
		// 记录创建对象时的时间
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				while(mClient.isConnected()) {
					mClient.receive();
				}
			}
		});
	}

	private void notifyConnected() {
		CLog.d(TAG, "notifyConnected");
//		mHandler.removeCallbacks(mWaitTimeout);
//		mHandler.postDelayed(mWaitTimeout, CapConfig.TIME_OUT);
		for (int i = 0; i < mConnectStateCallbackList.size(); i++) {
			mConnectStateCallbackList.get(i).notifyConnected();
		}
	}

	private void notifyDisconnected() {
		CLog.d(TAG, "notifyDisconnected");
		for (int i = 0; i < mConnectStateCallbackList.size(); i++) {
			mConnectStateCallbackList.get(i).notifyDisconnected();
		}
	}

	public interface OnResponseCallback {
		void onResponse(CapResponse resp);
	}

	public interface OnConnectStateCallback {
		void notifyConnected();
		void notifyUnconnect();
		void notifyDisconnected();
	}
}