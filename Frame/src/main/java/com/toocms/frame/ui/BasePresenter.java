package com.toocms.frame.ui;

/**
 * Presenter层基类
 * Author：Zero
 * Date：2017/4/20 17:52
 */
public abstract class BasePresenter<T> {

    public T view;

    public void attach(T view) {
        this.view = view;
    }

    public void dettach() {
        view = null;
    }
}
