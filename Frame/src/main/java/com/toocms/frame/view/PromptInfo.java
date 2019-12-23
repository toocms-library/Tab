package com.toocms.frame.view;

import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import org.xutils.x;

import cn.zero.android.common.view.snackbar.TSnackbar;

/**
 * 信息提示控件
 *
 * @author Zero
 * @date 2016/6/29 16:01
 */
public class PromptInfo {

    private static PromptInfo promptInfo;

    private Toast toast;
    private int duration;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            toast.cancel();
        }
    };

    private PromptInfo() {
    }

    public static PromptInfo getInstance() {
        if (promptInfo == null) {
            promptInfo = new PromptInfo();
        }
        return promptInfo;
    }

    public void showToast(View view, CharSequence text) {
        setText(view, text);
    }

    public void showToast(View view, int resId) {
        setText(view, x.app().getResources().getText(resId));
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private void setText(View view, CharSequence text) {
        if (x.dataSet().getAppConfig().isUseSnackBar()) {
            TSnackbar.make(view, text, TSnackbar.LENGTH_SHORT).show();
        } else {
            handler.removeCallbacks(runnable);
            if (toast != null) {
                toast.setText(text);
            } else {
                toast = Toast.makeText(x.app(), text, Toast.LENGTH_SHORT);
                if (duration != 0) toast.setDuration(duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
            }
            handler.postDelayed(runnable, 2000);
            toast.show();
        }
    }
}
