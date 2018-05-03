package cn.zero.android.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * �Զ�����Ի�����RelativeLayout, ������IOS�Ļ���ɾ��ҳ��Ч������Ҫʹ�� �˹��ܵ�ʱ����Ҫ����Activity�Ķ��㲼������ΪSildingFinishLayout
 *
 * @author Zero
 */
public class SildingFinishLayout extends RelativeLayout {
	/**
	 * SildingFinishLayout���ֵĸ�����
	 */
	private ViewGroup mParentView;
	/**
	 * ��������С����
	 */
	private int mTouchSlop;
	/**
	 * ���µ��X���?
	 */
	private int downX;
	/**
	 * ���µ��Y���?
	 */
	private int downY;
	/**
	 * ��ʱ�洢X���?
	 */
	private int tempX;
	/**
	 * ������
	 */
	private Scroller mScroller;
	/**
	 * SildingFinishLayout�Ŀ��?
	 */
	private int viewWidth;

	private boolean isSilding;

	private OnSildingFinishListener onSildingFinishListener;
	private boolean isFinish;

	public SildingFinishLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SildingFinishLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScroller = new Scroller(context);
	}

	/**
	 * �¼����ز���
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = tempX = (int) ev.getRawX();
			downY = (int) ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) ev.getRawX();
			// �������������SildingFinishLayout���������touch�¼�
			if (Math.abs(moveX - downX) > mTouchSlop && Math.abs((int) ev.getRawY() - downY) < mTouchSlop) {
				return true;
			}
			break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) event.getRawX();
			int deltaX = tempX - moveX;
			tempX = moveX;
			if (Math.abs(moveX - downX) > mTouchSlop && Math.abs((int) event.getRawY() - downY) < mTouchSlop) {
				isSilding = true;
			}

			if (moveX - downX >= 0 && isSilding) {
				mParentView.scrollBy(deltaX, 0);
			}
			break;
		case MotionEvent.ACTION_UP:
			isSilding = false;
			if (mParentView.getScrollX() <= -viewWidth / 2) {
				isFinish = true;
				scrollRight();
			} else {
				scrollOrigin();
				isFinish = false;
			}
			break;
		}

		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			// ��ȡSildingFinishLayout���ڲ��ֵĸ�����
			mParentView = (ViewGroup) this.getParent();
			viewWidth = this.getWidth();
		}
	}

	/**
	 * ����OnSildingFinishListener, ��onSildingFinish()������finish Activity
	 * 
	 * @param onSildingFinishListener
	 */
	public void setOnSildingFinishListener(OnSildingFinishListener onSildingFinishListener) {
		this.onSildingFinishListener = onSildingFinishListener;
	}

	/**
	 * ����������
	 */
	private void scrollRight() {
		final int delta = (viewWidth + mParentView.getScrollX());
		// ����startScroll����������һЩ�����Ĳ���������computeScroll()�����е���scrollTo������item
		mScroller.startScroll(mParentView.getScrollX(), 0, -delta + 1, 0, Math.abs(delta));
		postInvalidate();
	}

	/**
	 * ��������ʼλ��
	 */
	private void scrollOrigin() {
		int delta = mParentView.getScrollX();
		mScroller.startScroll(mParentView.getScrollX(), 0, -delta, 0, Math.abs(delta));
		postInvalidate();
	}

	@Override
	public void computeScroll() {
		// ����startScroll��ʱ��scroller.computeScrollOffset()����true��
		if (mScroller.computeScrollOffset()) {
			mParentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();

			if (mScroller.isFinished() && isFinish) {

				if (onSildingFinishListener != null) {
					onSildingFinishListener.onSildingFinish();
				} else {
					// û������OnSildingFinishListener�������������ʵλ��?
					scrollOrigin();
					isFinish = false;
				}
			}
		}
	}

	public interface OnSildingFinishListener {
		void onSildingFinish();
	}

}
