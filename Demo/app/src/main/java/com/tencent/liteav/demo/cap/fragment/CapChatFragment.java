package com.tencent.liteav.demo.cap.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.callback.CapActivityInterface;
import com.tencent.liteav.demo.cap.manager.CapAudioManager;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.roomutil.commondef.PusherInfo;
import com.tencent.liteav.demo.roomutil.commondef.RoomInfo;
import com.tencent.liteav.demo.rtcroom.IRTCRoomListener;
import com.tencent.liteav.demo.rtcroom.RTCRoom;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.ArrayList;
import java.util.List;

public class CapChatFragment extends Fragment implements IRTCRoomListener {

    private static final String TAG = CapChatFragment.class.getSimpleName();

    private Activity                            mActivity;
    private CapActivityInterface mActivityInterface;
    private Handler mHandler = new Handler();

    private RoomInfo                            mRoomInfo;
    private ArrayList<String>                   mUserIDs;
    private List<RoomVideoView>                 mPlayerViews    = new ArrayList<>();
    private boolean                             mIsBtnCall;

    private int                                 mBeautyStyle    = TXLiveConstants.BEAUTY_STYLE_SMOOTH;
    private int                                 mBeautyLevel    = 5;
    private int                                 mWhiteningLevel = 5;
    private int                                 mRuddyLevel     = 5;
    private List<String> mPusherList = new ArrayList<String>();

