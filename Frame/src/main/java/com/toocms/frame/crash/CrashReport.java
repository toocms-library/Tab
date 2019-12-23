package com.toocms.frame.crash;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.toocms.frame.tool.AppManager;
import com.toocms.frame.ui.BaseActivity;
import com.toocms.frame.ui.BaseFragment;
import com.toocms.frame.ui.R;

import org.xutils.common.util.LogUtil;
import org.xutils.x;

import java.io.IOException;

/**
 * 异常捕捉器
 * <p>
 * Author：Zero
 * Date：2017/8/31 16:51
 *
 * @version v1.0
 */

public class CrashReport implements Thread.UncaughtExceptionHandler {

    private static CrashReport mInstance;

    private Thread.UncaughtExceptionHandler mUncaughtExHandler;
    private Application mApp;

    private ProgressDialog progressDialog;

    private CrashReport(Application app) throws Exception {
        this.mApp = app;
        mUncaughtExHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        //Report crash logs when APP is running.
        if (CrashConfig.isAllowReportToHost()) {
            CrashLogSender cls = new CrashLogSender(app);
            cls.start();
        }
    }

    /**
     * Initializes an instance of CrashReport and listens uncaught exception.
     *
     * @param app
     * @return
     */
    public static final CrashReport init(Application app) {
        if (mInstance == null) {
            try {
                mInstance = new CrashReport(app);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.d(CrashConfig.TAG, ex.getLocalizedMessage() + "\n" + ex.getMessage());
        try {
            if (!CrashConfig.isAllowReportToHost() && mUncaughtExHandler != null) {
                mUncaughtExHandler.uncaughtException(thread, ex);
            } else {
                handleException(ex);
                Thread.sleep(2500);
                // 退出程序
                AppManager.getInstance().AppExit(mApp);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 现在收集日志加载条
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                progressDialog = new ProgressDialog(AppManager.getInstance().getTopActivity());
                progressDialog.setMessage(mApp.getString(R.string.crash_message));
                progressDialog.show();
                Looper.loop();
            }
        }.start();
        try {
            CrashLogStore.saveLogToFile(mApp, null, ex);
        } catch (IOException e) {
            Log.e(CrashConfig.TAG, "Save crash log failed. ", e);
        }
        return true;
    }
}
