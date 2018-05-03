package cn.zero.android.common.view.shapeimageview;

import android.content.Context;
import android.util.AttributeSet;
import cn.zero.android.common.view.shapeimageview.shader.BubbleShader;
import cn.zero.android.common.view.shapeimageview.shader.ShaderHelper;

public class BubbleImageView extends ShaderImageView {

    public BubbleImageView(Context context) {
        super(context);
    }

    public BubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public ShaderHelper createImageViewHelper() {
        return new BubbleShader();
    }
}
