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
     * 获取项目文件夹名字
     *
     * @return
     */
    public String getProgectFolder();

    /**
     * 是否使用SnackBar
     *
     * @return
     */
    public boolean isUseSnackBar();

    /**
     * 获取导航加载图片
     *
     * @return
     */
    public int getNavigationLoadingImage();
}
