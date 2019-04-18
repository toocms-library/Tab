package cn.zero.android.common.view.swipetoloadlayout.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.toocms.frame.ui.R;

import org.xutils.common.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import cn.zero.android.common.util.ArrayUtils;
import cn.zero.android.common.util.ListUtils;
import cn.zero.android.common.view.swipetoloadlayout.OnLoadMoreListener;
import cn.zero.android.common.view.swipetoloadlayout.OnRefreshListener;
import cn.zero.android.common.view.swipetoloadlayout.view.listener.OnItemClickListener;
import cn.zero.android.common.view.swipetoloadlayout.view.utils.RecyclerViewUtils;

/**
 * 下拉刷新、上拉加载更多、加Header、加Footer的RecyclerView
 *
 * @author Zero
 * @date 2016/4/23 15:44
 */
public class SwipeToLoadRecyclerView extends RelativeLayout {

    private List<View> headerViews;
    private List<View> footerViews;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    //    private View footerView;
    private ScrollView scrollView;
    private View emptyView;
    private HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter;
    private OnItemClickListener onItemClickListener;
    private OnRefreshListener onRefreshListener;
    private OnLoadMoreListener onLoadMoreListener;

    private Mode mode;
    private int lastVisibleItemPosition; // 最后一条可见item的位置
    private boolean isLoadEnable; // 加载更多是否可用
    private boolean isLoading; // 正在加载更多
    private boolean isDownSlide; // 是否下滑

    public SwipeToLoadRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public SwipeToLoadRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_swipetoload_recyclerview, this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_load);
        swipeRefreshLayout.setColorSchemeResources(R.color.clr_main, R.color.red, R.color.blue, R.color.black);
        recyclerView = (RecyclerView) findViewById(R.id.swipe_target);
        scrollView = (ScrollView) findViewById(R.id.swipe_frame);
        // 预设一个layoutManager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        switch (mode) {
            case DISABLED:
                swipeRefreshLayout.setEnabled(false);
                isLoadEnable = false;
                break;
            case PULL_FROM_START:
                swipeRefreshLayout.setEnabled(true);
                isLoadEnable = false;
                break;
            case PULL_FROM_END:
                swipeRefreshLayout.setEnabled(false);
                isLoadEnable = true;
                break;
            case BOTH:
                swipeRefreshLayout.setEnabled(true);
                isLoadEnable = true;
