package com.tencent.liteav.demo.cap;

public interface OnReceiveEventListener {
	void onLogin(CapInfoResponse resp);
	void onSOS(CapInfoResponse status);
	void onLocation(CapInfoResponse status);
	void onOpenRTSP(CapInfoResponse status);
	void onStopRTSP(CapInfoResponse status);
	void onPullWifiList(CapInfoResponse status);
	void onConnWifi(CapInfoResponse status);
}
