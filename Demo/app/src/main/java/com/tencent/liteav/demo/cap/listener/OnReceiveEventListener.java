package com.tencent.liteav.demo.cap.listener;

import com.tencent.liteav.demo.cap.socket.CapInfoResponse;

public interface OnReceiveEventListener {
	void onLogin(CapInfoResponse resp);
	void onSOS(CapInfoResponse resp);
	void onLocation(CapInfoResponse resp);
	void onOpenRTSP(CapInfoResponse resp);
	void onStopRTSP(CapInfoResponse resp);
	void onPullWifiList(CapInfoResponse resp);
	void onConnWifi(CapInfoResponse resp);
}
