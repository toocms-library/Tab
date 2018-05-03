package cn.zero.android.common.view.shapeimageview;

import android.content.Context;
import android.util.AttributeSet;

import com.toocms.frame.ui.R;

import cn.zero.android.common.view.shapeimageview.shader.ShaderHelper;
import cn.zero.android.common.view.shapeimageview.shader.SvgShader;

public class HexagonImageView extends ShaderImageView {

    public HexagonImageView(Context context) {
        super(context);
    }

    public HexagonImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HexagonImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public ShaderHelper createImageViewHelper() {
        return new SvgShader(R.raw.imgview_hexagon);
    }
}
