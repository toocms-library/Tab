package com.toocms.tab.toolkit;

import android.app.ActivityManager;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Iterator;
import java.util.Stack;

/**
 * APP管理类
 *
 * @author Zero
 */
public class AppManager {

    private static Stack<AppCompatActivity> mActivityStack;
    private static AppManager mAppManager;

    private AppManager() {
    }

    /**
     * 单一实例
     */
    public static AppManager getInstance() {
        if (mAppManager == null) {
            mAppManager = new AppManager();
        }
        return mAppManager;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(AppCompatActivity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(activity);
    }

    /**
     * 获取栈顶Activity（堆栈中最后一个压入的）
     */
    public AppCompatActivity getTopActivity() {
        AppCompatActivity activity = mActivityStack.lastElement();
        return activity;
    }

    /**
     * 结束栈顶Activity（堆栈中最后一个压入的）
     */
    public void killTopActivity() {
        AppCompatActivity activity = mActivityStack.lastElement();
        killActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void killActivity(AppCompatActivity activity) {
        if (activity != null) {
            mActivityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 根据localClassName获取activity
     *
     * @param localClassName 去除主包名前缀的activity类名
     * @return localClassName所对应的activity
     */
    public AppCompatActivity getActivityByLocalClassName(String localClassName) {
        Iterator<AppCompatActivity> iterator = mActivityStack.iterator();
        while (iterator.hasNext()) {
            AppCompatActivity activity = iterator.next();
            if (activity.getLocalClassName().equals(localClassName)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 移除在栈顶却已经调用finish的aty
     */
    public void removeTopFinshedActivity() {
        AppCompatActivity activity = getTopActivity();
        if (activity.isFinishing()) {
            mActivityStack.remove(activity);
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void killActivity(Class<?> cls) {
        Iterator<AppCompatActivity> iterator = mActivityStack.iterator();
        while (iterator.hasNext()) {
            AppCompatActivity activity = iterator.next();
            if (activity.getClass().equals(cls)) {
                iterator.remove();
                activity.finish();
                activity = null;
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void killAllActivity() {
        for (int i = 0, size = mActivityStack.size(); i < size; i++) {
            if (null != mActivityStack.get(i)) {
                mActivityStack.get(i).finish();
            }
        }
        mActivityStack.clear();
    }

    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {
        try {
            killAllActivity();
            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.restartPackage(context.getPackageName());
            System.exit(0);
        } catch (Exception e) {
        }
    }
}