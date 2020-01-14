package com.toocms.tab.ui;

/**
 * View层基类，实现一些基本每个页都会用到的方法
 * <p>
 * Author：Zero
 * Date：2017/4/20 17:50
 */
public interface BaseView {

    /**
     * 显示加载条
     */
    void showProgress();

    /**
     * 隐藏加载条
     */
    void removeProgress(Object tag);

    /**
     * 显示Toast提示
     *
     * @param text
     */
    void showToast(String text);
}
