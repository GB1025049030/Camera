package com.freeme.camera.common.network;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum NetworkConnectStateManager {
    I;

    private NetWorkReceiver mNetWorkReceiver;
    private Application mApplication;
    private IntentFilter mIntentFilter;
    private List<NetworkConnectStateChangeListener> mListeners;

    private void registerReceiver(Application application) {
        Optional.ofNullable(mApplication).orElseGet(() -> {
            Optional.ofNullable(application).ifPresent(application1 -> {
                mApplication = application1;
                mApplication.registerReceiver(getNetWorkReceiver(), getIntentFilter());
            });
            return mApplication;
        });
    }

    private void unregisterReceiver() {
        Optional.ofNullable(mApplication).ifPresent(application -> {
            mApplication.unregisterReceiver(mNetWorkReceiver);
            mApplication = null;
        });
    }

    public void addListener(Application application, NetworkConnectStateChangeListener listener) {
        registerReceiver(application);
        Optional.ofNullable(listener).ifPresent(listener1 -> getOrCreateListeners().add(listener1));
    }

    public void removeListener(NetworkConnectStateChangeListener listener) {
        Optional.ofNullable(listener).ifPresent(listener1 -> {
            if (mListeners.contains(listener1)) {
                mListeners.remove(listener1);
            }
            if (mListeners.isEmpty()) {
                unregisterReceiver();
            }
        });
    }

    private List<NetworkConnectStateChangeListener> getOrCreateListeners() {
        return Optional.ofNullable(mListeners).orElseGet(() -> {
            mListeners = new ArrayList<>(10);
            return mListeners;
        });
    }

    private NetWorkReceiver getNetWorkReceiver() {
        return Optional.ofNullable(mNetWorkReceiver).orElseGet(() -> {
            mNetWorkReceiver = new NetWorkReceiver();
            return mNetWorkReceiver;
        });
    }

    private IntentFilter getIntentFilter() {
        return Optional.ofNullable(mIntentFilter).orElseGet(() -> {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            return mIntentFilter;
        });
    }

    public interface NetworkConnectStateChangeListener {
        void onNetworkConnectStateChange();
    }

    private class NetWorkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Optional.ofNullable(mListeners).ifPresent(listeners -> {
                for (NetworkConnectStateChangeListener listener : listeners) {
                    listener.onNetworkConnectStateChange();
                }
            });
        }
    }
}
