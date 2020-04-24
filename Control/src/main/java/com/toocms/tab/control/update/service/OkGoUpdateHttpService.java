package com.toocms.tab.control.update.service;

import androidx.annotation.NonNull;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.toocms.tab.control.update.proxy.IUpdateHttpService;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author：Zero
 * Date：2020/4/22 11:31
 *
 * @version v5.0
 */
public class OkGoUpdateHttpService implements IUpdateHttpService {

    private Map<String, String> transform(Map<String, Object> params) {
        Map<String, String> map = new TreeMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toString());
        }
        return map;
    }

    @Override
    public void asyncGet(@NonNull String url, @NonNull Map<String, Object> params, @NonNull Callback callBack) {
        OkGo.<String>get(url)
                .tag(url)
                .params(transform(params))
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        callBack.onSuccess(response.body());
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        callBack.onError(response.getException());
                    }
                });
    }

    @Override
    public void asyncPost(@NonNull String url, @NonNull Map<String, Object> params, @NonNull Callback callBack) {
        OkGo.<String>post(url)
                .tag(url)
                .params(transform(params))
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        callBack.onSuccess(response.body());
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        callBack.onError(response.getException());
                    }
                });
    }

    @Override
    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull DownloadCallback callback) {
        OkGo.<File>get(url)
                .tag(url)
                .execute(new FileCallback(path, fileName) {

                    @Override
                    public void onStart(Request<File, ? extends Request> request) {
                        callback.onStart();
                    }

                    @Override
                    public void onSuccess(Response<File> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        callback.onProgress(progress.fraction, progress.totalSize);
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<File> response) {
                        super.onError(response);
                        callback.onError(response.getException());
                    }
                });
    }

    @Override
    public void cancelDownload(@NonNull String url) {
        OkGo.getInstance().cancelTag(url);
    }
}
