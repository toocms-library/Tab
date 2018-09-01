package com.toocms.frame.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.lzy.okgo.OkGo;
import com.toocms.frame.config.Constants;
import com.toocms.frame.config.IAppConfig;
import com.toocms.frame.config.WeApplication;
import com.toocms.frame.crash.CrashConfig;
import com.toocms.frame.crash.CrashLogSender;
import com.toocms.frame.crash.CrashLogStore;
import com.toocms.frame.fragment.FragmentParam;
import com.toocms.frame.fragment.FragmentParam.TYPE;
import com.toocms.frame.image.ImageLoader;
import com.toocms.frame.tool.AppManager;
import com.toocms.frame.tool.SystemBarTintManager;
import com.toocms.frame.ui.imageselector.SelectImageAty;
import com.toocms.frame.view.PromptInfo;
import com.toocms.frame.view.progress.ProgressDialog;
import com.umeng.analytics.MobclickAgent;

import org.xutils.x;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cn.zero.android.common.permission.PermissionGen;
import cn.zero.android.common.util.ListUtils;
import cn.zero.android.common.view.TooCMSToolbar;
import cn.zero.android.common.view.ucrop.model.CropType;
import okhttp3.Call;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * 页面基类
 * <p>
 * 1、该类实现方式Toolbar（{@link #mActionBar}--标题栏）||FrameLayout（{@link #titlebar}--自定义标题栏）+FrameLayout（{@link #content}--内容区域），
 * 需在子类中通过重写回调方法{@link #getLayoutResId()}来指定布局文件，
 * 指定的布局会通过{@link #setBasicContentView(int)}方法添加到{@link #content}中<br/><br/>
 * 2、若Toolbar无法实现需求则可以通过自定义标题栏来实现，自定义标题栏在XML中可以正常编写，但需在子类中重写{@link #getTitlebarResId()}
 * 方法，返回值为自定义标题栏的ID，实现方式是判断如果该方法返回值不为空的情况下把自定义标题栏的View从之前的Layout中移除，添加到自定义标题栏{@link #titlebar}
 * 中，否则将自定义标题栏{@link #titlebar}隐藏，同时也防止Toast的弹出方式为{@link Snackbar}的方式时弹出位置在自定义标题栏上方的问题<br/><br/>
 * 3、该类中的回调方法执行顺序如下：{@link #getLayoutResId()} -> {@link #initialized()} -> {@link #getPresenter()} -> {@link #onCreateActivity(Bundle)} -> {@link #requestData()}<br/><br/>
 * 4、<a href="http://twp.toocms.com/index.php/Blog/details/id/60">框架集成文档</a>
 *
 * @param <V> View层的实例
 * @param <T> Presenter层的实例
 * @author Zero
 */
public abstract class BaseActivity<V, T extends BasePresenter<V>> extends AppCompatActivity {

    private final String KEY_FROM = "KEY_FROM";

    /**
     * Toolbar控件
     * <p>
     * 当页面中不需要该控件时可在子类中调用mActionBar.hide()将其隐藏
     */
    protected TooCMSToolbar mActionBar;

    /**
     * Toolbar中的标题
     * <p>
     * 1、可调用{@link #setTitle(CharSequence)}更改标题<br/>
     * 2、可设置{@link IAppConfig#isShowTitleCenter()}回调来达到标题居中/居左的目的
     */
    private TextView mTitle;

    /**
     * Toolbar下方的内容显示区域，子类中指定的布局会被add到这里
     */
    private FrameLayout content;

    /**
     * 错误信息提示View，当网络请求回调中的代码出现Crash时该View会以覆盖的方式显示在{@link #content}的最上层，子类无需调用
     */
    private View error;

    /**
     * 自定义标题栏，子类无需调用
     */
    private FrameLayout titlebar;

    /**
     * Toolbar和Content之间的分割线，子类无需调用
     */
    private View divider;

    /**
     * 全屏加载条控件，子类无需调用
     */
    private View progress;

    /**
     * 中间加载条控件，子类无需调用
     */
    private ProgressDialog progressDialog;

    /**
     * 对话框构建器，子类无需调用
     */
    private AlertDialog.Builder builder;

    /**
     * {@link WeApplication}类的对象，子类中可通过该变量直接调用其中的方法
     */
    protected WeApplication application;

    /**
     * MVP中的Presenter层的对象
     */
    public T presenter;

    /**
     * 当前正在显示的Fragment，用于Activity中的Fragment切换
     */
    private BaseFragment currentFragment;

    /**
     * Activity中所有的Fragment的list
     */
    private List<BaseFragment> fragments;

    /**
     * 启动页面时是否带页面切换（startActivity/finish）动画的判断条件，在某些情况下子类中需要更改该变量的值去完成页面逻辑
     */
    protected boolean hasAnimiation = true;

    /**
     * 当网络请求回调中的代码出现Crash时标识是否显示{@link #error}，逻辑为如果出现Crash时加载条显示的是全屏加载条{@link #progress}的话，
     * 就会显示{@link #error}，否则以Toast方式提示，子类无需调用
     */
    private boolean isShowContent;

    /**
     * 上一个页面的类名，也就是从哪个类中跳转到该页面的
     */
    private String from; // 上一个页面

    /**
     * Activity的onCreate生命周期<br/>
     * 其中实现：<br/>
     * 1、初始化状态栏的色调<br/>
     * &nbsp&nbsp&nbsp&nbsp&nbsp通过子类重写{@link #getStatusBarColor()}回调方法返回状态栏背景颜色值<br/>
     * &nbsp&nbsp&nbsp&nbsp&nbsp通过子类重写{@link #getTitleColor()}回调方法返回状态文字是黑色/白色<br/>
     * &nbsp&nbsp&nbsp&nbsp&nbsp通过子类重写{@link #translucentStatusBar()}回调方法返回状态栏是否透明<br/>
     * 2、初始化{@link OkGo}中Context变量的设定<br/>
     * 3、将该Activity加入到堆栈列中<br/>
     * 4、竖屏锁定<br/>
     * 5、设置框架基础布局<br/>
     * 6、初始化Toolbar以及判断标题是否居中显示<br/>
     * 7、初始化框架基础布局中的控件<br/>
     * 8、将子类布局add到{@link #content}中<br/>
     * 9、{@link ButterKnife}控件注入框架的初始化<br/>
     * 10、自定义标题栏的判断以及逻辑<br/>
     * 11、{@link #preliminary()}方法的调用<br/>
     * 12、{@link #onCreateActivity(Bundle)}方法的调用<br/>
     * 13、全屏加载条{@link #progress}逻辑处理<br/>
     * 14、网络请求{@link #requestData()}回调方法调用
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
        }
        super.onCreate(savedInstanceState);
        initSystemBarTint();
        // 管理Activity
        AppManager.instance = this;
        AppManager.getInstance().addActivity(this);
        // 竖屏锁定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.aty_base);
        // 初始化Toolbar
        mActionBar = findViewById(R.id.toolbar);
        mActionBar.setTitle("");
        mTitle = mActionBar.findViewById(R.id.title);
        Toolbar.LayoutParams params = (Toolbar.LayoutParams) mTitle.getLayoutParams();
        params.gravity = x.dataSet().getAppConfig().isShowTitleCenter() ? Gravity.CENTER : Gravity.LEFT | Gravity.CENTER_VERTICAL;
        setSupportActionBar(mActionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // 初始化控件
        initControls();
        // 设置子布局
        setBasicContentView(getLayoutResId());
        // 开启控件注入机制
        ButterKnife.bind(this);
        // 判断是否有自定义标题栏
        setCustomTitlebar();
        // 向用户展示信息前的准备工作在这个方法里处理
        preliminary();
        onCreateActivity(savedInstanceState);
        // 请求数据
        showContent = true;
        requestData();
    }

    /**
     * Activity的onDestroy生命周期<br/>
     * 其中实现：onDestroy
     * 1、{@link #handler}的内存释放<br/>
     * 2、{@link #runnable}的内存释放<br/>
     * 3、该Activity在堆栈列中的销毁以及内存释放<br/>
     * 4、网络请求的取消<br/>
     * 5、View层的释放
     */
    @Override
    protected void onDestroy() {
        if (null != handler) handler = null;
        if (null != runnable) runnable = null;
        AppManager.getInstance().killActivity(this);
        //Activity销毁时，取消网络请求
        OkGo.getInstance().cancelTag(this);
        // 释放view层
        if (null != presenter) presenter.dettach();
        super.onDestroy();
    }

    /**
     * Activity的onResume生命周期<br/>
     * 其中实现：<br/>
     * 1、{@link OkGo}中Context变量的设定<br/>
     * 2、{@link #divider}分割线的显示与否<br/>
     * 3、友盟时长统计<br/>
     * 4、全屏加载条显示逻辑
     */
    @Override
    protected void onResume() {
        super.onResume();
        AppManager.instance = this;
        // 设置分割线显示
        if (getTitlebarResId() != 0 || mActionBar.isShowing())
            divider.setVisibility(VISIBLE);
        // 统计页面
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
        // 统计时长
        MobclickAgent.onResume(this);
        showContent = true;
        if (null != handler)
            handler.postDelayed(runnable, 300);
    }

    /**
     * Activity的onPause生命周期<br/>
     * 其中实现：友盟页面时长统计暂停<br/>
     */
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
        MobclickAgent.onPause(this);
    }

    /**
     * 实现点击空白处隐藏键盘逻辑
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null) {
                if (getCurrentFocus().getWindowToken() != null) {
                    mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 初始化框架基本布局中的控件
     */
    private void initControls() {
        titlebar = findViewById(R.id.aty_titlebar);
        content = findViewById(R.id.content);
        divider = findViewById(R.id.content_divider);
        progress = View.inflate(this, R.layout.loading_content, null);
        progress.setOnClickListener(null);
        error = View.inflate(this, R.layout.layout_error, null);
        error.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                requestData();
                onResume();
                content.removeView(error);
            }
        });
        progressDialog = new ProgressDialog(this);
    }

    /**
     * 设置{@link #content}的子布局
     *
     * @param layoutResId
     */
    private void setBasicContentView(int layoutResId) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View main = inflater.inflate(layoutResId, null);
        content.addView(main);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        main.setLayoutParams(layoutParams);
    }

    /**
     * 自定义标题栏的处理，
     * 当有自定义标题栏时把他放到标题栏控件中，以免把自定义的标题栏覆盖
     */
    private void setCustomTitlebar() {
        if (getTitlebarResId() != 0) {
            mActionBar.hide();
            View view = content.findViewById(getTitlebarResId());
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(view);
                titlebar.addView(view);
                view.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
            }
        } else {
            titlebar.setVisibility(GONE);
        }
    }

    /**
     * 向用户展示信息前的准备工作在这个方法里处理<br/>
     * 其中包括：<br/>
     * 1、{@link #application}的初始化<br/>
     * 2、{@link #glide}的初始化<br/>
     * 3、{@link #from}的初始化<br/>
     * 4、{@link #initialized()}方法的调用<br/>
     * 5、{@link #presenter}的初始化以及与View层的关联<br/>
     */
    private void preliminary() {
        application = (WeApplication) getApplication();
        // 初始化页面标识
        initFrom();
        // 初始化数据
        initialized();
        // 初始化Presenter
        presenter = getPresenter();
        if (null != presenter) presenter.attach((V) this);
    }

    /**
     * 初始化页面标识
     */
    private void initFrom() {
        if (null != getIntent().getExtras()) {
            if (getIntent().getExtras().containsKey(KEY_FROM)) {
                from = getIntent().getExtras().getString(KEY_FROM);
            }
        }
    }

    /**
     * 获取页面跳转的类名
     *
     * @return 该页面的上一个页面的类名
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置Toolbar的标题
     *
     * @param title string类型标题
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    /**
     * 设置Toolbar的标题
     *
     * @param titleId stringId类型标题
     */
    @Override
    public void setTitle(int titleId) {
        mTitle.setText(getText(titleId));
    }

    // ============================= 回调函数 ===========================

    /**
     * 框架中代替{@link #onCreate(Bundle)}的回调方法
     *
     * @param savedInstanceState
     */
    protected abstract void onCreateActivity(@Nullable Bundle savedInstanceState);

    /**
     * 设置子类页面布局
     */
    protected abstract int getLayoutResId();

    /**
     * 初始化数据
     */
    protected abstract void initialized();

    /**
     * 初始化Presenter
     *
     * @return Presenter层类的实例
     */
    protected abstract T getPresenter();

    /**
     * 网络请求的回调方法，需要调用{@link #showProgressContent()}/{@link #showProgressDialog()}以显示加载条<br/>
     * 若调用{@link #showProgressContent()}的情况下出现联网失败则会显示{@link #error}界面
     */
    protected abstract void requestData();

    /**
     * 当子类页面中有自定义标题栏时重写该回调方法
     *
     * @return 返回自定义标题的LayoutId
     */
    protected int getTitlebarResId() {
        return 0;
    }

    /**
     * 当页面中有Fragment时子类重写该回调方法
     *
     * @return 返回Fragment的ContainerId
     */
    protected int getFragmentContainerId() {
        return 0;
    }

    /**
     * 对外开放的获取{@link #content}的接口方法
     *
     * @return 返回框架基础布局中的内容区域
     */
    public FrameLayout getContent() {
        return content;
    }

    // ============================== 事件 =============================

    /**
     * 控件点击事件<br/>
     * 使用方法，在方法顶部增加监听注入代码：<br/>
     * 1、单控件监听：@OnClick(R.id.xxx)<br/>
     * 2、多控件监听：@OnClick({R.id.xxx,R.id.xxx})，之后在方法体中去做switch判断<br/>
     * 3、也可以自定义一个方法按照上面的步骤，需要注意的是方法必须加上View参数
     *
     * @param view
     */
    public void onClick(View view) {
    }

    // ============================= 状态栏 ============================

    /**
     * 子类可以重写以改变状态栏文字颜色
     *
     * @return true - 黑色，false - 白色，默认为false
     */
    protected boolean isStatusBarDark() {
        return false;
    }

    /**
     * 子类重写以改变状态栏颜色
     *
     * @return 状态栏颜色，默认颜色值为Toolbar背景颜色
     */
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.action_bg);
    }

    /**
     * 子类可以重写以决定是否使用透明状态栏
     *
     * @return true - 透明，false - 不透明，默认为false
     */
    protected boolean translucentStatusBar() {
        return false;
    }

    /**
     * 初始化状态栏的色调
     */
    private void initSystemBarTint() {
        Window window = getWindow();
        if (translucentStatusBar()) {
            // 设置状态栏全透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                int visibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isStatusBarDark()) {
                    visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                window.getDecorView().setSystemUiVisibility(visibility);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            return;
        }
        // 沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0以上使用原生方法
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getStatusBarColor());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4-5.0使用三方工具类，有些4.4的手机有问题，这里为演示方便，不使用沉浸式
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(getStatusBarColor());
        }
        // 更改状态栏文字颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                int visibility = decorView.getSystemUiVisibility();
                if (isStatusBarDark()) {
                    visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(visibility);
            }
        }
    }

    // ============================= 按键、Toolbar菜单事件 ============================

    /**
     * 系统按键监听回调方法，实现了屏蔽系统菜单按钮事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // do nothing
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Toolbar创建菜单回调方法，实现将其返回true，以至于子类中重写该方法时默认为返回true
     *
     * @param menu
     * @return 默认返回true，以让Toolbar菜单显示
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Toolbar菜单项点击事件，实现点击Toolbar的返回按钮关闭该Activity的逻辑，子类若需实现其他逻辑同样重写该方法即可
     *
     * @param item
     * @return 父类方法
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
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

    /**
     * 获取用户权限结果的回调，该方法无需重写<br/>
     * <a href="http://twp.toocms.com/index.php/Blog/details/id/62">使用方法</a>
     *
     * @param requestCode  请求标识
     * @param permissions  请求的权限
     * @param grantResults 授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    // ============================== 启动Activity ==============================

    /**
     * 启动一个Activity
     *
     * @param className 将要启动的Activity的类名
     * @param options   传到将要启动Activity的Bundle，不传时为null
     */
    public void startActivity(Class<?> className, Bundle options) {
        Intent intent = new Intent(this, className);
        intent.putExtra(KEY_FROM, getClass().getSimpleName());
        if (options != null) {
            intent.putExtras(options);
        }
        startActivity(intent);
    }

    /**
     * 启动一个有会返回值的Activity
     *
     * @param className   将要启动的Activity的类名
     * @param options     传到将要启动Activity的Bundle，不传时为null
     * @param requestCode 请求码
     */
    public void startActivityForResult(Class<?> className, Bundle options, int requestCode) {
        Intent intent = new Intent(this, className);
        intent.putExtra(KEY_FROM, getClass().getSimpleName());
        if (options != null) {
            intent.putExtras(options);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * 系统启动Activity的方法，重写实现启动Activity时更改滑入动画
     *
     * @param intent
     */
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (hasAnimiation) {
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
    }

    /**
     * 系统销毁Activity的方法，重写实现销毁Activity时更改滑出动画<br/>
     * 当退出App时需将{@link #hasAnimiation}的值设置为false以确保用系统默认动画退出App
     */
    @Override
    public void finish() {
        super.finish();
        if (hasAnimiation) {
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
        }
    }

    /**
     * 启动多图选择页面，<a href="http://twp.toocms.com/index.php/Blog/details/id/57">使用方法</a>
     *
     * @param defaultDataArray 已有图片的路径集合
     * @param maxSelectCount   最大可选图片数量
     */
    public void startSelectMultiImageAty(ArrayList<String> defaultDataArray, int maxSelectCount) {
        Bundle bundle = new Bundle();
        bundle.putInt(SelectImageAty.EXTRA_SELECT_COUNT, maxSelectCount);
        if (!ListUtils.isEmpty(defaultDataArray)) {
            bundle.putStringArrayList(SelectImageAty.EXTRA_DEFAULT_SELECTED_LIST, defaultDataArray);
        }
        startActivityForResult(SelectImageAty.class, bundle, Constants.SELECT_IMAGE);
    }

    /**
     * 启动单图选择页面，<a href="http://twp.toocms.com/index.php/Blog/details/id/57">使用方法</a>
     *
     * @param ratio 纵横比例xy
     */
    public void startSelectSignImageAty(int... ratio) {
        startSelectSignImageAtyByCropType(CropType.TYPE_SQUARE, ratio);
    }

    /**
     * 启动单图选择页面并指定裁剪方式
     *
     * @param cropType
     * @param ratio
     */
    public void startSelectSignImageAtyByCropType(int cropType, int... ratio) {
        Bundle bundle = new Bundle();
        bundle.putInt(SelectImageAty.EXTRA_SELECT_MODE, SelectImageAty.MODE_SINGLE);
        bundle.putInt(SelectImageAty.EXTRA_CROP_TYPE, cropType);
        if (cropType == CropType.TYPE_SQUARE && ratio != null && ratio.length != 0) {
            bundle.putFloat(SelectImageAty.EXTRA_ASPECT_RATIO_X, ratio[0]);
            bundle.putFloat(SelectImageAty.EXTRA_ASPECT_RATIO_Y, ratio[1]);
        }
        startActivityForResult(SelectImageAty.class, bundle, Constants.SELECT_IMAGE);
    }

    /**
     * 获取选择图片页面返回的图片路径集合<br/>
     * 在{@link #onActivityResult(int, int, Intent)}中调用，配合{@link #startSelectSignImageAty(int...)}/{@link #startSelectMultiImageAty(ArrayList, int)}使用
     *
     * @param data 传入{@link #onActivityResult(int, int, Intent)}中的Intent参数
     * @return 选择图片的本地路径集合
     */
    public ArrayList<String> getSelectImagePath(Intent data) {
        if (data != null)
            return data.getStringArrayListExtra(SelectImageAty.EXTRA_RESULT);
        return null;
    }

    /**
     * 系统接收结果的回调方法，实现了当返回该页面时OKGO的Context参数为该页面，否则会出现加载条不消失的问题
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppManager.instance = this;
    }

    // ================================ 启动Fragment =============================

    /**
     * 将Fragment以add的方式到该Activity中，并且是以加到返回堆栈的方式<br/>
     * 效果就是按返回按钮如果返回堆栈中有两个或以上Fragment时会显示前一个Fragment，类似Activity的返回效果
     *
     * @param cls  Fragment的类名
     * @param data 要传输的数据
     */
    public void pushFragmentToBackStatck(Class<?> cls, Object data) {
        FragmentParam param = new FragmentParam();
        param.cls = cls;
        param.data = data;
        param.addToBackStack = true;
        processFragement(param);
    }

    /**
     * 将Fragment以add的方式加到该Activity中
     *
     * @param cls  Fragment的类名
     * @param data 要传输的数据
     */
    public void addFragment(Class<?> cls, Object data) {
        FragmentParam param = new FragmentParam();
        param.cls = cls;
        param.data = data;
        param.addToBackStack = false;
        processFragement(param);
    }

    /**
     * 将Fragment以replace的方式加到该Activity中
     *
     * @param cls  Fragment的类名
     * @param data 要传输的数据
     */
    public void replaceFragment(Class<?> cls, Object data) {
        FragmentParam param = new FragmentParam();
        param.cls = cls;
        param.data = data;
        param.type = TYPE.REPLACE;
        param.addToBackStack = false;
        processFragement(param);
    }

    /**
     * 跳转到堆栈中指定的Fragment
     *
     * @param cls  Fragment的类名
     * @param data 要传输的数据
     */
    public void goToFragment(Class<?> cls, Object data) {
        if (cls == null) {
            return;
        }
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(cls.toString());
        if (fragment != null) {
            currentFragment = fragment;
            fragment.onBack(data);
        }
        getSupportFragmentManager().popBackStackImmediate(cls.toString(), 0);
    }

    /**
     * 跳转到堆栈中最顶部的Fragment
     *
     * @param cls  Fragment的类名
     * @param data 要传输的数据
     */
    public void popTopFragment(Object data) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate();
        currentFragment = null;
        int cnt = fm.getBackStackEntryCount();
        String name = fm.getBackStackEntryAt(cnt - 1).getName();
        currentFragment = (BaseFragment) fm.findFragmentByTag(name);
        currentFragment.onBack(data);
    }

    /**
     * 跳转到堆栈中的根Fragment，类似{@link #popTopFragment(Object)}
     *
     * @param data 要传输的数据
     */
    public void popToRoot(Object data) {
        FragmentManager fm = getSupportFragmentManager();
        while (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
        }
        popTopFragment(data);
    }

    /**
     * 获取Fragment的tag，tag即为Fragment的类名
     *
     * @param param
     * @return
     */
    private String getFragmentTag(FragmentParam param) {
        StringBuilder sb = new StringBuilder(param.cls.toString());
        return sb.toString();
    }

    /**
     * Fragment加到Activity中的处理方法<br/>
     * 处理步骤（这里会有两个概念1、即将跳转的Fragment &nbsp&nbsp&nbsp  2、当前正在显示的Fragment）：<br/>
     * 1、首先通过{@link #getFragmentContainerId()}回调方法获取Fragment的容器Id<br/>
     * 2、通过传进来的{@link FragmentParam#cls}参数获取到Fragment的类名<br/>
     * 3、通过{@link #getFragmentTag(FragmentParam)}方法获取到Fragment的tag标识<br/>
     * 4、通过获取到的tag找到Fragment，该Fragment为即将跳转到的Fragment<br/>
     * 5、判断获取到的Fragment是否为null，如果为null的话创建一个新的实例<br/>
     * 6、Fragment执行{@link BaseFragment#onComeIn(Object)}方法，该方法为自定义，可以视作Fragment的自定义生命周期，同时所传的参数也会在该方法内被传入<br/>
     * 7、判断{@link #currentFragment}是否为null，若不为null的话该Fragment执行{@link BaseFragment#onLeave()}方法，该方法也可视作Fragment的自定义生命周期<br/>
     * 10、前置逻辑处理完毕，接下来就是把Fragment添加（{@link FragmentTransaction#add(int, Fragment, String)}）/替换（{@link FragmentTransaction#replace(int, Fragment, String)}）到该Activity中了<br/>
     * 11、首先判断{@link FragmentParam}参数中的type属性<br/>
     * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp若为{@link TYPE#ADD}的话先判断将被加入的Fragment是否已经被添加进来过，如添加过直接显示，如未添加过则将调用{@link FragmentTransaction#add(int, Fragment, String)}将其添加进来<br/>
     * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp若为{@link TYPE#REPLACE}的话则调用{@link FragmentTransaction#replace(int, Fragment, String)}方法将Fragment进行替换显示<br/>
     * 12、将{@link #currentFragment}的值变成之前即将跳转的Fragment<br/>
     * 13、通过{@link FragmentParam#addToBackStack}变量判断是不是要将Fragment加入到返回堆栈行列中
     *
     * @param param 自定义的Fragment的参数类
     */
    private void processFragement(FragmentParam param) {
        int containerId = getFragmentContainerId();
        Class<?> cls = param.cls;
        if (cls == null) {
            return;
        }
        try {
            String fragmentTag = getFragmentTag(param);
            BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (fragment == null) {
                fragment = (BaseFragment) cls.newInstance();
            }
            fragment.onComeIn(param.data);
            if (currentFragment != null) {
                currentFragment.onLeave();
            }

            if (fragments == null) {
                fragments = new ArrayList<>();
            }
            ListUtils.addDistinctEntry(fragments, fragment);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (param.type == TYPE.ADD) {
                if (fragment.isAdded()) {
                    for (BaseFragment lastFragment : fragments) {
                        ft.hide(lastFragment);
                    }
                    currentFragment.onPause();
                    ft.show(fragment);
                    fragment.onResume();
                } else {
                    ft.add(containerId, fragment, fragmentTag);
                }
            } else {
                ft.replace(containerId, fragment, fragmentTag);
            }

            currentFragment = fragment;
            if (param.addToBackStack) {
                ft.addToBackStack(fragmentTag);
            }
            ft.commitAllowingStateLoss();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // ============================ 信息提示方式 ============================

    /**
     * 含有标题、内容和确定按钮的对话框，一般用来显示提示信息<br/>
     * 确定按钮无监听事件，点击即对话框消失
     *
     * @param title   标题
     * @param message 内容
     */
    protected void showDialog(String title, String message) {
        showDialog(title, message, "确定", null);
    }

    /**
     * 含有标题、内容和一个按钮的对话框<br/>
     * 按钮可以设置文字以及监听事件
     *
     * @param title        标题
     * @param message      内容
     * @param positiveText 按钮文本
     * @param listener     按钮监听
     */
    protected void showDialog(String title, String message, String positiveText, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, listener);
        builder.create().show();
    }

    /**
     * 含有标题、内容和两个按钮的对话框<br/>
     * 按钮可以设置文字以及监听事件
     *
     * @param title            标题
     * @param message          内容
     * @param positiveText     右侧按钮文本
     * @param negativeText     左侧按钮文本
     * @param positiveListener 右侧按钮监听
     * @param negativeListener 左侧按钮监听
     */
    protected void showDialog(String title, String message, String positiveText, String negativeText, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        builder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, positiveListener);
        builder.setNegativeButton(negativeText, negativeListener);
        builder.create().show();
    }

    /**
     * 带有item的Dialog，无按钮
     *
     * @param title    标题
     * @param itemsId  文本数组的ID
     * @param listener item的点击监听
     */
    protected void showItemsDialog(String title, @ArrayRes int itemsId, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setItems(itemsId, listener);
        builder.create().show();
    }

    /**
     * 带有item的Dialog，无按钮
     *
     * @param title    标题
     * @param items    文字数组
     * @param listener item的点击监听
     */
    protected void showItemsDialog(String title, CharSequence[] items, DialogInterface.OnClickListener listener) {
        builder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(title)) builder.setTitle(title);
        builder.setItems(items, listener);
        builder.create().show();
    }

    // ====================== Toast ==========================

    /**
     * 以{@link Toast}/{@link Snackbar}的形式弹出提示信息<br/>
     * 通过设置{@link IAppConfig#isUseSnackBar()}回调方法来决定以哪种方式弹出
     *
     * @param text 提示文本
     */
    public void showToast(CharSequence text) {
        PromptInfo.getInstance().showToast(this, text);
    }

    /**
     * 以{@link Toast}/{@link Snackbar}的形式弹出提示信息<br/>
     * 通过设置{@link IAppConfig#isUseSnackBar()}回调方法来决定以哪种方式弹出
     *
     * @param text 提示文本的ID
     */
    public void showToast(@StringRes int resId) {
        PromptInfo.getInstance().showToast(this, resId);
    }

    // ====================== 加载条 ==========================

    /**
     * 全屏加载条是否显示的变量
     */
    private boolean isShowing = false;
    /**
     * 是否显示全屏加载条的标识
     */
    private boolean showContent;

    /**
     * 用来处理全屏加载条显示时机的逻辑
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showContent = false;
        }
    };

    /**
     * 配合{@link #runnable}使用
     */
    private Handler handler = new Handler();

    /**
     * 显示全屏加载条
     */
    private void showProgressContent() {
        if (isShowing) return;
        isShowContent = true;
        isShowing = true;
        content.addView(progress);
    }


    /**
     * 移除全屏加载条
     */
    private void removeProgressContent() {
        if (isShowing) {
            isShowing = false;
            isShowContent = false;
            content.removeView(progress);
        }
    }

    /**
     * 显示局部加载条
     */
    void showProgressDialog() {
        isShowContent = false;
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    /**
     * 移除局部加载条
     */
    void removeProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // ====================== API回调方法 =======================

    /**
     * 显示全屏/局部加载条，已自动处理逻辑
     */
    public void showProgress() {
        if (showContent) showProgressContent();
        else showProgressDialog();
    }

    /**
     * 移除全屏/局部加载条
     */
    public void removeProgress() {
        removeProgressContent();
        removeProgressDialog();
    }

    /**
     * 网络请求回调方法中处理Crash的方法<br/>
     * 1、如果是全屏加载条则显示{@link #error}层、否则以{@link #showToast(int)}方式提示<br/>
     * 2、根据异常类型提示网络异常/其他异常（其实代码写的都是网络异常）<br/>
     * 3、打印堆栈跟踪（异常信息）<br/>
     * 4、手机信息和异常日志的收集以及发送到服务器<br/>
     * 5、移除加载条
     *
     * @param ex
     */
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
