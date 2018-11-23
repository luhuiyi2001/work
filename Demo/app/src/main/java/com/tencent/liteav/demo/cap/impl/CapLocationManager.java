package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;

public class CapLocationManager {
    private static final String TAG = CapLocationManager.class.getSimpleName();

    private LocationManager mLocationManager;
    private boolean mIsRequestUpdated;

    // 定义一个LocationListener来响应定位更新
    LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            CLog.d(TAG, "onStatusChanged = [ " + location.getLatitude() + ", " + location.getLongitude() + " ]");
            // 当地理位置信息有变化的时候回调
            makeUseOfNewLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            CLog.d(TAG, "onStatusChanged = [ " + provider + " ]");
        }

        public void onProviderEnabled(String provider) {
            CLog.d(TAG, "onProviderEnabled = [ " + provider + " ]");
        }

        public void onProviderDisabled(String provider) {
            CLog.d(TAG, "onProviderDisabled = [ " + provider + " ]");
        }
    };

    private Context mContext;
    public CapLocationManager(Activity context) {
        mContext = context;
        // 获取系统的LocationManager服务
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        saveLocation(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    }

    public void start() {
        requestLocationUpdates();
    }

    public void stop() {
        removeUpdates();
    }

    private void requestLocationUpdates() {
        if (mIsRequestUpdated) {
            return;
        }
        // 向Location Manager注册LocationListener监听定位更新
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        mIsRequestUpdated = true;
    }

    private void removeUpdates() {
        if (!mIsRequestUpdated) {
            return;
        }
        mLocationManager.removeUpdates(mLocationListener);
        mIsRequestUpdated = false;
    }

    private void makeUseOfNewLocation(Location location) {
        saveLocation(location);
    }

    private void saveLocation(Location location) {
        if (location == null) {
            return;
        }
        CapSharedPrefMgr.getInstance().putLatitude(String.valueOf(location.getLatitude()));
        CapSharedPrefMgr.getInstance().putLongitude(String.valueOf(location.getLongitude()));
    }
}
