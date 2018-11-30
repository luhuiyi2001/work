package com.tencent.liteav.demo;

import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.rtmp.TXLiveConstants;

public class DemoApplication extends MultiDexApplication {
    private static final String TAG = DemoApplication.class.getSimpleName();
//    private RefWatcher mRefWatcher;
    private static DemoApplication instance;
    String ugcLicenceUrl = "http://download-1252463788.cossh.myqcloud.com/xiaoshipin/licence_android/TXUgcSDK.licence";
    String ugcKey = "731ebcab46ecc59ab1571a6a837ddfb6";

    @Override
    public void onCreate() {

        super.onCreate();

        instance = this;

//        TXLiveBase.setConsoleEnabled(true);
//        TXLiveBase.setLogLevel(TXLiveConstants.LOG_LEVEL_DEBUG);
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
//        CapStateInfoManager.getInstance().setContext(this);
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        DemoApplication application = (DemoApplication) context.getApplicationContext();
//        return application.mRefWatcher;
//    }

    public static DemoApplication getApplication() {
        return instance;
    }

}
