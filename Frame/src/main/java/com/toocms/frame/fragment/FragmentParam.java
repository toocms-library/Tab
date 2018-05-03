package com.toocms.frame.fragment;

import com.toocms.frame.ui.BaseFragment;

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
