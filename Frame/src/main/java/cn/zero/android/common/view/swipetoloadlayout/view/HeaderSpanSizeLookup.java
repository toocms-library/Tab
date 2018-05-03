package cn.zero.android.common.view.swipetoloadlayout.view;

import android.support.v7.widget.GridLayoutManager;

/**
 * RecyclerView为GridLayoutManager时，设置了HeaderView，就会用到这个SpanSizeLookup
 *
 * @author Zero
 * @date 2016/4/23 16:34
 */
public class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private HeaderAndFooterRecyclerViewAdapter adapter;
    private int mSpanSize = 1;

    public HeaderSpanSizeLookup(HeaderAndFooterRecyclerViewAdapter adapter, int spanSize) {
        this.adapter = adapter;
        this.mSpanSize = spanSize;
    }

    @Override
    public int getSpanSize(int position) {
        boolean isHeaderOrFooter = adapter.isHeader(position) || adapter.isFooter(position);
        return isHeaderOrFooter ? mSpanSize : 1;
    }
}