    final Runnable mPusherJoinTimeout = new Runnable() {
        @Override public void run() {
            CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getReportRoomStatusMsg(mRoomInfo.roomID, "-1", null));
            onBackPressed();
        }
    };

    final Runnable mAllPusherExitTimeout = new Runnable() {
        @Override public void run() {
            CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getReportRoomStatusMsg(mRoomInfo.roomID, "-2", null));
            onBackPressed();
        }
    };

    public static CapChatFragment newInstance(RoomInfo config, String userID, boolean createRoom, ArrayList<String> userIds, boolean isBtnCall) {
        CLog.d(TAG, "newInstance");
        CapChatFragment fragment = new CapChatFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("roomInfo", config);
        bundle.putString("userID", userID);
        bundle.putBoolean("createRoom", createRoom);
        bundle.putStringArrayList("userIDs", userIds);
        bundle.putBoolean("btnCall", isBtnCall);
        fragment.setArguments(bundle);
        return fragment;
    }

    /***********************************************************************************************************************************************
     *
     *                                                      Fragment生命周期函数调用顺序
     *
     *     onAttach() --> onCreateView() --> onActivityCreated() --> onResume() --> onPause() --> onDestroyView() --> onDestroy() --> onDetach()
     *
     ***********************************************************************************************************************************************/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CLog.d(TAG, "onAttach");
        mActivity = ((Activity) context);
        mActivityInterface = ((CapActivityInterface) context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        CLog.d(TAG, "onAttach");
        mActivity = ((Activity) activity);
        mActivityInterface = ((CapActivityInterface) activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rtc_multi_room_chat, container, false);
        CLog.d(TAG, "onCreateView");
        TXCloudVideoView views[] = new TXCloudVideoView[4];
        views[0] = ((TXCloudVideoView) view.findViewById(R.id.rtmproom_video_0));
        views[1] = ((TXCloudVideoView) view.findViewById(R.id.rtmproom_video_1));
        views[2] = ((TXCloudVideoView) view.findViewById(R.id.rtmproom_video_2));
        views[3] = ((TXCloudVideoView) view.findViewById(R.id.rtmproom_video_3));

        TextView nameViews[] = new TextView[4];
        nameViews[0] = ((TextView) view.findViewById(R.id.rtmproom_video_name_0));
        nameViews[1] = ((TextView) view.findViewById(R.id.rtmproom_video_name_1));
        nameViews[2] = ((TextView) view.findViewById(R.id.rtmproom_video_name_2));
        nameViews[3] = ((TextView) view.findViewById(R.id.rtmproom_video_name_3));

        for (int i = 0; i < 4; i++) {
            mPlayerViews.add(new RoomVideoView(views[i], nameViews[i]));
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CLog.d(TAG, "onActivityCreated");
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getArguments();
        mRoomInfo            = bundle.getParcelable("roomInfo");
        String  selfUserID   = bundle.getString("userID");
        String  selfUserName = CapSharedPrefMgr.getInstance().getUserName();
        boolean createRoom   = bundle.getBoolean("createRoom");
        mUserIDs             = bundle.getStringArrayList("userIDs");
        mIsBtnCall           = bundle.getBoolean("btnCall");

        if (selfUserID == null || ( !createRoom && mRoomInfo == null)) {
            return;
        }

        mActivityInterface.setTitle(mRoomInfo.roomInfo);

        RoomVideoView videoView = applyVideoView(selfUserID, "我("+selfUserName+")");
        if (videoView == null) {
            CLog.e(TAG, String.format("申请 UserID {%s} 返回view 为空", selfUserID));
//            mActivityInterface.printGlobalLog("申请 UserID {%s} 返回view 为空", selfUserID);
            return;
        }

        mActivityInterface.getRTCRoom().startLocalPreview(videoView.videoView);
        mActivityInterface.getRTCRoom().setPauseImage(BitmapFactory.decodeResource(getResources(), R.drawable.pause_publish));
        mActivityInterface.getRTCRoom().setBitrateRange(200, 400);
        mActivityInterface.getRTCRoom().setBeautyFilter(mBeautyStyle, mBeautyLevel, mWhiteningLevel, mRuddyLevel);
        mActivityInterface.getRTCRoom().setMirror(true);

        if (createRoom){
            mActivityInterface.getRTCRoom().createRoom("", mRoomInfo.roomInfo, new RTCRoom.CreateRoomCallback() {
                @Override
                public void onSuccess(String roomId) {
                    CLog.i(TAG, "roomId : " + roomId);
                    mRoomInfo.roomID = roomId;
                    if (mIsBtnCall) {
                        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getBtnCallMsg(roomId));
                    } else {
                        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getCreateRoomMsg(roomId, mUserIDs));
                    }

                    mHandler.postDelayed(mPusherJoinTimeout, 60000);
                    CapAudioManager.getInstance().playReceivedVoice();
                }

                @Override
                public void onError(int errCode, String e) {
                    errorGoBack("创建会话错误", errCode, e);
                }
            });
        }
        else {
            mActivityInterface.getRTCRoom().enterRoom(mRoomInfo.roomID, new RTCRoom.EnterRoomCallback() {
                @Override
                public void onError(int errCode, String errInfo) {
                    errorGoBack("进入会话错误", errCode, errInfo);
                }

                @Override
                public void onSuccess() {
                    CapAudioManager.getInstance().playReceivedVoice();
                }
            });
        }

        CLog.i(TAG, "roomInfo = " + mRoomInfo.roomID);
    }

    @Override
    public void onResume() {
        super.onResume();
        CLog.d(TAG, "onResume");
        mActivityInterface.getRTCRoom().switchToForeground();
    }

    @Override
    public void onPause() {
        super.onPause();
        CLog.d(TAG, "onPause");
        mActivityInterface.getRTCRoom().switchToBackground();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CLog.d(TAG, "onDestroyView");
        mPlayerViews.clear();
        mActivityInterface.getRTCRoom().stopLocalPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.d(TAG, "onDestroy");
        recycleVideoView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        CLog.d(TAG, "onDetach");
        mActivity = null;
        mActivityInterface = null;
    }

    public void onBackPressed() {
        CLog.d(TAG, "onBackPressed");
        if (mActivityInterface != null) {
            mActivityInterface.getRTCRoom().exitRoom(new RTCRoom.ExitRoomCallback() {
                @Override
                public void onSuccess() {
                    CLog.i(TAG, "exitRoom Success");
                }

                @Override
                public void onError(int errCode, String e) {
                    CLog.e(TAG, "exitRoom failed, errorCode = " + errCode + " errMessage = " + e);
                }
            });
        }
        recycleVideoView();
        backStack();
    }

    private void errorGoBack(String title, int errCode, String errInfo){
        CLog.e(TAG, "ErrorInfo : [ " + title + ", " + errCode + ", " + errInfo + " ]");
        mActivityInterface.getRTCRoom().exitRoom(null);
        recycleVideoView();
        backStack();
    }

    private void backStack(){
        CLog.d(TAG, "backStack");
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mActivity != null) {
                        FragmentManager fm = mActivity.getFragmentManager();
                        FragmentTransaction ts = fm.beginTransaction();
                        ts.remove(CapChatFragment.this);
                        ts.commit();
//                        FragmentManager fragmentManager = mActivity.getFragmentManager();
//                        fragmentManager.popBackStack();
//                        fragmentManager.beginTransaction().commit();
                        mActivityInterface.backToStartRecord();
                    }
                }
            });
        }
    }

    @Override
    public void onGetPusherList(List<PusherInfo> pusherInfoList) {
        //do nothing
        CLog.d(TAG, "onGetPusherList");
    }

    @Override
    public void onPusherJoin(final PusherInfo pusher) {
        if (pusher == null || pusher.userID == null) {
            return;
        }
        mPusherList.add(pusher.userID);
        mHandler.removeCallbacks(mPusherJoinTimeout);
        mHandler.removeCallbacks(mAllPusherExitTimeout);
        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getReportRoomStatusMsg(mRoomInfo.roomID, "0", pusher.userID));
        RoomVideoView videoView = applyVideoView(pusher.userID, pusher.userName == null ? pusher.userID : pusher.userName);
        if (videoView != null)  {
            mActivityInterface.getRTCRoom().addRemoteView(videoView.videoView, pusher, new RTCRoom.RemoteViewPlayCallback() {
                @Override
                public void onPlayBegin() {
                }

                @Override
                public void onPlayError() {
                    onPusherQuit(pusher);
                }
            }); //开启远端视频渲染
        }
    }

    @Override
    public void onPusherQuit(PusherInfo pusher) {
        CLog.d(TAG, "onPusherQuit");
        mActivityInterface.getRTCRoom().deleteRemoteView(pusher);//关闭远端视频渲染
        recycleVideoView(pusher.userID);
        mPusherList.remove(pusher.userID);
        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getReportRoomStatusMsg(mRoomInfo.roomID, "1", pusher.userID));
        if (mPusherList.size() == 0) {
            mHandler.postDelayed(mAllPusherExitTimeout, 30000);
        }
    }

    @Override
    public void onRoomClosed(String roomId) {
        CLog.d(TAG, "onRoomClosed = " + roomId);
        boolean createRoom = getArguments().getBoolean("createRoom");
        if (createRoom == false) {
            CLog.e(TAG, "onRoomClosed : [ " + roomId + ", " + String.format("会话【%s】解散了", mRoomInfo != null ? mRoomInfo.roomInfo : "null") + " ]");
            onBackPressed();
        }
    }

    @Override
    public void onRecvRoomTextMsg(String roomid, String userid, String userName, String userAvatar, String msg) {
        CLog.d(TAG, "onRecvRoomTextMsg");
    }

    @Override
    public void onRecvRoomCustomMsg(final String roomID, final String userID, final String userName, final String userAvatar, final String cmd, final String message) {
        CLog.d(TAG, "onRecvRoomCustomMsg");
    }

    @Override
    public void onDebugLog(String line) {
        CLog.d(TAG, "line");
    }

    @Override
    public void onError(final int errorCode, final String errorMessage) {
        errorGoBack("会话错误", errorCode, errorMessage);
    }

    private class RoomVideoView {
        TXCloudVideoView  videoView;
        TextView          titleView;
        String            userID   = "";
        String            userName = "";
        boolean           isUsed   = false;

        public RoomVideoView(TXCloudVideoView view, TextView titleView) {
            this.videoView = view;
            this.videoView.setVisibility(View.GONE);
            this.titleView = titleView;
            this.titleView.setText("");
            this.isUsed = false;
        }

        private void setUsed(boolean used){
            videoView.setVisibility(used ? View.VISIBLE : View.GONE);
            titleView.setVisibility(used ? View.VISIBLE : View.GONE);
            titleView.setText(used ? userName : "");
            this.isUsed = used;
        }

    }

    public synchronized RoomVideoView applyVideoView(String id, String name){
        if (id == null) {
            return null;
        }

        for (RoomVideoView videoView : mPlayerViews) {
            if (!videoView.isUsed) {
                videoView.userName = name;
                videoView.userID = id;
                videoView.setUsed(true);
                return videoView;
            }
            else {
                if (videoView.userID != null && videoView.userID.equals(id)){
                    videoView.userName = name;
                    videoView.setUsed(true);
                    return videoView;
                }
            }
        }
        return null;
    }

    public synchronized void recycleVideoView(String id){
        for (RoomVideoView item : mPlayerViews) {
            if (item.userID != null && item.userID.equals(id)){
                item.userID = null;
                item.userName = "";
                item.setUsed(false);
            }
        }
    }

    public synchronized void recycleVideoView(){
        for (RoomVideoView item : mPlayerViews) {
            item.userID = null;
            item.userName = "";
            item.setUsed(false);
        }
    }
}
