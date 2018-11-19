package com.tencent.liteav.demo.cap.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Binder;
import android.os.IBinder;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.common.CapConstants;
import com.tencent.liteav.demo.cap.manager.CapClientManager;

/**
 * Created by XingYue on 2018/11/17.
 */

public class CapClientService extends Service {
    public static final String TAG = CapClientService.class.getSimpleName();

    private ClientBinder mBinder = new ClientBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        CLog.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CLog.d(TAG, "onStartCommand");
        String extraValue = intent.getStringExtra(CapConstants.EXTRA_CMD_CLIENT);
        if (CapConstants.CMD_START.equals(extraValue)) {
            CapClientManager.getInstance().onStart();
        } else {
            CapClientManager.getInstance().onStop();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.d(TAG, "onDestroy");
        CapClientManager.getInstance().onStop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class ClientBinder extends Binder {

//        public void startDownload() {
//            CLog.d(TAG, "startDownload() executed");
//            // 执行具体的下载任务
//        }
//        public int getProgress(){
//            CLog.d(TAG, "getProgress() executed");
//            return 0;
//        }

    }
}
