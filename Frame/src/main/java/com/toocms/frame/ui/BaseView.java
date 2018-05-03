package com.toocms.frame.ui;

/**
 * View层基类
 * Author：Zero
 * Date：2017/4/20 17:50
 */
public interface BaseView {
    void showProgress();

    void removeProgress();

    void showToast(CharSequence text);
}
