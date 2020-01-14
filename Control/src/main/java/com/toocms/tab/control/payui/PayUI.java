package com.toocms.tab.control.payui;

import android.os.Bundle;

import com.toocms.tab.toolkit.AppManager;

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
        fragment.show(AppManager.getInstance().getTopActivity().getSupportFragmentManager(), "Pay");
    }
}
