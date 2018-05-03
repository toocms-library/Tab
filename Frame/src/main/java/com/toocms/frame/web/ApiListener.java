package com.toocms.frame.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lzy.okgo.callback.AbsCallback;
import com.toocms.frame.ui.BaseActivity;
import com.toocms.frame.ui.BaseFragment;
import com.toocms.frame.view.PromptInfo;
import com.toocms.frame.web.modle.SimpleResponse;
import com.toocms.frame.web.modle.TooCMSResponse;

import org.xutils.common.util.LogUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import cn.zero.android.common.util.GSONUtils;
import cn.zero.android.common.util.StringUtils;
import okhttp3.Call;
import okhttp3.Response;

/**
 * API调用的事件监听器
 *
 * @author Zero @date 2017-03-09
 * @version 4.0
 */
public abstract class ApiListener<T> extends AbsCallback<T> {

    @Override
    public T convertResponse(Response response) throws Throwable {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Type type = params[0];
        if (!(type instanceof ParameterizedType)) throw new IllegalStateException("请填写泛型参数");
        Type rawType = ((ParameterizedType) type).getRawType();
        Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
        String json = response.body().string();
        if (typeArgument == Void.class) {
            SimpleResponse simpleResponse = GSONUtils.fromJson(json, SimpleResponse.class);
            response.close();
            return (T) simpleResponse.toTooCMSResponse();
        } else if (rawType == TooCMSResponse.class) {
            TooCMSResponse tooCMSResponse;
            try {
                tooCMSResponse = GSONUtils.fromJson(json, type);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                SimpleResponse simpleResponse = GSONUtils.fromJson(json, SimpleResponse.class);
                tooCMSResponse = simpleResponse.toTooCMSResponse();
            }
            response.close();
            return (T) tooCMSResponse;
        } else {
            response.close();
            throw new IllegalStateException("无法解析的类型");
        }
    }

    @Override
    public void onSuccess(com.lzy.okgo.model.Response<T> response) {
        if (StringUtils.equals(((TooCMSResponse) response.body()).getFlag(), "success")) {
            onComplete(response.body(), response.getRawCall(), response.getRawResponse());
        } else {
            onError(((TooCMSResponse) response.body()).getMessage(), response.getRawCall(), response.getRawResponse());
            onError(((TooCMSResponse) response.body()).getData().toString());
        }
        Object tag = response.getRawCall().request().tag();
        if (tag instanceof BaseActivity) {
            ((BaseActivity) tag).removeProgress();
        } else if (tag instanceof BaseFragment) {
            ((BaseFragment) tag).removeProgress();
        } else {
            LogUtil.e("Application");
        }
    }

    @Override
    public void onError(com.lzy.okgo.model.Response<T> response) {
        super.onError(response);
        onException(response.getRawCall(), response.getRawResponse(), response.getException());
    }

    /**
     * 数据请求成功回调
     *
     * @param data     返回的data字段下的数据
     * @param call
     * @param response
     */
    public abstract void onComplete(T data, Call call, Response response);

    /**
     * 出现业务错误时通知监听器错误码及字错误码等信息
     *
     * @param error
     * @param call
     * @param response
     */
    public void onError(String error, Call call, Response response) {
        PromptInfo.getInstance().showToast(call.request().tag(), error);
    }

    /**
     * 附加回调，用于处理flag为error时data中的数据
     *
     * @param data
     */
    public void onError(String data) {
    }

    /**
     * 出现网络问题等未知异常时会回调此方法
     *
     * @param call
     * @param response
     * @param e
     */
    public void onException(Call call, Response response, Throwable e) {
        Object tag = call.request().tag();
        if (tag instanceof BaseActivity) {
            ((BaseActivity) tag).onException(e);
        } else if (tag instanceof BaseFragment) {
            ((BaseFragment) tag).onException(e);
        } else {
            LogUtil.e("Application");
        }
    }
}
