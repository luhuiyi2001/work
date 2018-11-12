package com.tencent.liteav.demo.cap;

import java.util.ArrayList;

import android.content.Context;

import com.google.gson.Gson;


public class CapInfoManager {

	private static final String TAG = "CapInfoManager";
	private static CapInfoManager sMgr;
	private Context mContext;
	
	public static CapInfoManager getInstance() {
		if (sMgr == null) {
			sMgr = new CapInfoManager();
		}
		return sMgr;
	}

	private CapInfoManager() {
		
	}
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	
	public String getLoginReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_LOGIN;
		req.device_id = "adaa-dadna-daa";
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_login\",\"device_id\":\"adaa-dadna-daa\",\"status\":false}";
	}
	
	public String getLocationReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_REPORT_LOCATION;
		req.user_id = "1";
		req.x_point = "123.4";
		req.y_point = "23.123";
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_report_location\",\"user_id\":\"1\",\"x_point\":\"123.4\",\"y_point\":\"23.123\",\"status\":false}";
	}
	
	public String getSosReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_SOS;
		req.device_id = "adaa-dadna-daa";
		req.x_point = "123.34";
		req.y_point = "23.12";
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_sos\",\"device_id\":\"adaa-dadna-daa\",\"x_point\":\"123.34\",\"y_point\":\"23.12\",\"status\":false}";
	}
	
	public String getWifiListReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_UPLOAD_WIFI_LIST;
		req.device_id = "adaa-dadna-daa";
		WifiInfo wifiInfo = new WifiInfo();
		wifiInfo.status = 0;
		wifiInfo.spot = "wifi1";
		wifiInfo.pwd = 1;
		wifiInfo.intensity = "123";
		if (req.wifi_list == null) {
			req.wifi_list = new ArrayList<WifiInfo>();
		}
		req.wifi_list.add(wifiInfo);
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_upload_wifi_list\",\"device_id\":\"adaa-dadna-daa\",\"wifi_list\":[{\"spot\":\"wifi1\",\"status\":0,\"pwd\":\"1\",\"intensity\":\"123\"}],\"status\":false}";
	}
	
	public String getWifiConnReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_REPORT_WIFI_CONNECT_STATUS;
		req.device_id = "adaa-dadna-daa";
		req.spot = "wifi1";
		req.status = false;
		req.msg = "密码错误！";
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_report_wifi_connect_status\",\"device_id\":\"adaa-dadna-daa\",\"spot\":\"wifi1\",\"status\":false,\"msg\":\"密码错误！\"}";
	}
}
