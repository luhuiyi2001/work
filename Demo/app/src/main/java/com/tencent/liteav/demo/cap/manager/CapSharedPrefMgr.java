package com.tencent.liteav.demo.cap.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.liteav.demo.DemoApplication;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.common.CapSharePrefUtils;
import com.tencent.liteav.demo.play.superplayer.SharePreferenceUtils;

/**
 * Created by XingYue on 2018/11/18.
 */

public class CapSharedPrefMgr {
    private static final String TAG = CapInfoManager.class.getSimpleName();
    private static CapSharedPrefMgr sMgr;
    private SharedPreferences mSharedPrefs;

    public static CapSharedPrefMgr getInstance() {
        if (sMgr == null) {
            sMgr = new CapSharedPrefMgr();
        }
        return sMgr;
    }

    private CapSharedPrefMgr() {
        mSharedPrefs = CapSharePrefUtils.newInstance(DemoApplication.getApplication(), CapConstants.SHARED_PREFS_NAME);
    }

    public String getUserID() {
        return CapSharePrefUtils.getString(mSharedPrefs, CapConstants.KEY_USER_ID);
    }

    public String getUserSig() {
        return CapSharePrefUtils.getString(mSharedPrefs, CapConstants.KEY_USER_SIG);
    }

    public String getUserName() {
        return CapSharePrefUtils.getString(mSharedPrefs, CapConstants.KEY_USER_NAME);
    }

    public String getUserImg() {
        return CapSharePrefUtils.getString(mSharedPrefs, CapConstants.KEY_USER_IMG);
    }

    public final void putUserID(String value) {
        CapSharePrefUtils.putString(mSharedPrefs, CapConstants.KEY_USER_ID, value);
    }

    public final void putUserName(String value) {
        CapSharePrefUtils.putString(mSharedPrefs, CapConstants.KEY_USER_NAME, value);
    }

    public final void putUserSig(String value) {
        CapSharePrefUtils.putString(mSharedPrefs, CapConstants.KEY_USER_SIG, value);
    }

    public final void putUserImg(String value) {
        CapSharePrefUtils.putString(mSharedPrefs, CapConstants.KEY_USER_IMG, value);
    }
}
