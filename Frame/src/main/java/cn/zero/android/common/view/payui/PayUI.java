package cn.zero.android.common.view.payui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.toocms.frame.tool.AppManager;
import com.toocms.frame.ui.BaseActivity;
import com.toocms.frame.ui.BaseFragment;

/**
 * 显示输入支付密码
 * <p>
 * Author：Zero
 * Date：2017/5/25 17:38
 *
 * @version v4.0
 */

public class PayUI {

    public static final void showPayUI(String title, String content, PayPwdView.InputCallBack callBack) {
        Bundle bundle = new Bundle();
        bundle.putString(PayFragment.EXTRA_TITLE, title);
        bundle.putString(PayFragment.EXTRA_CONTENT, content);
        PayFragment fragment = new PayFragment();
        fragment.setArguments(bundle);
        fragment.setPaySuccessCallBack(callBack);
        FragmentManager manager;
        if (AppManager.instance instanceof FragmentActivity) {
            manager = ((BaseActivity) AppManager.instance).getSupportFragmentManager();
        } else if (AppManager.instance instanceof Fragment) {
            manager = ((BaseFragment) AppManager.instance).getFragmentManager();
        } else {
            throw new RuntimeException("请在FragmentActivity/Fragment中调用");
        }
        fragment.show(manager, "Pay");
    }
}
