package cn.zero.android.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 监听键盘显示/隐藏的布局
 *
 * @author Zero
 * @date 2016/1/21 14:10
 */
public class KeyboardLayout extends LinearLayout {

    private OnSoftKeyboardListener onSoftKeyboardListener;

    public KeyboardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (onSoftKeyboardListener != null) {
            final int newSpec = MeasureSpec.getSize(heightMeasureSpec);
            final int oldSpec = getMeasuredHeight();
            // If layout became smaller, that means something forced it to resize. Probably soft keyboard :)
            if (oldSpec > newSpec) {
                onSoftKeyboardListener.onShown();
            } else {
                onSoftKeyboardListener.onHidden();
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public final void setOnSoftKeyboardListener(final OnSoftKeyboardListener listener) {
        this.onSoftKeyboardListener = listener;
    }

    // Simplest possible listener :)
    public interface OnSoftKeyboardListener {
        public void onShown();

        public void onHidden();
    }
}
