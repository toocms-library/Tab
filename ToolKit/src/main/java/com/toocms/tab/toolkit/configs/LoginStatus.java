package com.toocms.tab.toolkit.configs;

import com.toocms.tab.toolkit.PreferencesUtils;
import com.toocms.tab.toolkit.x;

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
     */
    public static void setLogin(boolean isLogin, SignoutListener... listeners) {
        PreferencesUtils.putBoolean(x.app(), PREF_KEY_LOGIN_STATE, isLogin);
        // 退出登录回调
        if (!isLogin) {
            if (listeners != null && listeners.length != 0 && listeners[0] != null) {
                listeners[0].onSignout();
            }
        }
    }

    public interface SignoutListener {
        void onSignout();
    }
}
