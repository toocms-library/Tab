package com.toocms.tab.ui;

/**
 * Presenter层基类
 * <p>
 * Author：Zero
 * Date：2017/4/20 17:52
 */
public abstract class BasePresenter<T> {

    /**
     * View层的实例，实现View层的Activity
     */
    public T view;

    /**
     * 为Presenter层绑定View层
     *
     * @param view
     */
    public void attach(T view) {
        this.view = view;
    }

    /**
     * 解绑View层
     */
    public void dettach() {
        view = null;
    }
}
