package com.tencent.liteav.demo.rtcroom.ui.multi_room;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.common.CapConfig;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;
import com.tencent.liteav.demo.roomutil.commondef.PusherInfo;
import com.tencent.liteav.demo.roomutil.commondef.LoginInfo;
import com.tencent.liteav.demo.rtcroom.IRTCRoomListener;
import com.tencent.liteav.demo.rtcroom.RTCRoom;
import com.tencent.liteav.demo.common.misc.CommonAppCompatActivity;
import com.tencent.liteav.demo.common.misc.NameGenerator;
import com.tencent.liteav.demo.rtcroom.ui.multi_room.fragment.RTCMultiRoomListFragment;
import com.tencent.liteav.demo.rtcroom.ui.multi_room.fragment.RTCMultiRoomChatFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.logging.HttpLoggingInterceptor;

public class RTCMultiRoomActivity extends CommonAppCompatActivity implements RTCMultiRoomActivityInterface {

    private static final String TAG = RTCMultiRoomActivity.class.getSimpleName();

    public  Handler         uiHandler  = new Handler();
    
    private RTCRoom         RTCRoom;
    private String          userId = "";
    private String          userName = "RundeCap";
    private String          userAvatar = "RundeCapAvatar";
    private TextView        titleTextView;
    private TextView        globalLogTextview;
    private ScrollView      globalLogTextviewContainer;
    private Runnable        retryInitRoomRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_rtc_multi_room);

        findViewById(R.id.rtc_multi_room_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        titleTextView = ((TextView) findViewById(R.id.rtc_mutil_room_title_textview));
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (retryInitRoomRunnable != null) {
                    synchronized (RTCMultiRoomActivity.this) {
                        retryInitRoomRunnable.run();
                        retryInitRoomRunnable = null;
                    }
                }
            }
        });

        globalLogTextview = ((TextView) findViewById(R.id.rtc_multi_room_global_log_textview));
        globalLogTextview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                 new AlertDialog.Builder(RTCMultiRoomActivity.this, R.style.RtmpRoomDialogTheme)
                        .setTitle("Global Log")
                        .setMessage("清除Log")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("清除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                globalLogTextview.setText("");
                                dialog.dismiss();
                            }
                        }).show();
                 return true;
            }
        });

        findViewById(R.id.webrtc_link_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://cloud.tencent.com/document/product/454/14617"));
                startActivity(intent);
            }
        });
        
        globalLogTextviewContainer = ((ScrollView) findViewById(R.id.rtc_mutil_room_global_log_container));

        RTCRoom = new RTCRoom(this.getApplicationContext());
        RTCRoom.setRTCRoomListener(new MemberEventListener());

        internalInitializeRTCRoom(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RTCRoom.setRTCRoomListener(null);
        RTCRoom.logout();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        if (fragment instanceof RTCMultiRoomChatFragment){
            ((RTCMultiRoomChatFragment) fragment).onBackPressed();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPermissionDisable() {
        new AlertDialog.Builder(this, R.style.RtmpRoomDialogTheme)
                .setMessage("需要录音和摄像头权限，请到【设置】【应用】打开")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    @Override
    public void onPermissionGranted() {

    }


    private class LoginInfoResponse {
        public int    code;
        public String message;
        public int    sdkAppID;
        public String accType;
        public String userID;
        public String userSig;
    }

    private class HttpInterceptorLog implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String message) {
            Log.i("HttpRequest", message+"\n");
        }
    }


    private void internalInitializeRTCRoom(LoginInfoResponse resp) {
        LoginInfo loginInfo       = new LoginInfo();
        loginInfo.sdkAppID       = CapConfig.SDK_APP_ID;
        loginInfo.userID         = CapSharedPrefMgr.getInstance().getUserID();
        loginInfo.userSig        = CapSharedPrefMgr.getInstance().getUserSig();
        loginInfo.accType        = CapConfig.ACC_TYPE;
        loginInfo.userName       = userName;
        loginInfo.userAvatar     = userAvatar;

        RTCRoom.login("https://room.qcloud.com/weapp/multi_room", loginInfo, new com.tencent.liteav.demo.rtcroom.RTCRoom.LoginCallback() {
            @Override
            public void onError(int errCode, String errInfo) {
                setTitle(errInfo);
                printGlobalLog(String.format("[Activity]RTCRoom初始化失败：{%s}", errInfo));
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
                setTitle("多人音视频");
                RTCMultiRoomActivity.this.userId = userId;
                printGlobalLog("[Activity]初始化成功,userID{%s}", userId);

                Fragment fragment = getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
                if (!(fragment instanceof RTCMultiRoomListFragment)) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    fragment = RTCMultiRoomListFragment.newInstance(userId);
                    ft.replace(R.id.rtmproom_fragment_container, fragment);
                    ft.commit();
                }
            }
        });
    }

    @Override
    public RTCRoom getRTCRoom() {
        return RTCRoom;
    }

    @Override
    public String getSelfUserID() {
        return userId;
    }

    @Override
    public String getSelfUserName() {
        return userName;
    }

    @Override
    public void showGlobalLog(final boolean enable) {
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    globalLogTextviewContainer.setVisibility(enable ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    @Override
    public void printGlobalLog(final String format, final Object ...args){
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    SimpleDateFormat dataFormat = new SimpleDateFormat("HH:mm:ss");
                    String line = String.format("[%s] %s\n", dataFormat.format(new Date()), String.format(format, args));

                    globalLogTextview.append(line);
                    if (globalLogTextviewContainer.getVisibility() != View.GONE){
                        globalLogTextviewContainer.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }
            });
        }
    }

    @Override
    public void setTitle(final String s) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                String ss = NameGenerator.replaceNonPrintChar(s, 20, "...", false);
                titleTextView.setLinksClickable(false);
                titleTextView.setText(ss);
            }
        });
    }

    private final class MemberEventListener implements IRTCRoomListener {

        @Override
        public void onPusherJoin(PusherInfo member) {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof RTCMultiRoomChatFragment && fragment.isVisible()){
                ((RTCMultiRoomChatFragment) fragment).onPusherJoin(member);
            }
        }

        @Override
        public void onPusherQuit(PusherInfo member) {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof RTCMultiRoomChatFragment && fragment.isVisible()){
                ((RTCMultiRoomChatFragment) fragment).onPusherQuit(member);
            }
        }

        @Override
        public void onRoomClosed(String roomId) {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof RTCMultiRoomChatFragment && fragment.isVisible()){
                ((RTCMultiRoomChatFragment) fragment).onRoomClosed(roomId);
            }
        }

        @Override
        public void onDebugLog(String line) {
            printGlobalLog(line);
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
            Fragment fragment = getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
            if (fragment instanceof RTCMultiRoomChatFragment && fragment.isVisible()){
                ((RTCMultiRoomChatFragment) fragment).onError(errorCode, errorMessage);
            }
        }
    }
}
