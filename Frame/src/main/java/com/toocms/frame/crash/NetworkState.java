package com.toocms.frame.crash;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.telephony.TelephonyManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 网络状态
 * <p>
 * Author：Zero
 * Date：2018/10/31 10:59
 *
 * @version v1.0
 */
class NetworkState {

    /**
     * 网络连接状态
     */
    private static Map<DetailedState, String> networkState;

    static {
        networkState = new HashMap<>();
        networkState.put(DetailedState.IDLE, "闲置");
        networkState.put(DetailedState.SCANNING, "正在扫描");
        networkState.put(DetailedState.CONNECTING, "连接中...");
        networkState.put(DetailedState.AUTHENTICATING, "正在进行身份验证...");
        networkState.put(DetailedState.OBTAINING_IPADDR, "正在获取Ip地址");
        networkState.put(DetailedState.CONNECTED, "已连接");
        networkState.put(DetailedState.SUSPENDED, "已暂停");
        networkState.put(DetailedState.DISCONNECTING, "正在断开连接...");
        networkState.put(DetailedState.DISCONNECTED, "已断开");
        networkState.put(DetailedState.FAILED, "失败");
        networkState.put(DetailedState.BLOCKED, "已阻止");
        networkState.put(DetailedState.VERIFYING_POOR_LINK, "暂时关闭（网络状况不佳）");
        networkState.put(DetailedState.CAPTIVE_PORTAL_CHECK, "检查是否需要浏览器二次登录");
    }

    /**
     * 获取网络连接状态
     *
     * @param detailedState
     * @return
     */
    public static String getNetworkState(DetailedState detailedState) {
        return networkState.get(detailedState);
    }

    /**
     * 是否有网络
     *
     * @param has
     * @return
     */
    public static String hasNetwork(boolean has) {
        if (has) return "有网络";
        else return "无网络";
    }

    public static String getNetworkTypeName(Application app, NetworkInfo netInfo) {
        // WiFi
        if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return "WiFi";
        }
        // 移动
        if (CrashLogUtils.isNetworkTypeMobile(netInfo.getType())) {
            TelephonyManager telMgr = (TelephonyManager) app.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            switch (CrashLogUtils.getNetworkClass(telMgr.getNetworkType())) {
                case CrashLogUtils.NETWORK_CLASS_2_G:
                    return "2G";
                case CrashLogUtils.NETWORK_CLASS_3_G:
                    return "3G";
                case CrashLogUtils.NETWORK_CLASS_4_G:
                    return "4G";
                case CrashLogUtils.NETWORK_CLASS_UNKNOWN:
                    return "未知网络";
            }
        }
        return "未知网络";
    }
}
