package com.toocms.tab.toolkit;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;

/**
 * <p>
 * 一些常用的方法
 * 
 * @author Zero @date 2014-2-9<br>
 * @version 1.0<br>
 */
public class Commonly {

	/**
	 * 当字符串为null时所要显示的字符串
	 * 
	 * @param oldText
	 * @param newText
	 * @return String
	 */
	public static String getTextByNull(String oldText, String newText) {
		return TextUtils.isEmpty(oldText) ? newText : oldText;
	}

	/**
	 * 获取edittext的文字
	 * 
	 * @param editText
	 * @return <p>
	 *         String
	 */
	public static String getViewText(EditText editText) {
		return editText.getText().toString().trim();
	}

	public static Spanned stringToSpan(String src) {
		return src == null ? null : Html.fromHtml(src.replace("\n", "<br />"));
	}

	/**
	 * 给字符串添加颜色
	 * 
	 * @param src
	 * @param color
	 * @return String
	 */
	public static String colorFont(String src, String color) {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append("<font color=").append(color).append(">").append(src).append("</font>");
		return strBuf.toString();
	}

	public static String makeHtmlNewLine() {
		return "<br />";
	}

	/**
	 * 给字符串加空格
	 * 
	 * @param number
	 * @return String
	 */
	public static String makeHtmlSpace(int number) {
		final String space = "&nbsp;";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number; i++) {
			result.append(space);
		}
		return result.toString();
	}

}
