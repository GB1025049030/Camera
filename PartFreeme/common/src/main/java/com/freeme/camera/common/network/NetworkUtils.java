package com.freeme.camera.common.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import com.freeme.camera.common.Utils;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

public final class NetworkUtils {

    private static ConnectivityManager mConnectivityManager;

    public static void openWirelessSettings() {
        Utils.getApplication().startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isConnect() {
        boolean isConnect;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NetworkInfo info = getActiveNetworkInfo();
            isConnect = info != null && info.isConnected();
        } else {
            NetworkCapabilities capabilities = getNetworkCapabilities();
            isConnect = capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return isConnect;
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isMobileData() {
        boolean isMobileData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NetworkInfo info = getActiveNetworkInfo();
            isMobileData = info != null && info.isAvailable()
                    && info.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            NetworkCapabilities capabilities = getNetworkCapabilities();
            isMobileData = capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        }
        return isMobileData;
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isWifi() {
        boolean isWifi;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            NetworkInfo info = getActiveNetworkInfo();
            isWifi = info != null && info.isAvailable()
                    && info.getType() == ConnectivityManager.TYPE_WIFI;
        } else {
            NetworkCapabilities capabilities = getNetworkCapabilities();
            isWifi = capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return isWifi;
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static NetworkInfo getActiveNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static Network getActiveNetwork() {
        return getConnectivityManager().getActiveNetwork();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static NetworkCapabilities getNetworkCapabilities() {
        Network network = getActiveNetwork();
        if (network == null) return null;
        return getConnectivityManager().getNetworkCapabilities(network);
    }

    private static ConnectivityManager getConnectivityManager() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) Utils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return mConnectivityManager;
    }
}
