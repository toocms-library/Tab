package com.toocms.frame.config;

import com.umeng.analytics.MobclickAgent;

import org.xutils.x;

import cn.zero.android.common.util.PreferencesUtils;
import cn.zero.android.common.util.StringUtils;

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
     * 设置登录状态，并进行用户统计分析
     *
     * @param isLogin 登录-true ，退出-false
     * @param userId  用户Id，用于用户统计分析，登录时需要，退出时不再需要
     */
    public static void setLogin(boolean isLogin, String... userId) {
        PreferencesUtils.putBoolean(x.app(), PREF_KEY_LOGIN_STATE, isLogin);
        // 打开用户统计分析
        if (isLogin) {
            if (!StringUtils.isEmpty(userId[0])) MobclickAgent.onProfileSignIn(userId[0]);
        } else {
            // 清除用户信息
            WeApplication.getInstance().clearUserInfo();
            // 关闭用户统计分析
            MobclickAgent.onProfileSignOff();
        }
    }
}
