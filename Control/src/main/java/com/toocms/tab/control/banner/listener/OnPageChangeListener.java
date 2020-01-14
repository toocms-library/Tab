package com.toocms.tab.control.banner.listener;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Author：Zero
 * Date：2018/4/26 14:21
 */
public interface OnPageChangeListener {

    void onScrollStateChanged(RecyclerView recyclerView, int newState);

    void onScrolled(RecyclerView recyclerView, int dx, int dy);

    void onPageSelected(int index);
}