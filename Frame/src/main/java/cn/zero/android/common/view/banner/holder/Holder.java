package cn.zero.android.common.view.banner.holder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * @param <T> 任何你指定的对象
 */
public abstract class Holder<T> extends RecyclerView.ViewHolder {

    public Holder(View itemView) {
        super(itemView);
        initView(itemView);
    }

    protected abstract void initView(View itemView);

    public abstract void updateUI(T data);
}