package com.tencent.liteav.demo;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.liteav.demo.cap.CLog;
import com.tencent.liteav.demo.cap.CapClientManager;
import com.tencent.liteav.demo.cap.CapInfoResponse;
import com.tencent.liteav.demo.cap.OnReceiveEventListener;
import com.tencent.liteav.demo.play.LivePlayerActivity;
import com.tencent.liteav.demo.push.LivePublisherActivity;
import com.tencent.liteav.demo.ugccommon.TCVideoJoinChooseActivity;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;


public class DemoApplication extends MultiDexApplication implements OnReceiveEventListener {
    private static final String TAG = "CapClientManager";
//    private RefWatcher mRefWatcher;
    private static DemoApplication instance;
    String ugcLicenceUrl = "http://download-1252463788.cossh.myqcloud.com/xiaoshipin/licence_android/TXUgcSDK.licence";
    String ugcKey = "731ebcab46ecc59ab1571a6a837ddfb6";

    @Override
    public void onCreate() {

        super.onCreate();

        instance = this;

        TXLiveBase.setConsoleEnabled(true);
        TXLiveBase.setLogLevel(TXLiveConstants.LOG_LEVEL_DEBUG);
        TXLiveBase.setAppID("1252463788");
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(TXLiveBase.getSDKVersionStr());
        CrashReport.initCrashReport(getApplicationContext(),strategy);

        TXLiveBase.getInstance().setLicence(instance, ugcLicenceUrl, ugcKey);

//        File file = getFilesDir();
//        Log.w("DemoApplication", "load:" + file.getAbsolutePath());
//        TXLiveBase.setLibraryPath(file.getAbsolutePath());
        //测试代码
//        TCHttpEngine.getInstance().initContext(getApplicationContext());
//        mRefWatcher = LeakCanary.install(this);
        CapClientManager.getInstance().setOnReceiveEventListener(this);
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        DemoApplication application = (DemoApplication) context.getApplicationContext();
//        return application.mRefWatcher;
//    }

    public static DemoApplication getApplication() {
        return instance;
    }

    public void onLogin(CapInfoResponse resp) {
        CLog.d(TAG, "onLogin = [ " + resp.status + ", " + resp.msg + ", " + resp.data + " ]");
        if (resp.status == null || !resp.status) {
            return;
        }
        CapClientManager.getInstance().startHeartbeatTimer();
        //TODO do login info

    }
    public void onSOS(CapInfoResponse resp) {
        CLog.d(TAG, "onSOS = [ " + resp.status + ", " + resp.msg + " ]");

    }
    public void onLocation(CapInfoResponse resp) {
        CLog.d(TAG, "onLocation = [ " + resp.status + ", " + resp.msg + " ]");
    }
    public void onOpenRTSP(CapInfoResponse resp) {
        CLog.d(TAG, "onOpenRTSP = [ " + resp.push_url + " ]");
        Intent intent = new Intent(this, LivePublisherActivity.class);
        intent.putExtra("TITLE", LivePublisherActivity.class.getName());
        intent.putExtra("PUSH_URL", resp.push_url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void onStopRTSP(CapInfoResponse resp) {
        CLog.d(TAG, "onStopRTSP");
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void onPullWifiList(CapInfoResponse resp) {
        CLog.d(TAG, "onPullWifiList");
    }
    public void onConnWifi(CapInfoResponse resp) {
        CLog.d(TAG, "onConnWifi = [ " + resp.spot + ", " + resp.pwd + " ]");
    }

    private void launchActivity(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("TITLE", activityClass.getName());
//        if (childItem.mIconId == R.drawable.play) {
//            intent.putExtra("PLAY_TYPE", LivePlayerActivity.ACTIVITY_TYPE_VOD_PLAY);
//        } else if (childItem.mIconId == R.drawable.live) {
//            intent.putExtra("PLAY_TYPE", LivePlayerActivity.ACTIVITY_TYPE_LIVE_PLAY);
//        } else if (childItem.mIconId == R.drawable.mic) {
//            intent.putExtra("PLAY_TYPE", LivePlayerActivity.ACTIVITY_TYPE_LINK_MIC);
//        } else if (childItem.mIconId == R.drawable.cut) {
//            intent.putExtra("CHOOSE_TYPE", TCVideoJoinChooseActivity.TYPE_SINGLE_CHOOSE);
//        } else if (childItem.mIconId == R.drawable.composite) {
//            intent.putExtra("CHOOSE_TYPE", TCVideoJoinChooseActivity.TYPE_MULTI_CHOOSE);
//        } else if (childItem.mIconId == R.drawable.conf_icon) {
////                        intent.putExtra("CHOOSE_TYPE", VideoCallActivity.TYPE_MULTI_CHOOSE);
//        } else if (childItem.mIconId == R.drawable.realtime_play) {
//            intent.putExtra("PLAY_TYPE", LivePlayerActivity.ACTIVITY_TYPE_REALTIME_PLAY);
//        } else if (childItem.mIconId == R.drawable.update) {
//            intent.putExtra("CHOOSE_TYPE", TCVideoJoinChooseActivity.TYPE_PUBLISH_CHOOSE);
//        } else if (childItem.mIconId == R.drawable.short_video_picture) {
//            intent.putExtra("CHOOSE_TYPE", TCVideoJoinChooseActivity.TYPE_MULTI_CHOOSE_PICTURE);
//        }
        this.startActivity(intent);
    }
}
