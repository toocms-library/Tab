package com.toocms.tab.ui;

public class FragmentParam {

	public enum TYPE {
		ADD, REPLACE
	}

	public BaseFragment from;
	public Class<?> cls;
	public Object data;
	public TYPE type = TYPE.ADD;
	public boolean addToBackStack = true;

}
