package cn.zero.android.common.view.banner.holder;

import android.view.View;

public interface CBViewHolderCreator {

    Holder createHolder(View itemView);

    int getLayoutId();
}