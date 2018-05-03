package com.toocms.frame.config;

/**
 * Url集合类
 * Author：Zero
 * Date：2017/4/26 14:36
 */
public interface IUrls {

    /**
     * 获取Http主URL
     *
     * @return
     */
    public String getBaseUrl();

    /**
     * 获取更新的URL
     *
     * @return
     */
    public String getUpdateUrl();
}
