package com.toocms.tab.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 带边框的imageview
 *
 * @author Zero
 * @date 2016/6/14 14:27
 */
@SuppressLint("AppCompatCustomView")
public class FrameImageView extends ImageView {

    public FrameImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FrameImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameImageView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = canvas.getClipBounds();
        rect.bottom--;
        rect.right--;
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#CCCCCC"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect(rect, paint);
    }
}
