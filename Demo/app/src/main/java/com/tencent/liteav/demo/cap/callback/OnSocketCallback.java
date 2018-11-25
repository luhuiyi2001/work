package com.tencent.liteav.demo.cap.callback;

public interface OnSocketCallback {
	void onReceived(String msg);
	void onConnected();
	void onUnconnect();
	void onClosed();
}
