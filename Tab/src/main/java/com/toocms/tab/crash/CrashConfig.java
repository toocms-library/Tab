package com.toocms.tab.crash;

import com.toocms.tab.toolkit.x;

import java.lang.reflect.Field;

/**
 * Author：Zero
 * Date：2017/8/31 15:38
 *
 * @version v1.0
 */

public final class CrashConfig {

    public static final String TAG = "CrashReport";

    /**
     * 错误日志上传地址
     */
    public static final String REPORT_URL = "http://api.toocms.com/AppLog/addNew";

    public static final int MAGIC_NUM = 0x0003125B;

    public static final int VERSION = 2;

    /**
     * Preferences存储的文件名
     */
    public static final String PREFERENCES_NAME = "com.toocms.preferences";

    /**
     * Preferences报告的sdk版本号
     */
    public static final String PRE_SDK_VERSION = "sdk_version";

    /**
     * 日志文件名前缀
     */
    public static final String LOG_FILE_PREFIX = "crash_";

    /**
     * 日志文件后缀名
     */
    public static final String LOG_FILE_EXT = ".log";

    /**
     * 发送日志文件的延时时间
     */
    public static final int REPORT_LOG_DELAY = 0;

    /**
     * 是否允许上传错误日志
     * 日志文件将被删除
     */
    public static final boolean isAllowReportToHost() throws Exception {
        Class cls = Class.forName(x.app().getPackageName() + ".BuildConfig");
        Field field = cls.getDeclaredField("DEBUG");
        return !field.getBoolean(cls);
    }
}
