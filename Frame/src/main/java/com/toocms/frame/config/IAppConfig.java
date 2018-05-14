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
    public boolean isShowTitleCenter();

    /**
     * 是否使用SnackBar
     *
     * @return
     */
    public boolean isUseSnackBar();

    /**
     * 获取项目文件夹名字
     *
     * @return
     */
    public String getProgectFolder();

    /**
     * 获取友盟的AppKey
     *
     * @return
     */
    public String getUmengAppkey();

    /**
     * 获取友盟推送服务的Secret
     *
     * @return
     */
    public String getUmengPushSecret();

    /**
     * 获取导航加载图片
     *
     * @return
     */
    public int getNavigationLoadingImage();
}
