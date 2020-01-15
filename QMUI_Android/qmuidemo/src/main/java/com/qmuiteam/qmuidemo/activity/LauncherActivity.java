/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qmuiteam.qmuidemo.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.utilcode.util.ToastUtils;
import com.qmuiteam.qmui.arch.QMUILatestVisit;
import com.qmuiteam.qmuidemo.QDMainActivity;
import com.suntek.commonlibrary.utils.PermissionUtils;
import com.xy.activity.HKHoldActivity;

/**
 * @author cginechen
 * @date 2016-12-08
 */

public class LauncherActivity extends Activity {
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQUEST_CODE = 100001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        Intent intent = QMUILatestVisit.intentOfLatestVisit(this);
        if (intent == null) {
            intent = new Intent(this, QDMainActivity.class);
        }
        startActivity(intent);
        initPermission();
        finish();
    }

    /**
     * 跳转到权限设置页面返回后再次检查
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        initPermission();
    }

    /**
     * 检查，申请权限
     */
    private void initPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean has = PermissionUtils.checkPermissions(this, BASIC_PERMISSIONS);
            if (!has){
                PermissionUtils.requestPermissions(this,PERMISSION_REQUEST_CODE,
                        BASIC_PERMISSIONS);
            }
        }
    }

    /**
     * 权限授予结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            PermissionUtils.dealPermissionResult(LauncherActivity.this, permissions, grantResults,
                    new PermissionUtils.RequestPermissionCallBack() {
                        @Override
                        public void onGrant(String... permissions) {
                            ToastUtils.showLong("PERMISSION Grant");
                        }

                        @Override
                        public void onDenied(String... permissions) {
                            ToastUtils.showLong("PERMISSION Denied");
                            finish();
                        }

                        @Override
                        public void onDeniedAndNeverAsk(String... permissions) {
                            ToastUtils.showLong("PERMISSION onDeniedAndNeverAsk");
                        }
                    });
        }
    }

}
