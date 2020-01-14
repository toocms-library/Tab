package com.toocms.tab.control.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * drawableTop与文本一起居中显示
 *
 * @author Zero
 * @date 2016/1/21 9:55
 */
@SuppressLint("AppCompatCustomView")
public class DrawableTopCenterTextView extends TextView {

    public DrawableTopCenterTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawableTopCenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableTopCenterTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        if (drawables != null) {
            Drawable drawableTop = drawables[1];
            if (drawableTop != null) {
                Rect rect = new Rect();
                getPaint().getTextBounds(getText().toString(), 0, getText().length(), rect);
                float textHeight = rect.height();
                int drawablePadding = getCompoundDrawablePadding();
                int drawableHeight = drawableTop.getIntrinsicHeight();
                float bodyHeight = textHeight + drawableHeight + drawablePadding;
                canvas.translate(0, (getHeight() - bodyHeight) / 2);
            }
        }
        super.onDraw(canvas);
    }
}