package com.toocms.tab.network;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonSyntaxException;
import com.lzy.okgo.callback.AbsCallback;
import com.toocms.tab.network.modle.SimpleResponse;
import com.toocms.tab.network.modle.TooCMSResponse;
import com.toocms.tab.toolkit.AppManager;
import com.toocms.tab.toolkit.ClassUtils;
import com.toocms.tab.toolkit.GSONUtils;
import com.toocms.tab.toolkit.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * API调用的事件监听器
 *
 * @author Zero
 * @version 4.0
 * @date 2017-03-09
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
            return GSONUtils.fromJson(json, rawType);
        }
    }

    @Override
    public void onSuccess(com.lzy.okgo.model.Response<T> response) {
        onComplete(response);
        if (StringUtils.equals(((TooCMSResponse) response.body()).getFlag(), "success")) {
            onComplete(response.body(), response.getRawCall(), response.getRawResponse());
        } else {
            onError(((TooCMSResponse) response.body()).getMessage(), response.getRawCall(), response.getRawResponse());
            if (((TooCMSResponse) response.body()).getData() != null)
                onError(((TooCMSResponse) response.body()).getData().toString());
        }
        Object tag = response.getRawCall().request().tag();
        if (tag instanceof Application) return;
        try {
            ClassUtils.invoke(tag, "removeProgress", new Class[]{Object.class}, new Object[]{tag});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(com.lzy.okgo.model.Response<T> response) {
        super.onError(response);
        onException(response.getRawCall(), response.getException());
    }

    /**
     * 数据请求成功回调，用于处理特殊返回格式<br/>
     * 与{@link #onComplete(Object, Call, Response)、{@link #onError(String, Call, Response)}保持唯一使用即可
     *
     * @param response }
     */
    public void onComplete(com.lzy.okgo.model.Response response) {
    }

    /**
     * 数据请求成功回调<br/>
     * 与{@link #onComplete(com.lzy.okgo.model.Response)}保持唯一使用即可
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
        try {
            AppCompatActivity activity = AppManager.getInstance().getTopActivity();
            ClassUtils.invoke(activity, "showErrorToast", new Class[]{CharSequence.class}, new Object[]{error});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 附加回调，用于处理flag为error时data中的数据<br/>
     *
     * @param data
     * @deprecated {@link #onComplete(com.lzy.okgo.model.Response)}方法回调进行处理特殊返回格式
     */
    @Deprecated
    public void onError(String data) {
    }

    /**
     * 出现网络问题等未知异常时会回调此方法
     *
     * @param call
     * @param e
     */
    public void onException(Call call, Throwable e) {
        try {
            AppCompatActivity activity = AppManager.getInstance().getTopActivity();
            ClassUtils.invoke(activity, "onException", new Class[]{Request.class, Throwable.class}, new Object[]{call.request(), e});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
