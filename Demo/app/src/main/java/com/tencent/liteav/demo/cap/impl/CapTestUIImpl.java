package com.tencent.liteav.demo.cap.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Button;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.CapActivity;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.manager.CapInfoManager;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;
import com.tencent.liteav.demo.cap.ui.AllAppActivity;

public class CapTestUIImpl {
    private static final String TAG = CapTestUIImpl.class.getSimpleName();

    private CapActivity mActivity;
    private Button mBtnRecorder;
    private Button mBtnPusher;
    private Button mBtnChat;
    private Button mBtnShowApps;

    private boolean isRecorderStart = false;
    private boolean isPusherStart = false;
    private boolean isChatStart = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            CLog.d(TAG, "onReceive = " + intent.getAction());
            if ("com.runde.test.action_send_login".equals(action)) {
                CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLoginReqMsg());
            } else if ("com.runde.test.action_send_location".equals(action)) {
                CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLocationReqMsg());
            }
        }
    };
    public CapTestUIImpl(CapActivity context) {
        mActivity = context;
    }

    private void initView() {
        mBtnRecorder = (Button)mActivity.findViewById(R.id.btn_recorder);
        mBtnRecorder.setText("Start Recorder");
        mBtnRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRecorder();
            }
        });

        mBtnPusher = (Button)mActivity.findViewById(R.id.btn_pusher);
        mBtnPusher.setText("Start Pusher");
        mBtnPusher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPusher();
            }
        });

        mBtnChat = (Button)mActivity.findViewById(R.id.btn_chat);
        mBtnChat.setText("Start Chat");
        mBtnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doChat();
            }
        });

        mBtnShowApps = (Button)mActivity.findViewById(R.id.btn_show_apps);
        mBtnShowApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showApps();
            }
        });
    }

    public void create() {
        initView();
        registerIntentReceivers();
    }

    public void destroy() {
        unregisterIntentReceivers();
    }

    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.runde.test.action_send_login");
        filter.addAction("com.runde.test.action_send_location");
        mActivity.registerReceiver(mReceiver, filter);
    }

    private void unregisterIntentReceivers() {
        mActivity.unregisterReceiver(mReceiver);
    }

    private void sendLocation() {
        CapSocketManager.getInstance().onSend(CapInfoManager.getInstance().getLocationReqMsg());
    }

    private void doRecorder() {
        CLog.d(TAG, "doRecorder");
        if (isRecorderStart) {
//            Intent intentOne = new Intent(mActivity, CapRecorderService.class);
//            intentOne.putExtra(CapConstants.EXTRA_CMD_RECORDER, CapConstants.CMD_STOP);
//            mActivity.startService(intentOne);
//            mActivity.stopRecorder();
            mBtnRecorder.setText("Start Recorder");
        } else {
//            Intent intentOne = new Intent(mActivity, CapRecorderService.class);
//            intentOne.putExtra(CapConstants.EXTRA_CMD_RECORDER, CapConstants.CMD_START);
//            mActivity.startService(intentOne);
//            mActivity.startRecorder();
            mBtnRecorder.setText("Stop Recorder");
        }
        isRecorderStart = !isRecorderStart;
    }

    private void doPusher() {
        CLog.d(TAG, "doPusher");
        if (isPusherStart) {
            mActivity.stopPusher();
            mBtnPusher.setText("Start Pusher");
        } else {
            mActivity.startPusher("rtmp://aqm.runde.pro:1935/live/36147_" + CapSharedPrefMgr.getInstance().getUserID());
            mBtnPusher.setText("Stop Pusher");
        }
        isPusherStart = !isPusherStart;
    }

    private void doChat() {
        CLog.d(TAG, "doChat");
        if (isChatStart) {
            mActivity.stopChat();
            mBtnChat.setText("Start Chat");
        } else {
            mActivity.startChat(null, null, false, false);
            mBtnChat.setText("Stop Chat");
        }
        isChatStart = !isChatStart;
    }

    private void showApps() {
        CLog.d(TAG, "showApps");
        Intent intent = new Intent(mActivity, AllAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }
}
