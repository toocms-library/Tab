package com.toocms.frame.config;

import com.umeng.analytics.MobclickAgent;

import org.xutils.x;

import cn.zero.android.common.util.PreferencesUtils;

/**
 * 用户账户登录状态
 *
 * @author Zero @date 2017年4月26日
 * @version 1.0
 */
public class LoginStatus {

    /**
     * 登录状态
     */
    public static final String PREF_KEY_LOGIN_STATE = "PREF_KEY_LOGIN_STATE";

    /**
     * 用户是否已经登陆
     */
    public static boolean isLogin() {
        return PreferencesUtils.getBoolean(x.app(), PREF_KEY_LOGIN_STATE);
    }

    /**
     * 设置登录状态
     *
     * @param isLogin 登录-true ，退出-false
     */
    public static void setLogin(boolean isLogin) {
        PreferencesUtils.putBoolean(x.app(), PREF_KEY_LOGIN_STATE, isLogin);
        if (!isLogin) MobclickAgent.onProfileSignOff();
    }
}
