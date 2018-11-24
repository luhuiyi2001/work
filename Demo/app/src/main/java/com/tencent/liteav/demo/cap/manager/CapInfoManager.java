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

	public void setContext(Context context) {
		mContext = context;
	}
	
	
	public String getLoginReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_LOGIN;
		req.device_id = CapUtils.getImei();
		return new Gson().toJson(req);
	}
	
	public String getLocationReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_REPORT_LOCATION;
		req.user_id = CapSharedPrefMgr.getInstance().getUserID();
		req.x_point = CapSharedPrefMgr.getInstance().getLatitude();
		req.y_point = CapSharedPrefMgr.getInstance().getLongitude();
		return new Gson().toJson(req);
	}
	
	public String getSosReqMsg() {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_SOS;
		req.device_id = CapUtils.getImei();
		req.x_point = CapSharedPrefMgr.getInstance().getLatitude();
		req.y_point = CapSharedPrefMgr.getInstance().getLongitude();
		return new Gson().toJson(req);
	}
	
	public String getWifiListReqMsg(List<WifiInfo> wifiList) {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_UPLOAD_WIFI_LIST;
		req.device_id = CapUtils.getImei();
		req.wifi_list = wifiList;
		return new Gson().toJson(req);
	}
	
	public String getWifiConnStateReqMsg(String ssid, boolean isSuccess) {
		CapInfoRequest req = new CapInfoRequest();
		req.act = CapConstants.REQ_ACT_CA_REPORT_WIFI_CONNECT_STATUS;
		req.device_id = CapUtils.getImei();
		req.spot = ssid;
		req.status = isSuccess;
		req.msg = isSuccess ? "连接成功" : "密码错误";
		return new Gson().toJson(req);
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
