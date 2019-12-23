package cn.zero.android.common.view;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;

/**
 * Author：Zero
 * Date：2018/8/24 10:58
 *
 * @version v1.0
 */
public class TooCMSToolbar extends Toolbar {

    public TooCMSToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    @Override
    @Deprecated
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }
}
