package com.freeme.camera.common.location;

import android.content.Context;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public enum BDLocationUtil {
    I;

    private LocationClient mLocationClient;
    private BDLListener mBDLListener;
    private BDLocationListener mResultListener;

    public void requestLocationUpdates(Context context, BDLocationListener listener) {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(context.getApplicationContext());
            mLocationClient.setLocOption(getLocationClientOption());
            mLocationClient.registerLocationListener(mBDLListener = new BDLListener());
            mResultListener = listener;
        }
        mLocationClient.start();
    }

    public void removeUpdates() {
        if (mLocationClient != null) {
            mLocationClient.setLocOption(null);
            mLocationClient.unRegisterLocationListener(mBDLListener);
            mLocationClient.stop();
            mLocationClient = null;
        }
    }

    private LocationClientOption getLocationClientOption() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);                 // 打开gps
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");            // 可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll
        option.setScanSpan(8000);                // 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);          // 可选(all || noaddr), 设置是否需要地址信息，默认不需要
        option.setIsNeedLocationDescribe(false); // 可选，设置是否需要地址描述
        option.setNeedDeviceDirect(false);       // 可选，设置是否需要设备方向结果
        option.setLocationNotify(false);         // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(false);       // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIsNeedLocationPoiList(false);  // 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.SetIgnoreCacheException(false);   // 可选，默认false，设置是否收集CRASH信息，默认收集
        return option;
    }

    class BDLListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null) {
                switch (bdLocation.getLocType()) {
                    case BDLocation.TypeGpsLocation:
                    case BDLocation.TypeNetWorkLocation:
                    case BDLocation.TypeOffLineLocation:
                    case BDLocation.TypeCacheLocation:
                        mResultListener.onLocationResult(bdLocation);
                        break;
                    default:
                        mResultListener.onLocationResult(null);
                        break;
                }
            }
        }
    }

    public interface BDLocationListener {
        void onLocationResult(BDLocation bdLocation);
    }
}
