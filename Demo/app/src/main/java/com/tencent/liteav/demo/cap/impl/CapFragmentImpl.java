package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.tencent.liteav.demo.R;
import com.tencent.liteav.demo.cap.CapActivity;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapUtils;
import com.tencent.liteav.demo.cap.fragment.CapChatFragment;
import com.tencent.liteav.demo.cap.fragment.CapPusherFragment;
import com.tencent.liteav.demo.cap.fragment.CapRecorderFragment;
import com.tencent.liteav.demo.cap.manager.CapSocketManager;

public class CapFragmentImpl {
    private static final String TAG = CapFragmentImpl.class.getSimpleName();
    public static final int FRAGMENT_INDEX_NONE = 0;
    public static final int FRAGMENT_INDEX_RECORD = 1;
    public static final int FRAGMENT_INDEX_PUSHER = 2;
    public static final int FRAGMENT_INDEX_CHAT = 3;

    private CapActivity mActivity;
    public CapFragmentImpl(CapActivity context) {
        mActivity = context;
    }

    public void create() {
    }

    public void destroy() {
    }

    public void replaceFragement(Fragment fragment) {
        FragmentTransaction ts = mActivity.getFragmentManager().beginTransaction();
        ts.replace(R.id.rtmproom_fragment_container, fragment);
        ts.commit();
    }

    public boolean isChatUI() {
        return getCurFragmentIndex() == FRAGMENT_INDEX_CHAT;
    }

    public boolean isPusherUI() {
        return getCurFragmentIndex() == FRAGMENT_INDEX_PUSHER;
    }

    public boolean isRecordUI() {
        return getCurFragmentIndex() == FRAGMENT_INDEX_RECORD;
    }

//    public boolean isLiveVideo() {
//        int curIndex = getCurFragmentIndex();
//        return curIndex == FRAGMENT_INDEX_CHAT || curIndex == FRAGMENT_INDEX_PUSHER;
//    }

    public int getCurFragmentIndex() {
        Fragment fragment = mActivity.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
        int index = FRAGMENT_INDEX_NONE;
        if (fragment instanceof CapPusherFragment){
            index = FRAGMENT_INDEX_PUSHER;
        } else if (fragment instanceof CapRecorderFragment){
            index = FRAGMENT_INDEX_RECORD;
        } else if (fragment instanceof CapChatFragment){
            index = FRAGMENT_INDEX_CHAT;
        }
        CLog.d(TAG, "getCurFragmentIndex = " + index);
        return index;
    }

    public Fragment getCurFragment() {
        return mActivity.getFragmentManager().findFragmentById(R.id.rtmproom_fragment_container);
    }
}
