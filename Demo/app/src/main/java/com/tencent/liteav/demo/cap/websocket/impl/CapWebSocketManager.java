package com.tencent.liteav.demo.cap.websocket.impl;

import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.liteav.demo.cap.callback.OnSocketCallback;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.socket.CapResponse;
import com.tencent.liteav.demo.cap.socket.CapSocket;
import com.tencent.liteav.demo.cap.websocket.ErrorResponse;
import com.tencent.liteav.demo.cap.websocket.IResponseDispatcher;
import com.tencent.liteav.demo.cap.websocket.MessageType;
import com.tencent.liteav.demo.cap.websocket.Response;
import com.tencent.liteav.demo.cap.websocket.ResponseDelivery;
import com.tencent.liteav.demo.cap.websocket.SocketListener;
import com.tencent.liteav.demo.cap.websocket.WebSocketSetting;
import com.tencent.liteav.demo.cap.websocket.WebSocketThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CapWebSocketManager extends CapSocketManager implements SocketListener {

	private static final String TAG = CapWebSocketManager.class.getSimpleName();

	private WebSocketThread mWebSocketThread;


	public CapWebSocketManager() {
		mWebSocketThread = new WebSocketThread(WebSocketSetting.getConnectUrl());
		mWebSocketThread.setSocketListener(this);
		mWebSocketThread.start();
	}

	public void destroy() {
		CLog.d(TAG, "destroy");
		mWebSocketThread.getHandler().sendEmptyMessage(MessageType.QUIT);
	}

	public void sendText(String text) {
		CLog.d(TAG, "sendText = " + text);
		if (mWebSocketThread.getHandler() == null) {
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.setErrorCode(3);
			errorResponse.setCause(new Throwable("WebSocket does not initialization!"));
			errorResponse.setRequestText(text);
			onSendMessageError(errorResponse);
		} else {
			Message message = mWebSocketThread.getHandler().obtainMessage();
			message.obj = text;
			message.what = MessageType.SEND_MESSAGE;
			mWebSocketThread.getHandler().sendMessage(message);
		}
	}

	public void disconnect() {
		CLog.d(TAG, "disconnect");
		if (mWebSocketThread.getHandler() == null) {
			onConnectError(new Throwable("WebSocket dose not ready"));
		} else {
			mWebSocketThread.getHandler().sendEmptyMessage(MessageType.DISCONNECT);
		}
	}

	public void onSend(final String req) {
		CLog.d(TAG, "onSend = " + req);
		if (TextUtils.isEmpty(req)) {
			return;
		}
		sendText(req);
	}

	/**
	 * 连接 WebSocket
	 */
	public void connect() {
		CLog.d(TAG, "reconnect");
		if (mWebSocketThread.getHandler() == null) {
			onConnectError(new Throwable("WebSocket dose not ready"));
		} else {
			mWebSocketThread.getHandler().sendEmptyMessage(MessageType.CONNECT);
		}
	}

	@Override
	public void onConnected() {
		CLog.d(TAG, "onConnected");
		notifyConnected();
	}

	@Override
	public void onConnectError(Throwable cause) {
		CLog.e(TAG, "onConnectError");
	}

	@Override
	public void onDisconnected() {
		CLog.d(TAG, "onDisconnected");
		notifyDisconnected();
	}

	@Override
	public void onMessageResponse(Response message) {
		CLog.d(TAG, "onMessageResponse");
		try {
			CapResponse resp = new Gson().fromJson(message.getResponseText(), CapResponse.class);
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
	public void onSendMessageError(ErrorResponse message) {
		CLog.e(TAG, "onSendMessageError");
	}
}
