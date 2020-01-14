package com.toocms.tab.control.swipetoloadlayout.view;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * RecyclerView为GridLayoutManager时，设置了HeaderView，就会用到这个SpanSizeLookup
 *
 * @author Zero
 * @date 2016/4/23 16:34
 */
public class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private HeaderAndFooterRecyclerViewAdapter mAdapter;
    private int mSpanSize = 1;

    public HeaderSpanSizeLookup(HeaderAndFooterRecyclerViewAdapter adapter, int spanSize) {
        this.mAdapter = adapter;
        this.mSpanSize = spanSize;
    }

    @Override
    public int getSpanSize(int position) {
        if (mAdapter == null) {
            throw new RuntimeException("you must setAdapter for RecyclerView first.");
        }
        boolean isHeaderOrFooter = mAdapter.isHeader(position) || mAdapter.isFooter(position);
        return isHeaderOrFooter ? mSpanSize : 1;
    }
}
