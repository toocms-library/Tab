package com.toocms.frame.config;

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
     * 是否使用SnackBar
     *
     * @return
     */
    boolean isUseSnackBar();

    /**
     * 获取项目文件夹名字
     *
     * @return
     * @deprecated 已被弃用，将在以后某个版本中移除
     */
    @Deprecated
    String getProgectFolder();

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
    void initJarForWeApplication(WeApplication application);
}
