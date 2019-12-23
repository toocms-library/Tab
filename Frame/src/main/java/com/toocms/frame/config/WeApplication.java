package com.toocms.frame.config;

import android.app.Application;
import android.service.autofill.Dataset;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.DBCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.toocms.frame.crash.CrashConfig;
import com.toocms.frame.crash.CrashReport;
import com.toocms.frame.crash.VerificationService;
import com.toocms.frame.tool.AppManager;
import com.toocms.frame.tool.Toolkit;
import com.toocms.frame.ui.BuildConfig;
import com.umeng.commonsdk.UMConfigure;

import org.xutils.x;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import cn.zero.android.common.util.DigestUtils;
import cn.zero.android.common.util.FileManager;
import cn.zero.android.common.util.GSONUtils;
import cn.zero.android.common.util.PreferencesUtils;
import cn.zero.android.common.util.StringUtils;
import okhttp3.OkHttpClient;

/**
 * Application类初始化配置
 *
 * @author Zero
 * @version 1.0
 * @date 2014年7月31日
 */
public class WeApplication extends Application {

    private final long REQUEST_TIMEOUT = 15000;      //网络请求超时时间
    private final String PREF_USERINFO = "user_info";

    /**
     * Application的实例
     */
    private volatile static WeApplication instance;

    /**
     * 框架中用来存储用户信息的Map
     */
    private Map<String, String> userInfo;

    /**
     * 用户信息实体类的接口
     */
    private IUser user;

    /**
     * 定位信息存储Map
     */
    private Map<String, String> locationInfo;

    /**
     * 该方法因为是整个程序的入口，所以主要就是初始化数据
     * 1、xUtils的初始化，说是xUtils其实只剩下Database模块了以及添加了{@link Dataset}的初始化
     * 2、{@link #instance}的初始化
     * 3、网络框架{@link OkGo}的初始化
     * 4、崩溃异常捕捉器{@link CrashReport}的初始化
     * 5、验证框架可用性，当服务器中存在项目包名时才可用，所以当新建一个项目时需要往服务器添加包名
     * 6、{@link #start()}方法的调用
     * 7、初始化用户信息{@link #initUserInfo()}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化XUtils
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
        instance = this;
        // 初始化OkGo
        initOkGo();
        // 初始化崩溃异常捕捉器
        CrashReport.init(this);
        // 初始化Umeng
        UMConfigure.init(this,
                x.dataSet().getAppConfig().getUmengAppkey(),
                "Umeng",
                UMConfigure.DEVICE_TYPE_PHONE,
                x.dataSet().getAppConfig().getUmengPushSecret());
        // 开启打印日志
        try {
            UMConfigure.setLogEnabled(!CrashConfig.isAllowReportToHost());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 初始化第三方Jar包
        x.dataSet().getAppConfig().initJarForWeApplication(this);
        // 验证可用性
        VerificationService.getInstance().verification();
        // 初始化数据线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 初始化信息
                start();
                // 初始化用户信息
                initUserInfo();
            }
        }).start();
    }

    /**
     * 获取Application示例
     *
     * @return WeApplication
     */
    public static WeApplication getInstance() {
        return instance;
    }

    /**
     * 初始化OkGo
     */
    private void initOkGo() {
        // 全局请求头部
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json");
        // 全局参数
        HttpParams params = new HttpParams();
        params.put("terminal", "1");  // 终端
        params.put("token", DigestUtils.md5("toocms")); // token
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // log相关
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor("网络信息");
        interceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY); // log打印级别，决定了log显示的详细程度
        interceptor.setColorLevel(Level.INFO); // log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(interceptor); // 添加OkGo默认debug日志
        // 超时时间设置，10秒
        builder.readTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);   //全局的连接超时时间
        // 自动管理cookie
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this))); // 使用数据库保持cookie，如果cookie不过期，则一直有效
        // https相关设置

        // 其他配置
        OkGo.getInstance().init(this)                                                        // 调用初始化
                .setOkHttpClient(builder.build())                                           // 设置OkHttpClient
                .setCacheMode(CacheMode.NO_CACHE)                              // 不使用缓存
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)           // 永不过期
                .setRetryCount(3)                                                               // 3次重连
                .addCommonHeaders(headers)                                              // 添加全局请求头部
                .addCommonParams(params);                                                // 添加全局请求参数
    }

    /**
     * 获取屏幕的宽高以及创建文件夹<br/>
     * 屏幕的宽高属性{@link Settings#displayWidth}、{@link Settings#displayHeight}
     */
    private void start() {
        // 获得屏幕宽度（像素）
        Settings.displayWidth = Toolkit.getScreenWidth(this);
        // 获得屏幕高度（像素）
        Settings.displayHeight = Toolkit.getScreenHeight(this);
    }

    private void finish() {
        // 关闭程序运行的所有activity
        AppManager.getInstance().AppExit(this);
        // 调用系统垃圾处理
        System.gc();
    }

    /**
     * 初始化用户信息
     */
    private void initUserInfo() {
        try {
            String jsonStr = PreferencesUtils.getString(this, PREF_USERINFO);
            if (!StringUtils.isEmpty(jsonStr)) {
                user = (IUser) GSONUtils.fromJson(jsonStr, Class.forName(x.app().getPackageName() + ".config.User"));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("缺少：" + x.app().getPackageName() + ".config.User类");
        }
    }

    /**
     * 获取用户信息
     *
     * @param <T>
     * @return
     */
    public <T extends IUser> T getUserInfo() {
        return (T) user;
    }

    /**
     * 设置用户信息
     *
     * @param user
     */
    public void setUserInfo(IUser user) {
        this.user = user;
        String json = GSONUtils.toJson(user);
        PreferencesUtils.putString(this, PREF_USERINFO, json);
    }

    /**
     * 清除用户信息
     */
    public void clearUserInfo() {
        user = null;
        PreferencesUtils.putString(this, PREF_USERINFO, "");
    }
}
