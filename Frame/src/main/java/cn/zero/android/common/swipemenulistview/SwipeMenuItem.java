package cn.zero.android.common.swipemenulistview;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * 滑动菜单选项<br/>
 * &nbsp;&nbsp;初始化SwipeMenuItem<br/>
 * SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());<br/>
 * &nbsp;&nbsp;设置item背景 <br/>
 * openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));<br/>
 * &nbsp;&nbsp;设置item宽度 <br/>
 * openItem.setWidth(dp2px(90)); <br/>
 * &nbsp;&nbsp;设置item标题 <br/>
 * openItem.setTitle("Open"); <br/>
 * &nbsp;&nbsp;设置item标题字号 <br/>
 * openItem.setTitleSize(18);<br/>
 * &nbsp;&nbsp;设置item标题文字颜色 <br/>
 * openItem.setTitleColor(Color.WHITE);<br/>
 * &nbsp;&nbsp;添加到menu <br/>
 * menu.addMenuItem(openItem);
 * 
 * @author Zero @date 2014�?10�?8�?
 * @version 1.0
 */
public class SwipeMenuItem {

	private int id;
	private Context mContext;
	private String title;
	private Drawable icon;
	private Drawable background;
	private int titleColor;
	private int titleSize;
	private int width;

	public SwipeMenuItem(Context context) {
		mContext = context;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTitleColor() {
		return titleColor;
	}

	public int getTitleSize() {
		return titleSize;
	}

	public void setTitleSize(int titleSize) {
		this.titleSize = titleSize;
	}

	public void setTitleColor(int titleColor) {
		this.titleColor = titleColor;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitle(int resId) {
		setTitle(mContext.getString(resId));
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public void setIcon(int resId) {
		this.icon = mContext.getResources().getDrawable(resId);
	}

	public Drawable getBackground() {
		return background;
	}

	public void setBackground(Drawable background) {
		this.background = background;
	}

	public void setBackground(int resId) {
		this.background = mContext.getResources().getDrawable(resId);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}