//                // 有加载更多，初始化footer
//                footerView = View.inflate(getContext(), R.layout.layout_swipe_footer, null);
//                // 设置footer不能点击
//                footerView.setEnabled(false);
                break;
        }
    }

    private void parseStyle(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeToLoadRecyclerView);
        if (typedArray.hasValue(R.styleable.SwipeToLoadRecyclerView_swipe_mode)) {
            mode = Mode.mapIntToValue(typedArray.getInteger(R.styleable.SwipeToLoadRecyclerView_swipe_mode, 4));
        }
    }

    public void setEmptyView(View emptyView) {
        if (emptyView == null) return;
        this.emptyView = emptyView;
        ViewParent viewParent = this.emptyView.getParent();
        if (viewParent instanceof ViewGroup) {
            ((ViewGroup) viewParent).removeView(this.emptyView);
            // 设置emptyview居中显示
            this.emptyView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            scrollView.addView(this.emptyView);
        }
        this.emptyView.setOnClickListener(null);
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (emptyView != null && headerAndFooterRecyclerViewAdapter != null) {
            final boolean emptyViewVisible = headerAndFooterRecyclerViewAdapter.getItemCount() == 0;
            scrollView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            recyclerView.setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
        if (!ListUtils.isEmpty(headerViews))
            for (View v : headerViews) {
                RecyclerViewUtils.setHeaderView(recyclerView, v);
            }
        if (!ListUtils.isEmpty(footerViews))
            for (View v : footerViews) {
                RecyclerViewUtils.setFooterView(recyclerView, v);
            }
    }

    public void setAdapter(final RecyclerView.Adapter adapter) {
        headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(adapter);
        if (onItemClickListener != null)
            headerAndFooterRecyclerViewAdapter.setOnItemClickListener(onItemClickListener);
        // 监听数据变化
        headerAndFooterRecyclerViewAdapter.registerAdapterDataObserver(observer);
        recyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        checkIfEmpty();
        layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new HeaderSpanSizeLookup((HeaderAndFooterRecyclerViewAdapter) recyclerView.getAdapter(), ((GridLayoutManager) layoutManager).getSpanCount()));
            recyclerView.setLayoutManager(layoutManager);
        }
        if (!ListUtils.isEmpty(headerViews))
            for (View v : headerViews) {
                RecyclerViewUtils.setHeaderView(recyclerView, v);
            }
        if (!ListUtils.isEmpty(footerViews))
            for (View v : footerViews) {
                RecyclerViewUtils.setFooterView(recyclerView, v);
            }
        // 滑到底部自动加载更多
        if (isLoadEnable)
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (layoutManager == null) return;
                    // 向下滑动时直接return
                    if (isDownSlide) return;
                    // 当可见数量 = 列表数量时（内容没有超出屏幕）
                    if (recyclerView.getChildCount() == headerAndFooterRecyclerViewAdapter.getItemCount())
                        return;
                    // 没有处于加载状态
                    if (newState == RecyclerView.SCROLL_STATE_IDLE // 当滚动停止时
                            && layoutManager.getChildCount() > 0 // 界面上可见的数量>0
                            && lastVisibleItemPosition >= layoutManager.getItemCount() - 1 // 到达了最后一个item
                            && !swipeRefreshLayout.isRefreshing() // 非正在刷新数据时
                            && !isLoading) { // 非正在加载更多数据时
                        startLoadMore();
                        if (onLoadMoreListener != null) onLoadMoreListener.onLoadMore();
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    isDownSlide = dy < 0; // 获取是否上滑
                    // 根据不同layoutmanager获取其最后一个item的位置
                    if (layoutManager instanceof LinearLayoutManager) {
                        lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof GridLayoutManager) {
                        lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                        int[] lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                        staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                        lastVisibleItemPosition = ArrayUtils.getMax(lastPositions);
                    } else {
                        throw new RuntimeException("不支持的layoutManager");
                    }
                    // 当处于加载更多时关闭下拉刷新功能
                    if (isLoading) swipeRefreshLayout.setEnabled(false);
                    else swipeRefreshLayout.setEnabled(true);
                }
            });
    }

    public HeaderAndFooterRecyclerViewAdapter getRealAdapter() {
        return headerAndFooterRecyclerViewAdapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public List<View> getHeaderViews() {
        return headerViews;
    }

    public List<View> getFooterViews() {
        return footerViews;
    }

    public void addHeaderView(View view) {
        if (headerViews == null)
            headerViews = new ArrayList<>();
        if (ListUtils.isEmpty(headerViews) || !headerViews.contains(view))
            headerViews.add(view);
    }

    public void removeHeaderView(View view) {
        if (!ListUtils.isEmpty(headerViews) && headerViews.contains(view)) {
            headerViews.remove(view);
            RecyclerViewUtils.removeHeaderView(recyclerView, view);
        }
    }

    public void addFooterView(View view) {
        if (footerViews == null)
            footerViews = new ArrayList<>();
        if (ListUtils.isEmpty(footerViews) || !footerViews.contains(view))
            footerViews.add(view);
    }

    public void removeFooterView(View view) {
        if (!ListUtils.isEmpty(footerViews) && footerViews.contains(view)) {
            footerViews.remove(view);
            RecyclerViewUtils.removeHeaderView(recyclerView, view);
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        onRefreshListener = listener;
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        onLoadMoreListener = listener;
    }

    public void startRefreshing() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        // 自动刷新时需调用一下刷新监听的方法，否则达不到效果
        onRefreshListener.onRefresh();
    }

    public void stopRefreshing() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void startLoadMore() {
        isLoading = true;
//        RecyclerViewUtils.setFooterView(recyclerView, footerView);
//        // 滑动到最后一项item，也就是上面新加的footer
//        recyclerView.scrollToPosition(headerAndFooterRecyclerViewAdapter.getItemCount() - 1);
    }

    public void stopLoadMore() {
        isLoading = false;
//        RecyclerViewUtils.removeFooterView(recyclerView, footerView);
    }

    public enum Mode {
        DISABLED(0),
        PULL_FROM_START(1),
        PULL_FROM_END(2),
        BOTH(3);

        private int mIntValue;

        Mode(int modeInt) {
            mIntValue = modeInt;
        }

        static Mode mapIntToValue(final int modeInt) {
            for (Mode value : Mode.values()) {
                if (modeInt == value.getIntValue()) {
                    return value;
                }
            }
            return getDefault();
        }

        int getIntValue() {
            return mIntValue;
        }

        static Mode getDefault() {
            return BOTH;
        }
    }

    private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };
}
