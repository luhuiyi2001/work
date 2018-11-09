package com.runde.cap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.runde.cap.tcp.TcpManager;
import com.tencent.rtmp.TXLiveBase;

public class MainActivity extends Activity {

    private TcpManager manager;

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            String result = (String) msg.obj;
            Toast.makeText(MainActivity.this,result, Toast.LENGTH_LONG).show();
//            switch (msg.what) {
//                case TcpManager.STATE_FROM_SERVER_OK:
//                    String result = (String) msg.obj;
//                    Toast.makeText(MainActivity.this,result, Toast.LENGTH_LONG).show();
//                    break;
//
//                default:
//                    break;
//            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = TcpManager.getInstance();
        manager.connect(mHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sdkver = TXLiveBase.getSDKVersionStr();
        Log.d("liteavsdk", "liteav sdk version is : " + sdkver);
        //Toast.makeText(this,"liteav sdk version is : " + sdkver, Toast.LENGTH_LONG).show();
    }

    public void clickMessage(View view){
        manager.sendMessage("12212");
    }

    @Override
    protected void onDestroy() {
        manager.disConnect();
        super.onDestroy();
    }
}
