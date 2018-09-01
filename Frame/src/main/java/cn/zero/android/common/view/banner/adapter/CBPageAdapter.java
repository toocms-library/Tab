package cn.zero.android.common.view.banner.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.zero.android.common.view.banner.holder.CBViewHolderCreator;
import cn.zero.android.common.view.banner.holder.Holder;
import cn.zero.android.common.view.banner.listener.OnItemClickListener;

public class CBPageAdapter<T> extends RecyclerView.Adapter<Holder> {

    protected List<T> datas;
    private CBViewHolderCreator creator;
    private CBPageAdapterHelper helper;
    private boolean canLoop;
    private OnItemClickListener onItemClickListener;

    public CBPageAdapter(CBViewHolderCreator creator, List<T> datas, boolean canLoop) {
        this.creator = creator;
        this.datas = datas;
        this.canLoop = canLoop;
        helper = new CBPageAdapterHelper();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = creator.getLayoutId();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        helper.onCreateViewHolder(parent, itemView);
        return creator.createHolder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        helper.onBindViewHolder(holder.itemView, position, getItemCount());
        int realPosition = position % datas.size();
        holder.updateUI(datas.get(realPosition));

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new OnPageClickListener(realPosition));
        }
    }

    @Override
    public int getItemCount() {
        //根据模式决定长度
        if (datas.size() == 0) return 0;
        return canLoop ? 3 * datas.size() : datas.size();
    }

    public void setCanLoop(boolean canLoop) {
        this.canLoop = canLoop;
    }

    public int getRealItemCount() {
        return datas.size();
    }

    public boolean isCanLoop() {
        return canLoop;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class OnPageClickListener implements View.OnClickListener {
        private int position;

        public OnPageClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(position);
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}