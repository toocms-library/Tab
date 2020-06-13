package com.toocms.tab.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.lzy.okgo.OkGo;
import com.toocms.tab.R;
import com.toocms.tab.WeApplication;
import com.toocms.tab.toolkit.permission.PermissionGen;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;

import static android.view.View.GONE;

/**
 * 主Fragment类，继承此类之后才能使用BaseAty中的与Fragment交互的方法<br/>
 * 该类中的方法基本和{@link BaseActivity}中的方法相同，掌握了{@link BaseActivity}中的方法该类的用法也就基本掌握了
 *
 * @param <V> View层的实例
 * @param <T> Presenter层的实例
 * @author Zero
 */
public abstract class BaseFragment<V, T extends BasePresenter<V>> extends Fragment implements IBaseFragement {

    private FrameLayout titlebar;
    private FrameLayout content;
    private View divider; // Titlebar和Content之间的分割线
    protected View progress;
    protected View error;
    private AlertDialog.Builder builder;

    protected WeApplication application;
    public T presenter;

    /**
     * Activity中传输过来的参数，需进行类型强转
     */
    protected Object mDataIn;

    /**
     * 是否可见状态
     */
    private boolean isVisible;

    /**
     * 标志位，View已经初始化完成
     */
    private boolean isPrepared;

    /**
     * 是否第一次加载
     */
    private boolean isFirstLoad = true;
    protected boolean hasAnimiation = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preliminary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        View layout = inflater.inflate(R.layout.fgt_base, container, false);
        initControls(layout);
        setBasicContentView(getLayoutResId());
        // 判断是否有自定义标题栏
        setCustomTitlebar();
        ButterKnife.bind(this, layout);
        isPrepared = true;
        lazyLoad();
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreateFragment(savedInstanceState);
        // 请求数据
        requestData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

    @Override
    public void onDestroy() {
        OkGo.getInstance().cancelTag(this);
        if (null != presenter) presenter.dettach();
        super.onDestroy();
    }

    @Override
    public void onComeIn(Object data) {
        mDataIn = data;
    }

    @Override
    public void onLeave() {
    }

    @Override
    public void onBack(Object data) {
    }

    protected void onVisible() {
        lazyLoad();
    }

    protected void onInvisible() {
    }

    protected void lazyLoad() {
        if (!isPrepared || !isVisible || !isFirstLoad) {
            return;
        }
        isFirstLoad = false;
    }

    /**
     * 初始化控件
     */
    private void initControls(View layout) {
        // 加入fragment的标题栏
        titlebar = layout.findViewById(R.id.fgt_titlebar);
        content = layout.findViewById(R.id.content);
        divider = layout.findViewById(R.id.content_divider);
        // 给最底层的layout设置一个点击监听防止切换页面之后还会点击到别的页面的BUG
        content.setOnClickListener(null);
        progress = layout.findViewById(R.id.loading);
        progress.setOnClickListener(null);
        error = layout.findViewById(R.id.error);
        error.setOnClickListener(v -> {
            getContext().resetShowContent();
            error.setVisibility(GONE);
            requestData();
            onResume();
        });
    }

