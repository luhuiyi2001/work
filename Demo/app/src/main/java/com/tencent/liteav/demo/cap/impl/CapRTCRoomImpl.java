package com.tencent.liteav.demo.cap.impl;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.CapActivity;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.fragment.CapChatFragment;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;
import com.tencent.liteav.demo.cap.socket.CapResponse;
import com.tencent.liteav.demo.cap.socket.CapUser;
import com.tencent.liteav.demo.roomutil.commondef.LoginInfo;
import com.tencent.liteav.demo.roomutil.commondef.PusherInfo;
import com.tencent.liteav.demo.roomutil.commondef.RoomInfo;
import com.tencent.liteav.demo.rtcroom.IRTCRoomListener;
import com.tencent.liteav.demo.rtcroom.RTCRoom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.tencent.liteav.demo.rtcroom.RTCRoom.*;

public class CapRTCRoomImpl {
    private static final String TAG = CapRTCRoomImpl.class.getSimpleName();

    private RTCRoom mRTCRoom;
    private String mUserId;
    private String mUserName;
    private Runnable retryInitRoomRunnable;
    private String mRoomID;
    private ArrayList<String> mUserIDs;
    private boolean mIsBtnCall;
    private boolean mIsAudioChat;

    private CapActivity mActivity;
    public CapRTCRoomImpl(CapActivity context) {
        mActivity = context;
    }

    public void create() {
        CLog.d(TAG, "create");
        mRTCRoom = new RTCRoom(mActivity.getApplicationContext());
        mRTCRoom.setRTCRoomListener(new MemberEventListener());
    }

    public void destroy() {
        CLog.d(TAG, "destroy");
        mRTCRoom.setRTCRoomListener(null);
        mRTCRoom.logout();
    }

    public void onStartChat(String roomId, ArrayList<String> userIDs, boolean isBtnCall, boolean isAudioChat) {
        mIsAudioChat = isAudioChat;
        onStartChat(roomId, userIDs, isBtnCall);
    }

    private void onStartChat(String roomId, ArrayList<String> userIDs, boolean isBtnCall) {
        mRoomID = roomId;
        mUserIDs = userIDs;
        mIsBtnCall = isBtnCall;
        if (TextUtils.isEmpty( CapSharedPrefMgr.getInstance().getUserSig())) {
            requestUserSig();
            return;
        }
        internalInitializeRTCRoom();
    }

    public void onStopChat() {
        Fragment fragment = mActivity.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof CapChatFragment && fragment.isVisible()){
            ((CapChatFragment) fragment).onBackPressed();
        }
    }

    private void requestUserSig() {
        String userID = CapSharedPrefMgr.getInstance().getUserID();
        final Request request = new Request.Builder()
                .url(CapConfig.URL_REQ_USER_SIG + userID)
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
                CapResponse resp = new Gson().fromJson(json, CapResponse.class);
                CapSharedPrefMgr.getInstance().putUserSig(resp.userSig);
                internalInitializeRTCRoom();
            }
        });
    }

    private void internalInitializeRTCRoom() {
        LoginInfo loginInfo       = new LoginInfo();
        loginInfo.userID         = CapSharedPrefMgr.getInstance().getUserID();
        loginInfo.userSig        = CapSharedPrefMgr.getInstance().getUserSig();
        loginInfo.userName       = CapSharedPrefMgr.getInstance().getUserName();
        loginInfo.userAvatar     = CapSharedPrefMgr.getInstance().getUserImg();
        CLog.e(TAG, "userSig = [ " + loginInfo.userSig + " ]");
        CLog.e(TAG, "userInfo = [ " + loginInfo.userID + ", " + loginInfo.userName + ", " + loginInfo.userAvatar + " ]");
        mRTCRoom.login(CapConfig.URL_LOGIN_RTC_ROOM, loginInfo, new LoginCallback() {
            @Override
            public void onError(int errCode, String errInfo) {
                CLog.e(TAG, "RTCRoom Login Error = [ " + errCode + ", " + errInfo + " ]");
                CLog.e(TAG, "URL = [ " + CapConfig.URL_LOGIN_RTC_ROOM + " ]");
                mActivity.setTitle(errInfo);
                CLog.e(TAG, String.format("[Activity]RTCRoom初始化失败：{%s}", errInfo));
//                mActivity.printGlobalLog(String.format("[Activity]RTCRoom初始化失败：{%s}", errInfo));
                retryInitRoomRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(RTCMultiRoomActivity.this, "重试...", Toast.LENGTH_SHORT).show();
                        //initializeRTCRoom();
                    }
                };
            }

            @Override
            public void onSuccess(String userId) {
                CLog.e(TAG, "RTCRoom Login Success");
                mActivity.setTitle("多人音视频");
                mUserId = userId;
                mUserName = CapSharedPrefMgr.getInstance().getUserName();
                CLog.d(TAG, String.format("[Activity]初始化成功,userID{%s}", userId));
//                mActivity.printGlobalLog("[Activity]初始化成功,userID{%s}", userId);

                createRoom();
            }
        });
    }

    public RTCRoom getRTCRoom() {
        return mRTCRoom;
    }

    public String getUserID() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    private void createRoom() {
        RoomInfo roomInfo = new RoomInfo();
        roomInfo.roomInfo = CapUtils.getImei();
        roomInfo.roomID = mRoomID;
        enterRoom(roomInfo, getUserID(), TextUtils.isEmpty(roomInfo.roomID) ? true : false, mUserIDs, mIsBtnCall, mIsAudioChat);
    }

    private void enterRoom(final RoomInfo roomInfo, final String userID, final boolean requestCreateRoom, final ArrayList<String> userIds, final boolean isBtnCall, final boolean isAudioChat) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.replaceFragement(CapChatFragment.newInstance(roomInfo, userID, requestCreateRoom, userIds, isBtnCall, isAudioChat));
            }
        });
    }

    private final class MemberEventListener implements IRTCRoomListener {

        @Override
        public void onPusherJoin(PusherInfo member) {
            Fragment fragment = mActivity.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof CapChatFragment && fragment.isVisible()){
                ((CapChatFragment) fragment).onPusherJoin(member);
            }
        }

        @Override
        public void onPusherQuit(PusherInfo member) {
            Fragment fragment = mActivity.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof CapChatFragment && fragment.isVisible()){
                ((CapChatFragment) fragment).onPusherQuit(member);
            }
        }

        @Override
        public void onRoomClosed(String roomId) {
            Fragment fragment = mActivity.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof CapChatFragment && fragment.isVisible()){
                ((CapChatFragment) fragment).onRoomClosed(roomId);
            }
        }

        @Override
        public void onDebugLog(String line) {
            CLog.d(TAG, line);
//            mActivity.printGlobalLog(line);
        }

        @Override
        public void onGetPusherList(List<PusherInfo> pusherInfoList) {
            for (PusherInfo pusherInfo : pusherInfoList) {
                onPusherJoin(pusherInfo);
            }
        }

        @Override
        public void onRecvRoomTextMsg(String roomid, String userid, String userName, String userAvatar, String msg) {
            //do nothing
        }

        @Override
        public void onRecvRoomCustomMsg(final String roomID, final String userID, final String userName, final String userAvatar, final String cmd, final String message) {
            //do nothing
        }

        @Override
        public void onError(final int errorCode, final String errorMessage) {
            Fragment fragment = mActivity.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof CapChatFragment && fragment.isVisible()){
                ((CapChatFragment) fragment).onError(errorCode, errorMessage);
            }
        }
    }
}
