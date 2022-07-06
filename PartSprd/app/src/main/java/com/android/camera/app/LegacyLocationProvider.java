/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.android.camera.debug.Log;
import com.android.camera.util.AndroidServices;
import com.baidu.location.BDLocation;
import com.freeme.camera.common.location.BDLocationUtil;

import java.text.SimpleDateFormat;

/**
 * A class that handles legacy (network, gps) location providers, in the event
 * the fused location provider from Google Play Services is unavailable.
 */
public class LegacyLocationProvider implements LocationProvider {
    private static final Log.Tag TAG = new Log.Tag("LcyLocProvider");

    private Context mContext;
    private android.location.LocationManager mLocationManager;
    private boolean mCurrentRecordState;
    private boolean mRecordLocation;
    private boolean mRecordLocationAddress;

    LocationListener [] mLocationListeners = new LocationListener[] {
            new LocationListener(android.location.LocationManager.GPS_PROVIDER),
            new LocationListener(android.location.LocationManager.NETWORK_PROVIDER)
    };

    private BDLocation mBDLocation;

    public LegacyLocationProvider(Context context) {
        mContext = context;
    }

    @Override
    public Location getCurrentLocation() {
        if (!mRecordLocation) {
            return null;
        }

        if (mBDLocation == null) {
            return null;
        }

        // go in best to worst order
        for (int i = 0; i < mLocationListeners.length; i++) {
            Location l = mLocationListeners[i].current();
            if (l != null) {
                l.setLatitude(mBDLocation.getLatitude());
                l.setLongitude(mBDLocation.getLongitude());
                return l;
            }
        }

        Log.d(TAG, "No location received yet.");

        return getNewLocation(mBDLocation);
    }

    private Location getNewLocation(BDLocation bdLocation) {
        Location location = null;
        if (bdLocation != null) {
            location = new Location(android.location.LocationManager.NETWORK_PROVIDER);
            location.setAltitude(bdLocation.getAltitude());
            location.setSpeed(bdLocation.getSpeed());
            location.setTime(getTimeStamp(bdLocation.getTime(), "yyyy-MM-dd HH:mm:ss"));
            location.setLatitude(bdLocation.getLatitude());
            location.setLongitude(bdLocation.getLongitude());
        }
        return location;
    }

    private long getTimeStamp(String date_str, String format){
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date_str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public String getCurrentAddress() {
        if (mBDLocation == null) {
            return null;
        }
        return mBDLocation.getAddrStr();
    }

    @Override
    public void recordLocation(boolean recordLocation) {
        if (mRecordLocation != recordLocation) {
            mRecordLocation = recordLocation;
            setReceivingLocationState(mRecordLocation || mRecordLocationAddress);
        }
    }

    @Override
    public void recordLocationAddress(boolean recordLocationAddress) {
        if (mRecordLocationAddress != recordLocationAddress) {
            mRecordLocationAddress = recordLocationAddress;
            setReceivingLocationState(mRecordLocationAddress || mRecordLocation);
        }
    }

    private void setReceivingLocationState(boolean state) {
        if (mCurrentRecordState != state) {
            mCurrentRecordState = state;
            if (state) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }

    @Override
    public void disconnect() {
        Log.d(TAG, "disconnect");
        // The onPause() call to stopReceivingLocationUpdates is sufficient to unregister the
        // Network/GPS listener.
    }

    private void startReceivingLocationUpdates() {
        Log.v(TAG, "starting location updates");
        if (mLocationManager == null) {
            mLocationManager = AndroidServices.instance().provideLocationManager();
        }
        if (mLocationManager != null) {
            try {
                mLocationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[1]);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex.getMessage());
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[0]);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex.getMessage());
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            Log.d(TAG, "startReceivingLocationUpdates");

            BDLocationUtil.I.requestLocationUpdates(mContext, bdLocation -> {
                if(bdLocation != null) mBDLocation = bdLocation;
            });
        }
    }

    private void stopReceivingLocationUpdates() {
        Log.v(TAG, "stopping location updates");
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
            Log.d(TAG, "stopReceivingLocationUpdates");

            BDLocationUtil.I.removeUpdates();
        }
    }

    private class LocationListener
            implements android.location.LocationListener {
        Location mLastLocation;
        boolean mValid = false;
        String mProvider;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
        }

        @Override
        public void onLocationChanged(Location newLocation) {
            if (newLocation.getLatitude() == 0.0
                    && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }
            if (!mValid) {
                Log.d(TAG, "Got first location.");
            }
            mLastLocation.set(newLocation);
            mValid = true;
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            mValid = false;
        }

        @Override
        public void onStatusChanged(
                String provider, int status, Bundle extras) {
            switch(status) {
                case android.location.LocationProvider.OUT_OF_SERVICE:
                case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    // SPRD: locate action is too much frequent, as we get location with current() function,
                    // we can't get the correct location but null.
                    //mValid = false;
                    break;
                }
            }
        }

        public Location current() {
            return mValid ? mLastLocation : null;
        }
    }
}
