package com.toocms.tab.control.banner.holder;

import android.view.View;

public interface CBViewHolderCreator {

    Holder createHolder(View itemView);

    int getLayoutId();
}