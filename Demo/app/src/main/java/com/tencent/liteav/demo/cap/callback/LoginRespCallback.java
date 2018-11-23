package com.tencent.liteav.demo.cap.callback;

import com.google.gson.Gson;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.manager.CapClientManager;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;
import com.tencent.liteav.demo.cap.socket.CapInfoResponse;
import com.tencent.liteav.demo.cap.socket.UserInfo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by XingYue on 2018/11/17.
 */

public class LoginRespCallback implements OnResponseCallback {
    public static final String TAG = LoginRespCallback.class.getSimpleName();
    @Override
    public void onResponse(CapInfoResponse resp) {
        if (resp == null) {
            return;
        }
        if (!CapConstants.RES_CMD_CA_LOGIN.equals(resp.cmd)) {
            return;
        }
        CLog.d(TAG, "onLogin = [ " + resp.status + ", " + resp.msg + ", " + resp.data + " ]");
        if (resp.status == null || !resp.status) {
            CLog.e(TAG, "Login Failed");
            return;
        }
        saveLoginInfo(resp.data);
        requestUserSig(resp.data);
        CapClientManager.getInstance().startHeartbeatTimer();
    }

    public void saveLoginInfo(UserInfo userInfo) {
        CLog.d(TAG, "saveLoginInfo = [ " + userInfo + " ]");
        if (userInfo == null) {
            return;
        }
        CapSharedPrefMgr.getInstance().putUserID(userInfo.user_id);
        CapSharedPrefMgr.getInstance().putUserName(userInfo.user_name);
        CapSharedPrefMgr.getInstance().putUserImg(userInfo.user_img);
    }

    public void requestUserSig(UserInfo userInfo) {
        CLog.d(TAG, "requestUserSig = [ " + userInfo + " ]");
        if (userInfo == null) {
            return;
        }
        final Request request = new Request.Builder()
                .url(CapConfig.URL_REQ_USER_SIG + userInfo.user_id)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                CLog.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(final Call call, final okhttp3.Response response) throws IOException {
                String json = response.body().string();
                CLog.d(TAG, "onResponse = " + json);
                CapInfoResponse resp = new Gson().fromJson(json, CapInfoResponse.class);
                CapSharedPrefMgr.getInstance().putUserSig(resp.userSig);
            }
        });
    }
}
