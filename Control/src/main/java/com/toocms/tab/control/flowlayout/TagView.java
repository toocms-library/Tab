package com.toocms.tab.control.flowlayout;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * 标签控件
 *
 * @author Zero
 * @date 2016/9/19 16:58
 */
public class TagView extends FrameLayout implements Checkable {

    private boolean isChecked;
    private static final int[] CHECK_STATE = new int[]{android.R.attr.state_checked};

    public TagView(Context context) {
        super(context);
    }

    public View getTagView() {
        return getChildAt(0);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] states = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(states, CHECK_STATE);
        }
        return states;
    }

    /**
     * 设置选中状态
     *
     * @param checked
     */
    @Override
    public void setChecked(boolean checked) {
        if (this.isChecked != checked) {
            this.isChecked = checked;
            refreshDrawableState();
        }
    }

    /**
     * 获取选中状态
     *
     * @return
     */
    @Override
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * 自动更改选中状态
     */
    @Override
    public void toggle() {
        setChecked(!isChecked);
    }
}
