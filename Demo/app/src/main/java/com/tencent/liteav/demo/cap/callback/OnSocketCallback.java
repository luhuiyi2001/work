package com.tencent.liteav.demo.cap.callback;

import com.tencent.liteav.demo.cap.socket.CapInfoResponse;

public interface OnSocketCallback {
	void onResponse(CapInfoResponse resp);
	void notifyConnected();
	void notifyDisconnected();
}
