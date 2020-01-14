package com.toocms.tab.control.progress;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Author：Zero
 * Date：2017/7/4 10:48
 *
 * @version v4.2.2
 */
class CustomView extends RelativeLayout {

    final int disabledBackgroundColor = Color.parseColor("#E2E2E2");
    int beforeBackground;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled)
            setBackgroundColor(beforeBackground);
        else
            setBackgroundColor(disabledBackgroundColor);
    }
}
