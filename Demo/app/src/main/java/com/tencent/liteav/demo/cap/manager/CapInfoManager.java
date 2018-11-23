package com.tencent.liteav.demo.cap.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.google.gson.Gson;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.socket.CapInfoRequest;
import com.tencent.liteav.demo.cap.socket.WifiInfo;
import com.tencent.liteav.demo.cap.wifi.WifiAdmin;


public class CapInfoManager {

	private static final String TAG = CapInfoManager.class.getSimpleName();
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

//	private WifiAdmin mWifiMgr = null;

	public void setContext(Context context) {
		mContext = context;
//		mWifiMgr = new WifiAdmin(context);
	}
	
	
	public String getLoginReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_LOGIN;
		req.device_id = CapUtils.getImei();
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_login\",\"device_id\":\"adaa-dadna-daa\",\"status\":false}";
	}
	
	public String getLocationReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_REPORT_LOCATION;
		req.user_id = CapSharedPrefMgr.getInstance().getUserID();
		req.x_point = CapSharedPrefMgr.getInstance().getLatitude();
		req.y_point = CapSharedPrefMgr.getInstance().getLongitude();
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_report_location\",\"user_id\":\"1\",\"x_point\":\"123.4\",\"y_point\":\"23.123\",\"status\":false}";
	}
	
	public String getSosReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_SOS;
		req.device_id = CapUtils.getImei();
		req.x_point = "123.34";
		req.y_point = "23.12";
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_sos\",\"device_id\":\"adaa-dadna-daa\",\"x_point\":\"123.34\",\"y_point\":\"23.12\",\"status\":false}";
	}
	
	public String getWifiListReqMsg(Context context) {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_UPLOAD_WIFI_LIST;
		req.device_id = CapUtils.getImei();
//		WifiInfo wifiInfo = new WifiInfo();
//		wifiInfo.status = 0;
//		wifiInfo.spot = "wifi1";
//		wifiInfo.pwd = 1;
//		wifiInfo.intensity = "123";
//		if (req.wifi_list == null) {
//			req.wifi_list = new ArrayList<WifiInfo>();
//		}
//		req.wifi_list.add(wifiInfo);
		req.wifi_list = getWifiList(context);
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_upload_wifi_list\",\"device_id\":\"adaa-dadna-daa\",\"wifi_list\":[{\"spot\":\"wifi1\",\"status\":0,\"pwd\":\"1\",\"intensity\":\"123\"}],\"status\":false}";
	}
	
	public String getWifiConnReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_REPORT_WIFI_CONNECT_STATUS;
		req.device_id = CapUtils.getImei();
		req.spot = "wifi1";
		req.status = false;
		req.msg = "密码错误！";
		return new Gson().toJson(req);
		//return "{\"act\":\"ca_report_wifi_connect_status\",\"device_id\":\"adaa-dadna-daa\",\"spot\":\"wifi1\",\"status\":false,\"msg\":\"密码错误！\"}";
	}

	private List<WifiInfo> getWifiList(Context context) {
//		if (mWifiMgr == null) {
//			return null;
//		}
		WifiAdmin mWifiMgr = new WifiAdmin(context);
		mWifiMgr.openWifi();
		mWifiMgr.startScan();
		CLog.i(TAG, "getWifiList = " + mWifiMgr.lookUpScan().toString());
		List<ScanResult> scanWifiList = mWifiMgr.getWifiList();
		if (scanWifiList.size() == 0) {
			return null;
		}
		List<WifiInfo> wifiList = new ArrayList<WifiInfo>();
		for (int i = 0; i < scanWifiList.size(); i++) {
			ScanResult curResult = scanWifiList.get(i);
			WifiInfo wifiInfo = new WifiInfo();
			wifiInfo.spot = curResult.SSID;
			wifiInfo.pwd = curResult.capabilities.contains("WPA") ? 1 : 0;
			wifiInfo.intensity = WifiManager.calculateSignalLevel(curResult.level, 5);
			wifiInfo.status = mWifiMgr.isExsits(curResult.SSID) != null ? 1 : 0;
			wifiList.add(wifiInfo);
		}
		return wifiList;
	}

	public String getCreateRoomMsg(String roomId, ArrayList<String> userIDs) {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_CREATE_ROOM_FOR_HELP;
		req.room_id = roomId;
		req.user_ids = userIDs;
		return new Gson().toJson(req);
	}

	public String getReportRoomStatusMsg(String roomId, String type, String userID) {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_REPORT_ROOM_STATUS;
		req.room_id = roomId;
		req.type = type;
		req.user_id = userID;
		return new Gson().toJson(req);
	}
}
