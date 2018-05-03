package cn.zero.android.common.util;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build.VERSION;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * ViewUtils
 * <ul>
 * <strong>get view height</strong>
 * <li>{@link ViewUtils#getListViewHeightBasedOnChildren(ListView)}</li>
 * <li>{@link ViewUtils#getAbsListViewHeightBasedOnChildren(AbsListView)}</li>
 * </ul>
 * <ul>
 * <strong>set view height</strong>
 * <li>{@link ViewUtils#setViewHeight(View, int)} set view height</li>
 * <li>{@link ViewUtils#setListViewHeightBasedOnChildren(ListView)}</li>
 * <li>{@link ViewUtils#setAbsListViewHeightBasedOnChildren(AbsListView)}</li>
 * </ul>
 * <ul>
 * <strong>get other info</strong>
 * <li>{@link ViewUtils#getGridViewVerticalSpacing(GridView)} get GridView
 * vertical spacing</li>
 * </ul>
 * <ul>
 * <strong>set other info</strong>
 * <li>{@link ViewUtils#setSearchViewOnClickListener(View, OnClickListener)}</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-12-24
 */
public class ViewUtils {

	/**
	 * get ListView height according to every children
	 * 
	 * @param view
	 * @return
	 */
	public static int getListViewHeightBasedOnChildren(ListView view) {
		int height = getAbsListViewHeightBasedOnChildren(view);
		ListAdapter adapter;
		int adapterCount;
		if (view != null && (adapter = view.getAdapter()) != null && (adapterCount = adapter.getCount()) > 0) {
			height += view.getDividerHeight() * (adapterCount - 1);
		}
		return height;
	}

	// /**
	// * get GridView height according to every children
	// *
	// * @param view
	// * @return
	// */
	// public static int getGridViewHeightBasedOnChildren(GridView view) {
	// int height = getAbsListViewHeightBasedOnChildren(view);
	// ListAdapter adapter;
	// int adapterCount, numColumns = getGridViewNumColumns(view);
	// if (view != null && (adapter = view.getAdapter()) != null &&
	// (adapterCount = adapter.getCount()) > 0
	// && numColumns > 0) {
	// int rowCount = (int)Math.ceil(adapterCount / (double)numColumns);
	// height = rowCount * (height / adapterCount +
	// getGridViewVerticalSpacing(view));
	// }
	// return height;
	// }
	//
	// /**
	// * get GridView columns number
	// *
	// * @param view
	// * @return
	// */
	// public static int getGridViewNumColumns(GridView view) {
	// if (view == null || view.getChildCount() <= 0) {
	// return 0;
	// }
	// if (Build.VERSION.SDK_INT >= 11) {
	// return getNumColumnsCompat11(view);
	//
	// } else {
	// int columns = 0;
	// int children = view.getChildCount();
	// if (children > 0) {
	// int width = view.getChildAt(0).getMeasuredWidth();
	// if (width > 0) {
	// columns = view.getWidth() / width;
	// }
	// }
	// return columns;
	// }
	// }
	//
	// @TargetApi(11)
	// private static int getNumColumnsCompat11(GridView view) {
	// return view.getNumColumns();
	// }

	private static final String CLASS_NAME_GRID_VIEW = "android.widget.GridView";
	private static final String FIELD_NAME_VERTICAL_SPACING = "mVerticalSpacing";

	/**
	 * get GridView vertical spacing
	 * 
	 * @param view
	 * @return
	 */
	public static int getGridViewVerticalSpacing(GridView view) {
		// get mVerticalSpacing by android.widget.GridView
		Class<?> demo = null;
		int verticalSpacing = 0;
		try {
			demo = Class.forName(CLASS_NAME_GRID_VIEW);
			Field field = demo.getDeclaredField(FIELD_NAME_VERTICAL_SPACING);
			field.setAccessible(true);
			verticalSpacing = (Integer) field.get(view);
			return verticalSpacing;
		} catch (Exception e) {
			/**
			 * accept all exception, include ClassNotFoundException,
			 * NoSuchFieldException, InstantiationException,
			 * IllegalArgumentException, IllegalAccessException,
			 * NullPointException
			 */
			e.printStackTrace();
		}
		return verticalSpacing;
	}

	/**
	 * get AbsListView height according to every children
	 * 
	 * @param view
	 * @return
	 */
	public static int getAbsListViewHeightBasedOnChildren(AbsListView view) {
		ListAdapter adapter;
		if (view == null || (adapter = view.getAdapter()) == null) {
			return 0;
		}

		int height = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View item = adapter.getView(i, null, view);
			if (item instanceof ViewGroup) {
				item.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
			item.measure(0, 0);
			height += item.getMeasuredHeight();
		}
		height += view.getPaddingTop() + view.getPaddingBottom();
		return height;
	}

	/**
	 * set view height
	 * 
	 * @param view
	 * @param height
	 */
	public static void setViewHeight(View view, int height) {
		if (view == null) {
			return;
		}

		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.height = height;
	}

	// /**
	// * set GistView height which is calculated by {@link #
	// getGridViewHeightBasedOnChildren(GridView)}
	// *
	// * @param view
	// * @return
	// */
	// public static void setGridViewHeightBasedOnChildren(GridView view) {
	// setViewHeight(view, getGridViewHeightBasedOnChildren(view));
	// }

	/**
	 * set ListView height which is calculated by
	 * {@link # getListViewHeightBasedOnChildren(ListView)}
	 * 
	 * @param view
	 * @return
	 */
	public static void setListViewHeightBasedOnChildren(ListView view) {
		setViewHeight(view, getListViewHeightBasedOnChildren(view));
	}

	/**
	 * set AbsListView height which is calculated by
	 * {@link # getAbsListViewHeightBasedOnChildren(AbsListView)}
	 * 
	 * @param view
	 * @return
	 */
	public static void setAbsListViewHeightBasedOnChildren(AbsListView view) {
		setViewHeight(view, getAbsListViewHeightBasedOnChildren(view));
	}

	/**
	 * set SearchView OnClickListener
	 * 
	 * @param v
	 * @param listener
	 */
	public static void setSearchViewOnClickListener(View v, OnClickListener listener) {
		if (v instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) v;
			int count = group.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = group.getChildAt(i);
				if (child instanceof LinearLayout || child instanceof RelativeLayout) {
					setSearchViewOnClickListener(child, listener);
				}

				if (child instanceof TextView) {
					TextView text = (TextView) child;
					text.setFocusable(false);
				}
				child.setOnClickListener(listener);
			}
		}
	}

	/**
	 * 无效值
	 */
	public static final int INVALID = Integer.MIN_VALUE;

	/**
	 * 描述：重置AbsListView的高度. item 的最外层布局要用
	 * RelativeLayout,如果计算的不准，就为RelativeLayout指定一个高度
	 * 
	 * @param absListView
	 *            the abs list view
	 * @param lineNumber
	 *            每行几个 ListView一行一个item
	 * @param verticalSpace
	 *            the vertical space
	 */
	public static void setAbsListViewHeight(AbsListView absListView, int lineNumber, int verticalSpace) {

		int totalHeight = getAbsListViewHeight(absListView, lineNumber, verticalSpace);
		ViewGroup.LayoutParams params = absListView.getLayoutParams();
		params.height = totalHeight;
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		absListView.setLayoutParams(params);
	}

	/**
	 * 描述：获取AbsListView的高度.
	 *
	 * @param absListView
	 *            the abs list view
	 * @param lineNumber
	 *            每行几个 ListView一行一个item
	 * @param verticalSpace
	 *            the vertical space
	 * @return the abs list view height
	 */
	public static int getAbsListViewHeight(AbsListView absListView, int lineNumber, int verticalSpace) {
		int totalHeight = 0;
		int w = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		int h = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		absListView.measure(w, h);
		ListAdapter mListAdapter = absListView.getAdapter();
		if (mListAdapter == null) {
			return totalHeight;
		}

		int count = mListAdapter.getCount();
		if (absListView instanceof ListView) {
			for (int i = 0; i < count; i++) {
				View listItem = mListAdapter.getView(i, null, absListView);
				listItem.measure(w, h);
				totalHeight += listItem.getMeasuredHeight();
			}
			if (count == 0) {
				totalHeight = verticalSpace;
			} else {
				totalHeight = totalHeight + (((ListView) absListView).getDividerHeight() * (count - 1));
			}

		} else if (absListView instanceof GridView) {
			int remain = count % lineNumber;
			if (remain > 0) {
				remain = 1;
			}
			if (mListAdapter.getCount() == 0) {
				totalHeight = verticalSpace;
			} else {
				View listItem = mListAdapter.getView(0, null, absListView);
				listItem.measure(w, h);
				int line = count / lineNumber + remain;
				totalHeight = line * listItem.getMeasuredHeight() + (line - 1) * verticalSpace;
			}

		}
		return totalHeight;

	}

	/**
	 * 测量这个view 最后通过getMeasuredWidth()获取宽度和高度.
	 * 
	 * @param view
	 *            要测量的view
	 * @return 测量过的view
	 */
	public static void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * 获得这个View的宽度 测量这个view，最后通过getMeasuredWidth()获取宽度.
	 * 
	 * @param view
	 *            要测量的view
	 * @return 测量过的view的宽度
	 */
	public static int getViewWidth(View view) {
		measureView(view);
		return view.getMeasuredWidth();
	}

	/**
	 * 获得这个View的高度 测量这个view，最后通过getMeasuredHeight()获取高度.
	 * 
	 * @param view
	 *            要测量的view
	 * @return 测量过的view的高度
	 */
	public static int getViewHeight(View view) {
		measureView(view);
		return view.getMeasuredHeight();
	}

}
