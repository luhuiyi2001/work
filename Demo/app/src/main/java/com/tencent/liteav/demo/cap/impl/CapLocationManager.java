package com.tencent.liteav.demo.cap.impl;

import android.app.Activity;
import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.tencent.liteav.demo.cap.common.CLog;
import com.tencent.liteav.demo.cap.manager.CapSharedPrefMgr;

public class CapLocationManager {
    private static final String TAG = CapLocationManager.class.getSimpleName();

//    private LocationManager mLocationManager;
//    private boolean mIsRequestUpdated;

    // 定义一个LocationListener来响应定位更新
//    LocationListener mLocationListener = new LocationListener() {
//        public void onLocationChanged(Location location) {
//            CLog.d(TAG, "onStatusChanged = [ " + location.getLatitude() + ", " + location.getLongitude() + " ]");
//            // 当地理位置信息有变化的时候回调
//            makeUseOfNewLocation(location);
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            CLog.d(TAG, "onStatusChanged = [ " + provider + " ]");
//        }
//
//        public void onProviderEnabled(String provider) {
//            CLog.d(TAG, "onProviderEnabled = [ " + provider + " ]");
//        }
//
//        public void onProviderDisabled(String provider) {
//            CLog.d(TAG, "onProviderDisabled = [ " + provider + " ]");
//        }
//    };

//    LocationClient mLocationClient;
//声明AMapLocationClient类对象
public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            CLog.d(TAG, "onLocationChanged");
            if (aMapLocation != null) {
                CLog.d(TAG, "onLocationChanged : [ " + aMapLocation.getErrorCode() + " ]");
                if (aMapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    int lcationType = aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    double latitude = aMapLocation.getLatitude();//获取纬度
                    double longitude = aMapLocation.getLongitude();
                    CLog.d(TAG, "onLocationChanged : [ " + latitude + ", " + longitude + ", " + lcationType + " ]");
                    saveLocation(latitude, longitude);
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    CLog.e("AmapError","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };


    private Context mContext;
    public CapLocationManager(Activity context) {
        mContext = context;
        // 获取系统的LocationManager服务
//        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//        saveLocation(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
//        initLocationOption();
        initAmapLocation();
    }

    public void start() {
        requestBDLocationUpdates();
    }

    public void stop() {
        removeBDLocationUpdates();
    }

    private void requestBDLocationUpdates() {
        //开始定位
//        mLocationClient.start();
        //启动定位
        mLocationClient.startLocation();
    }

    private void removeBDLocationUpdates() {
//        mLocationClient.stop();
        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
    }
//    private void requestLocationUpdates() {
//        if (mIsRequestUpdated) {
//            return;
//        }
//        // 向Location Manager注册LocationListener监听定位更新
//        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
//        mIsRequestUpdated = true;
//    }

//    private void removeUpdates() {
//        if (!mIsRequestUpdated) {
//            return;
//        }
//        mLocationManager.removeUpdates(mLocationListener);
//        mIsRequestUpdated = false;
//    }

//    private void makeUseOfNewLocation(Location location) {
//        saveLocation(location);
//    }
//
//    private void saveLocation(Location location) {
//        if (location == null) {
//            return;
//        }
//        CapSharedPrefMgr.getInstance().putLatitude(String.valueOf(location.getLatitude()));
//        CapSharedPrefMgr.getInstance().putLongitude(String.valueOf(location.getLongitude()));
//    }

    private void saveLocation(double latitude, double longitude) {
        CapSharedPrefMgr.getInstance().putLatitude(String.valueOf(latitude));
        CapSharedPrefMgr.getInstance().putLongitude(String.valueOf(longitude));
    }

    private void initAmapLocation() {
        if (mLocationClient != null) {
            CLog.d(TAG, "mLocationClient != null");
            return;
        }
        //初始化定位
        mLocationClient = new AMapLocationClient(mContext.getApplicationContext());
        //声明AMapLocationClientOption对象
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setInterval(10000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(locationOption);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
    }

    /**
     * 初始化定位参数配置
     */
//    private void initLocationOption() {
//        CLog.d(TAG, "initLocationOption");
//        if (mLocationClient != null) {
//            CLog.d(TAG, "mLocationClient != null");
//            return;
//        }
//        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
//        mLocationClient = new LocationClient(mContext.getApplicationContext());
//        //声明LocationClient类实例并配置定位参数
//        LocationClientOption locationOption = new LocationClientOption();
//        MyLocationListener myLocationListener = new MyLocationListener();
//        //注册监听函数
//        mLocationClient.registerLocationListener(myLocationListener);
//        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
//        locationOption.setCoorType("gcj02");
//        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
//        locationOption.setScanSpan(1000);
//        //可选，设置是否需要地址信息，默认不需要
//        locationOption.setIsNeedAddress(true);
//        //可选，设置是否需要地址描述
//        locationOption.setIsNeedLocationDescribe(true);
//        //可选，设置是否需要设备方向结果
//        locationOption.setNeedDeviceDirect(false);
//        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        locationOption.setLocationNotify(true);
//        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//        locationOption.setIgnoreKillProcess(true);
//        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        locationOption.setIsNeedLocationDescribe(true);
//        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        locationOption.setIsNeedLocationPoiList(true);
//        //可选，默认false，设置是否收集CRASH信息，默认收集
//        locationOption.SetIgnoreCacheException(false);
//        //可选，默认false，设置是否开启Gps定位
//        locationOption.setOpenGps(true);
//        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
//        locationOption.setIsNeedAltitude(false);
//        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
//        locationOption.setOpenAutoNotifyMode();
//        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
//        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
//        mLocationClient.setLocOption(locationOption);
//    }
    /**
     * 实现定位回调
     */
//    public class MyLocationListener extends BDAbstractLocationListener {
//        @Override
//        public void onReceiveLocation(BDLocation location){
//            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
//            //以下只列举部分获取经纬度相关（常用）的结果信息
//            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
//
//            //获取纬度信息
//            double latitude = location.getLatitude();
//            //获取经度信息
//            double longitude = location.getLongitude();
//            //获取定位精度，默认值为0.0f
//            float radius = location.getRadius();
//            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
//            String coorType = location.getCoorType();
//            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
//            int errorCode = location.getLocType();
//            CLog.d(TAG, "onReceiveLocation : [ " + latitude + ", " + longitude + ", " + radius + ", " + coorType + ", " + errorCode + " ]");
//            saveLocation(latitude, longitude);
//        }
//    }
}
