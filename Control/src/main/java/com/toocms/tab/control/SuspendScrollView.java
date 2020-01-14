package com.toocms.tab.control;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 仿美团立即抢购悬浮条的ScrollView
 * 
 * @author Zero
 *
 *         2015年1月8日
 */
public class SuspendScrollView extends ScrollView {

	private OnScrollListener onScrollListener;

	public SuspendScrollView(Context context) {
		this(context, null);
	}

	public SuspendScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SuspendScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置滚动接口
	 * 
	 * @param onScrollListener
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (onScrollListener != null) {
			onScrollListener.onScroll(t);
		}
	}

	/**
	 * 滚动的回调接口
	 * 
	 * @author Zero
	 *
	 *         2015年1月8日
	 */
	public interface OnScrollListener {
		/**
		 * 回调方法， 返回MyScrollView滑动的Y方向距离
		 * 
		 * @param scrollY
		 */
		void onScroll(int scrollY);
	}

}
