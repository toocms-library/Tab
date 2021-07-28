package com.toocms.tab.toolkit.configs;

import android.app.Application;

/**
 * App配置接口
 * Created by Zero on 2017/2/9.
 */
public interface IAppConfig {

    /**
     * 是否使标题居中
     *
     * @return
     */
    boolean isShowTitleCenter();

    /**
     * 获取友盟的AppKey
     *
     * @return
     */
    String getUmengAppkey();

    /**
     * 获取友盟推送服务的Secret
     *
     * @return
     */
    String getUmengPushSecret();

    /**
     * 在WeApplication中做第三方Jar包的初始化操作
     */
    void initJarForWeApplication(Application application);

    /**
     * 是否初始化三方SDK（包括initJarForWeApplication回调方法）
     *
     * @return
     */
    boolean isInitializationSDK();
}
