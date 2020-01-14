package com.toocms.tab.toolkit;

import android.app.Application;
import android.content.Context;

import com.toocms.tab.toolkit.configs.BaseDataSet;

import java.lang.reflect.Method;

/**
 * 任务控制中心, db接口的入口.
 * 需要在application的onCreate中初始化: x.Ext.init(this);
 */
public final class x {

    private x() {
    }

    public static boolean isDebug() {
        return Ext.debug;
    }

    public static Application app() {
        if (Ext.app == null) {
            try {
                // 在IDE进行布局预览时使用
                Class<?> renderActionClass = Class.forName("com.android.layoutlib.bridge.impl.RenderAction");
                Method method = renderActionClass.getDeclaredMethod("getCurrentContext");
                Context context = (Context) method.invoke(null);
                Ext.app = new MockApplication(context);
            } catch (Throwable ignored) {
                throw new RuntimeException("需要在Application的onCreate()调用 x.Ext.init(app)");
            }
        }
        return Ext.app;
    }

    public static <T extends BaseDataSet> T dataSet() {
        return (T) Ext.dataSet;
    }

    public static class Ext {
        private static boolean debug;
        private static Application app;
        private static BaseDataSet dataSet;

        private Ext() {
        }

        public static void init(Application app) {
            if (Ext.app == null) {
                Ext.app = app;
            }
            if (Ext.dataSet == null) {
                try {
                    Class<? extends BaseDataSet> data = (Class<? extends BaseDataSet>) Class.forName(x.app().getPackageName() + ".config.DataSet");
                    Method method = data.getMethod("getInstance", new Class[]{});
                    Ext.dataSet = (BaseDataSet) method.invoke(null, new Object[]{});
                } catch (Exception e) {
                    throw new RuntimeException("缺少：" + x.app().getPackageName() + ".config.DataSet类");
                }
            }
        }

        public static void setDebug(boolean debug) {
            Ext.debug = debug;
        }
    }

    private static class MockApplication extends Application {
        public MockApplication(Context baseContext) {
            this.attachBaseContext(baseContext);
        }
    }
}
