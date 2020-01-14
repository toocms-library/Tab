package com.toocms.tab.control.progress;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.toocms.tab.control.R;

/**
 * 加载条
 * <p>
 * Author：Zero
 * Date：2017/7/4 10:55
 *
 * @version v4.2.2
 */
public class ProgressDialog extends Dialog {

    public ProgressDialog(Context context) {
        super(context, R.style.progress);
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progress);
//        ButterKnife.bind(this);

        //设置SelectPicPopupWindow弹出窗体的背景
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


    @Override
    public void onBackPressed() {
        dismiss();
    }
}
