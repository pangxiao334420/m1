package com.goluk.a6.control.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

public class NetUtil {

    /* M1 热点开头 */
    private static final String GOLUK_M1_SSID = "Goluk-M1";

    /**
     * 判断移动数据是否打开
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 当前是否已经连上WIFI
     *
     * @param context Context
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiNetworkInfo && wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前连接的WIFI是否以特定名称开头
     */
    private static boolean matchStartSSID(Context context, String ssidStart) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null)
            return false;
        String ssid = wifiInfo.getSSID();
        if (TextUtils.isEmpty(ssid))
            return false;
        // 删除系统获取SSID前后带"
        if (ssid.startsWith("\""))
            ssid = ssid.substring(1);
        if (ssid.endsWith("\""))
            ssid = ssid.substring(0, ssid.length() - 1);
        boolean isMatch = ssid.startsWith(ssidStart);
        return isMatch;
    }

    /**
     * 当前是否连接的是M1的热点
     */
    public static boolean isConnectedM1Wifi(Context context) {
        return isWifiConnected(context) && matchStartSSID(context, GOLUK_M1_SSID);
    }

}
