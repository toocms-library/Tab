package com.toocms.tab.toolkit.configs;

/**
 * 数据集基类
 * <p>
 * 获取项目的AppConfig、Urls、User类
 * <p>
 * Author：Zero
 * Date：2017/6/20 17:49
 *
 * @version v4.2.1
 */
public abstract class BaseDataSet<CONFIG extends IAppConfig, URL extends IUrls, USER extends IUser> {

    /**
     * 获取AppConfig类
     *
     * @return
     */
    public abstract CONFIG getAppConfig();

    /**
     * 获取Urls类
     *
     * @return
     */
    public abstract URL getUrls();

    /**
     * 获取用户信息类
     *
     * @return
     */
    public abstract USER getUser();
}
