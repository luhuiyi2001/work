package com.tencent.liteav.demo.cap.manager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.liteav.demo.cap.common.CapConstants;


public class CapStateInfoManager {

	private static final String TAG = CapStateInfoManager.class.getSimpleName();
	private static CapStateInfoManager sMgr;
	private Context mContext;

	public static CapStateInfoManager getInstance() {
		if (sMgr == null) {
			sMgr = new CapStateInfoManager();
		}
		return sMgr;
	}

	private CapStateInfoManager() {
	}

	public void setContext(Context context) {
		mContext = context;
	}
	
	
	public void update(String msg) {
		if (mContext != null) {
			Intent intent = new Intent(CapConstants.ACTION_UPDATE_STATE_INFO);
			intent.putExtra(CapConstants.EXTRA_UPDATE_STATE_INFO, msg);
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
		}
	}

}
