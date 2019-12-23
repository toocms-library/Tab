package com.toocms.frame.crash;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import cn.zero.android.common.util.FileManager;

/**
 * 错误日志工具类
 *
 * @author Zero
 * @date 2016/7/30 16:52
 */
public class CrashLogUtils {

    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_CLASS_2_G = 1;
    public static final int NETWORK_CLASS_3_G = 2;
    public static final int NETWORK_CLASS_4_G = 3;

    private CrashLogUtils() {
    }

    /**
     * Return general class of network type, such as "3G" or "4G".
     */
    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case 0x1: //TelephonyManager.NETWORK_TYPE_GPRS: level 1
            case 0x2: //TelephonyManager.NETWORK_TYPE_EDGE: level 1
            case 0x4: //TelephonyManager.NETWORK_TYPE_CDMA: level 4
            case 0x7: //TelephonyManager.NETWORK_TYPE_1xRTT: level 4
            case 0xb: //TelephonyManager.NETWORK_TYPE_IDEN: level 8
                return NETWORK_CLASS_2_G;
            case 0x3: //TelephonyManager.NETWORK_TYPE_UMTS: level 1
            case 0x5: //TelephonyManager.NETWORK_TYPE_EVDO_0: level 4
            case 0x6: //TelephonyManager.NETWORK_TYPE_EVDO_A: level 4
            case 0x8: //TelephonyManager.NETWORK_TYPE_HSDPA: level 5
            case 0x9: //TelephonyManager.NETWORK_TYPE_HSUPA: level 5
            case 0xa: //TelephonyManager.NETWORK_TYPE_HSPA: level 5
            case 0xc: //TelephonyManager.NETWORK_TYPE_EVDO_B: level 9
            case 0xe: //TelephonyManager.NETWORK_TYPE_EHRPD: level 11
            case 0xf: //TelephonyManager.NETWORK_TYPE_HSPAP: level 13
                return NETWORK_CLASS_3_G;
            case 0xd: //TelephonyManager.NETWORK_TYPE_LTE: level 11
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    public static boolean isNetworkTypeMobile(int networkType) {
        switch (networkType) {
            case 0x0: //ConnectivityManager.TYPE_MOBILE:  level 1
            case 0x2: //ConnectivityManager.TYPE_MOBILE_MMS: level 8
            case 0x3: //ConnectivityManager.TYPE_MOBILE_SUPL: level 8
            case 0x4: //ConnectivityManager.TYPE_MOBILE_DUN: level 8
            case 0x5: //ConnectivityManager.TYPE_MOBILE_HIPRI: level 8
                return true;
            default:
                return false;
        }
    }

    public static String getVersionName(Application app) {
        String versionName = "";
        try {
            PackageManager pm = app.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(app.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(CrashConfig.TAG, "Unable to get 'VersionName'.", e);
        }
        return versionName;
    }

    public static String getLogStorageDir() {
        return FileManager.getCrashLogFilePath();
    }
}