package com.tencent.liteav.demo.cap.callback;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.liteav.demo.DemoApplication;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;

/**
 * Created by XingYue on 2018/11/17.
 */

public class PushRtmpRespCallback implements OnResponseCallback {
    public static final String TAG = PushRtmpRespCallback.class.getSimpleName();
//    private Context mContext;
//    public PushRtmpRespCallback(Context context) {
//        mContext = context;
//    }

    @Override
    public void onResponse(CapInfoResponse resp) {
        if (resp == null) {
            CLog.d(TAG, "resp == null || mContext == null");
            return;
        }
        if (CapConstants.RES_CMD_SERVER_PUSH_OPEN_RTSP.equals(resp.cmd)) {
            CLog.d(TAG, "onOpenRTSP = [ " + resp.push_url + " ]");
            Intent intent = new Intent(CapConstants.ACTION_START_PUBLISH);
            intent.putExtra(CapConstants.EXTRA_PUSH_URL, resp.push_url);
            LocalBroadcastManager.getInstance(DemoApplication.getApplication()).sendBroadcast(intent);
        } else if (CapConstants.RES_CMD_SERVER_PUSH_STOP_RTSP.equals(resp.cmd)) {
            CLog.d(TAG, "onStopRTSP");
            Intent intent = new Intent(CapConstants.ACTION_STOP_PUBLISH);
            LocalBroadcastManager.getInstance(DemoApplication.getApplication()).sendBroadcast(intent);
        }
    }
}