    /**
     * 设置content的子布局
     */
    private void setBasicContentView(int layoutResId) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutResId, null);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        content.addView(view);
    }

    /**
     * 当有自定义标题栏时把他放到标题栏控件中，以免把自定义的标题栏覆盖
     */
    private void setCustomTitlebar() {
        if (getTitlebarResId() != 0) {
            divider.setVisibility(View.VISIBLE);
            View view = content.findViewById(getTitlebarResId());
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(view);
                titlebar.addView(view);
                view.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
            }
        } else {
            titlebar.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
    }

    /**
     * 向用户展示信息前的准备工作在这个方法里处理
     */
    private void preliminary() {
        application = getContext().application;
        // 初始化数据
        initialized();
        // 初始化Presenter
        presenter = getPresenter();
        if (null != presenter) presenter.attach((V) this);
    }

    // ============================= 回调函数 ===========================

    protected abstract void onCreateFragment(@Nullable Bundle savedInstanceState);

    /**
     * 用于设置页面布局
     */
    protected abstract int getLayoutResId();

    /**
     * 初始化数据
     */
    protected abstract void initialized();

    /**
     * 初始化Presenter
     *
     * @return
     */
    protected abstract T getPresenter();

    /**
     * 请求数据，需要写showProgressContent/showProgressDialog<br/>
     * 若调用showProgressContent的情况下出现联网失败则会显示ERROR界面
     */
    protected abstract void requestData();

    /**
     * <font color=#ff0000>如页面中需要添加自定义标题栏则重写此方法，否则可以不用重写</font><br/>
     * 当页面中有自定义标题栏时返回他的控件，没有时返回null即可
     */
    protected int getTitlebarResId() {
        return 0;
    }

    public FrameLayout getContent() {
        return content;
    }

    public BaseActivity getContext() {
        return (BaseActivity) getActivity();
    }

    public void onClick(View view) {
    }

    // ================== 权限 ===================

    /**
     * 请求单个权限
     *
     * @param requestCode 请求标识
     * @param permission  权限
     */
    protected void requestPermissions(int requestCode, String permission) {
        PermissionGen.needPermission(this, requestCode, permission);
    }

    /**
     * 请求多个权限
     *
     * @param requestCode 请求标识
     * @param permissions 权限数组
     */
    protected void requestPermissions(int requestCode, String... permissions) {
        PermissionGen.needPermission(this, requestCode, permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    // =============== 启动Activity =================

    /**
     * 启动一个Activity
     *
     * @param className 将要启动的Activity的类名
     * @param options   传到将要启动Activity的Bundle，不传时为null
     */
    protected void startActivity(Class<?> className, Bundle options) {
        Intent intent = new Intent(getActivity(), className);
        if (options != null) {
            intent.putExtras(options);
        }
        startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (hasAnimiation) {
            getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
    }

//    /**
//     * 启动多图选择页面
//     *
//     * @param defaultDataArray 默认选择的图片的路径
//     */
//    public void startSelectMultiImageAty(ArrayList<String> defaultDataArray, int maxSelectCount) {
//        Bundle bundle = new Bundle();
//        bundle.putInt(SelectImageAty.EXTRA_SELECT_COUNT, maxSelectCount);
//        if (!ListUtils.isEmpty(defaultDataArray)) {
//            bundle.putStringArrayList(SelectImageAty.EXTRA_DEFAULT_SELECTED_LIST, defaultDataArray);
//        }
//        getContext().startActivityForResult(SelectImageAty.class, bundle, Constants.SELECT_IMAGE);
//    }
//
//    /**
//     * 启动单图选择页面
//     *
//     * @param ratio 纵横比例xy
//     */
//    public void startSelectSignImageAty(int... ratio) {
//        Bundle bundle = new Bundle();
//        bundle.putInt(SelectImageAty.EXTRA_SELECT_MODE, SelectImageAty.MODE_SINGLE);
//        if (ratio != null && ratio.length != 0) {
//            bundle.putFloat(SelectImageAty.EXTRA_ASPECT_RATIO_X, ratio[0]);
//            bundle.putFloat(SelectImageAty.EXTRA_ASPECT_RATIO_Y, ratio[1]);
//        }
//        getContext().startActivityForResult(SelectImageAty.class, bundle, Constants.SELECT_IMAGE);
//    }
//
//    /**
//     * 获取选择图片页面返回的图片路径集合
//     *
//     * @param data
//     * @return
//     */
//    protected ArrayList<String> getSelectImagePath(Intent data) {
//        if (data != null)
//            return data.getStringArrayListExtra(SelectImageAty.EXTRA_RESULT);
//        return null;
//    }

    // ================ 信息提示方式 ===================

    /**
     * 含有标题、内容和确定按钮的对话框 -- 一般用来显示信息
     *
     * @param title
     * @param message
     */
    protected void showDialog(String title, String message) {
        getContext().showDialog(title, message);
    }

    /**
     * 含有标题、内容和一个按钮的对话框
     *
     * @param title
     * @param message
     * @param positiveText
     * @param listener
     */
    protected void showDialog(String title, String message, String positiveText, DialogInterface.OnClickListener listener) {
        getContext().showDialog(title, message, positiveText, listener);
    }

    /**
     * 含有标题、内容和两个按钮的对话框
     *
     * @param title
     * @param message
     * @param positiveText
     * @param negativeText
     * @param positiveListener
     * @param negativeListener
     */
    protected void showDialog(String title, String message, String positiveText, String negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        getContext().showDialog(title, message, positiveText, negativeText, positiveListener, negativeListener);
    }

    /**
     * 带有item的Dialog
     *
     * @param title
     * @param itemsId
     * @param listener
     */
    protected void showItemsDialog(String title, int itemsId, DialogInterface.OnClickListener listener) {
        getContext().showItemsDialog(title, itemsId, listener);
    }

    /**
     * 带有item的Dialog
     *
     * @param title
     * @param items
     * @param listener
     */
    protected void showItemsDialog(String title, String[] items, DialogInterface.OnClickListener listener) {
        getContext().showItemsDialog(title, items, listener);
    }

    // ====================== Toast ==========================

    /**
     * 弹出提示信息
     *
     * @param text 提示文本
     */
    public void showToast(String text) {
        getContext().showToast(text);
    }

    /**
     * 弹出提示信息
     *
     * @param resId 提示文本的ID
     */
    public void showToast(@StringRes int resId) {
        getContext().showToast(resId);
    }

    /**
     * 弹出成功提示信息
     *
     * @param text 提示文本
     */
    public void showSuccessToast(String text) {
        getContext().showSuccessToast(text);
    }

    /**
     * 弹出成功提示信息
     *
     * @param resId 提示文本的ID
     */
    public void showSuccessToast(@StringRes int resId) {
        getContext().showSuccessToast(resId);
    }

    /**
     * 弹出错误提示信息
     *
     * @param text 提示文本
     */
    public void showErrorToast(String text) {
        getContext().showErrorToast(text);
    }

    /**
     * 弹出错误提示信息
     *
     * @param resId 提示文本的ID
     */
    public void showErrorToast(@StringRes int resId) {
        getContext().showErrorToast(resId);
    }

    /**
     * 弹出警告提示信息
     *
     * @param text 提示文本
     */
    public void showWarningToast(String text) {
        getContext().showWarningToast(text);
    }

    /**
     * 弹出警告提示信息
     *
     * @param resId 提示文本的ID
     */
    public void showWarningToast(@StringRes int resId) {
        getContext().showWarningToast(resId);
    }

    // ===================== 加载条 ==================

    /**
     * 显示全屏/局部加载条，已自动处理逻辑
     */
    public void showProgress() {
        getContext().showProgress();
    }

    /**
     * 移除全屏/局部加载条
     */
    public void removeProgress(Object tag) {
        getContext().removeProgress(tag);
    }
}
