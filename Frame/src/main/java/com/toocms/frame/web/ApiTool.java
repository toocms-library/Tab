package com.toocms.frame.web;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.toocms.frame.tool.AppManager;
import com.toocms.frame.tool.Toolkit;

import org.xutils.x;

import java.io.File;

/**
 * API接口调用类
 *
 * @author Zero
 * @version 4.0
 * @date 2017年03月09日
 */
public final class ApiTool<T> {

    /**
     * 以GET方式请求数据
     *
     * @param url         设置URL，当传入url为完整地址时直接以该地址为请求地址，否则以格式为主URL+url的拼接结果为请求地址
     * @param params      请求的参数
     * @param tag         请求的tag，主要用于取消对应的请求以及调用接口出现异常时的处理（区别Activity/Fragment）
     * @param apiListener 结果回调监听，<>里为实体类
     */
    public final void getApi(String url, HttpParams params, ApiListener<T> apiListener) {
        OkGo.<T>get(Toolkit.isUrl(url) ? url : x.dataSet().getUrls().getBaseUrl() + url)
                .tag(AppManager.instance)
                .params(params)
                .execute(apiListener);
    }

    /**
     * 以POST方式请求数据
     *
     * @param url         设置URL，当传入url为完整地址时直接以该地址为请求地址，否则以格式为主URL+url的拼接结果为请求地址
     * @param params      请求的参数
     * @param tag         请求的tag，主要用于取消对应的请求以及调用接口出现异常时的处理（区别Activity/Fragment）
     * @param apiListener 结果回调监听，<>里为实体类
     */
    public final void postApi(String url, HttpParams params, ApiListener<T> apiListener) {
        OkGo.<T>post(Toolkit.isUrl(url) ? url : x.dataSet().getUrls().getBaseUrl() + url)
                .tag(AppManager.instance)
                .params(params)
                .execute(apiListener);
    }

    /**
     * 请求下载文件
     *
     * @param url          设置URL，当传入url为完整地址时直接以该地址为请求地址，否则以格式为主URL+url的拼接结果为请求地址
     * @param params       请求的参数
     * @param tag          请求的tag，主要用于取消对应的请求以及调用接口出现异常时的处理（区别Activity/Fragment）
     * @param fileCallback 下载回调监听
     */
    public static final void downloadApi(String url, HttpParams params, FileCallback fileCallback) {
        OkGo.<File>get(Toolkit.isUrl(url) ? url : x.dataSet().getUrls().getBaseUrl() + url)
                .tag(AppManager.instance)
                .params(params)
                .execute(fileCallback);
    }

    /**
     * 请求上传文件
     *
     * @param url            设置URL，当传入url为完整地址时直接以该地址为请求地址，否则以格式为主URL+url的拼接结果为请求地址
     * @param params         请求的参数
     * @param tag            请求的tag，主要用于取消对应的请求以及调用接口出现异常时的处理（区别Activity/Fragment）
     * @param stringCallback 下载回调监听
     */
    public static final void upLoadApi(String url, HttpParams params, StringCallback stringCallback) {
        OkGo.<String>post(Toolkit.isUrl(url) ? url : x.dataSet().getUrls().getBaseUrl() + url)
                .tag(AppManager.instance)
                .params(params)
                .execute(stringCallback);
    }
}
