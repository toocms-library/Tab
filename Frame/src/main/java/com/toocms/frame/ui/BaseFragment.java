package com.toocms.frame.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.toocms.frame.config.Constants;
import com.toocms.frame.config.WeApplication;
import com.toocms.frame.crash.CrashConfig;
import com.toocms.frame.crash.CrashLogSender;
import com.toocms.frame.crash.CrashLogStore;
import com.toocms.frame.fragment.IBaseFragement;
import com.toocms.frame.tool.AppManager;
import com.toocms.frame.ui.imageselector.SelectImageAty;
import com.toocms.frame.view.PromptInfo;
import com.toocms.frame.view.progress.ProgressDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import cn.zero.android.common.permission.PermissionGen;
import cn.zero.android.common.util.ListUtils;
import okhttp3.Call;

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
    private View progress;
    private View error;
    private ProgressDialog progressDialog;
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
    private boolean isShowContent;
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
        AppManager.instance = this;
        onCreateFragment(savedInstanceState);
        // 请求数据
        showContent = true;
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
        if (isVisible) {
            AppManager.instance = this;
        }
        showStatus("onResume");
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
        showContent = true;
        if (null != handler)
            handler.postDelayed(runnable, 300);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

    @Override
    public void onDestroy() {
        if (null != handler) handler = null;
        if (null != runnable) runnable = null;
        OkGo.getInstance().cancelTag(this);
        if (null != presenter) presenter.dettach();
        super.onDestroy();
    }

    @Override
    public void onComeIn(Object data) {
        mDataIn = data;
        showStatus("onComeIn");
    }

    @Override
    public void onLeave() {
        showStatus("onLeave");
    }

    @Override
    public void onBack(Object data) {
        showStatus("onBack");
    }

    @Override
    public void onStop() {
        super.onStop();
        showStatus("onStop");
    }

    protected void onVisible() {
        lazyLoad();
    }

    protected void onInvisible() {
    }

    private void showStatus(String status) {
        Log.d("test", String.format("%s %s", this.getClass().getName(), status));
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
        progress = View.inflate(getActivity(), R.layout.loading_content, null);
        progress.setOnClickListener(null);
        error = View.inflate(getActivity(), R.layout.layout_error, null);
        error.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                requestData();
                onResume();
                content.removeView(error);
            }
        });
        progressDialog = new ProgressDialog(getContext());
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
        application = (WeApplication) getActivity().getApplication();
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

    /**
     * 启动多图选择页面
     *
     * @param defaultDataArray 默认选择的图片的路径
     */
    public void startSelectMultiImageAty(ArrayList<String> defaultDataArray, int maxSelectCount) {
        Bundle bundle = new Bundle();
        bundle.putInt(SelectImageAty.EXTRA_SELECT_COUNT, maxSelectCount);
        if (!ListUtils.isEmpty(defaultDataArray)) {
            bundle.putStringArrayList(SelectImageAty.EXTRA_DEFAULT_SELECTED_LIST, defaultDataArray);
        }
        getContext().startActivityForResult(SelectImageAty.class, bundle, Constants.SELECT_IMAGE);
    }

    /**
     * 启动单图选择页面
     *
     * @param ratio 纵横比例xy
     */
    public void startSelectSignImageAty(int... ratio) {
        Bundle bundle = new Bundle();
        bundle.putInt(SelectImageAty.EXTRA_SELECT_MODE, SelectImageAty.MODE_SINGLE);
        if (ratio != null && ratio.length != 0) {
            bundle.putFloat(SelectImageAty.EXTRA_ASPECT_RATIO_X, ratio[0]);
            bundle.putFloat(SelectImageAty.EXTRA_ASPECT_RATIO_Y, ratio[1]);
        }
        getContext().startActivityForResult(SelectImageAty.class, bundle, Constants.SELECT_IMAGE);
    }

    /**
     * 获取选择图片页面返回的图片路径集合
     *
     * @param data
     * @return
     */
    protected ArrayList<String> getSelectImagePath(Intent data) {
        if (data != null)
            return data.getStringArrayListExtra(SelectImageAty.EXTRA_RESULT);
        return null;
    }

    // ================ 信息提示方式 ===================

    /**
     * 含有标题、内容和确定按钮的对话框 -- 一般用来显示信息
     *
     * @param title
     * @param message
     */
    protected void showDialog(String title, String message) {
        showDialog(title, message, "确定", null);
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
        builder = new AlertDialog.Builder(getActivity());
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, listener);
        builder.create().show();
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
        builder = new AlertDialog.Builder(getActivity());
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, positiveListener);
        builder.setNegativeButton(negativeText, negativeListener);
        builder.create().show();
    }

    /**
     * 带有item的Dialog
     *
     * @param title
     * @param itemsId
     * @param listener
     */
    protected void showItemsDialog(String title, int itemsId, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(getActivity());
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setItems(itemsId, listener);
        builder.create().show();
    }

    /**
     * 带有item的Dialog
     *
     * @param title
     * @param items
     * @param listener
     */
    protected void showItemsDialog(String title, CharSequence[] items, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(getActivity());
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setItems(items, listener);
        builder.create().show();
    }

    // ====================== Toast ==========================

    public void showToast(CharSequence text) {
        PromptInfo.getInstance().showToast(this, text);
    }

    public void showToast(int resId) {
        PromptInfo.getInstance().showToast(this, resId);
    }

    // ===================== 加载条 ==================

    private boolean isShowing = false;
    private boolean showContent; // 网络加载时是否显示content加载框
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showContent = false;
        }
    };

    private void showProgressContent() {
        if (isShowing) return;
        isShowContent = true;
        isShowing = true;
        content.addView(progress);
    }

    private void removeProgressContent() {
        if (isShowing) {
            isShowing = false;
            isShowContent = false;
            content.removeView(progress);
        }
    }

    private void showProgressDialog() {
        isShowContent = false;
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void removeProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // ====================== API回调方法 =======================

    public void showProgress() {
        if (showContent) showProgressContent();
        else showProgressDialog();
    }

    public void removeProgress() {
        removeProgressContent();
        removeProgressDialog();
    }

    public void onException(String request, Throwable ex) {
        if (isShowContent) {
            int index = content.indexOfChild(error);
            if (index < 0) content.addView(error);
        } else {
            if (ex instanceof SocketException || ex instanceof InterruptedIOException || ex instanceof UnknownHostException || ex instanceof UnknownServiceException) {
                showToast(getString(R.string.network_anomaly));
            } else {
                showToast(getString(R.string.server_unknow_error));
            }
        }
        ex.printStackTrace();
        // 处理错误日志
        try {
            if (CrashConfig.isAllowReportToHost()) {
                CrashLogStore.saveLogToFile(application, request, ex, Thread.currentThread());
                CrashLogSender cls = new CrashLogSender(application);
                cls.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        removeProgress();
    }
}
