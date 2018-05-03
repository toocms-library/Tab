package com.toocms.frame.image.cache;

import android.content.Context;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * 下载图片
 * <p>
 * Author：Zero
 * Date：2017/5/15 17:03
 *
 * @version v4.0
 */

public final class ImageCacheAsyncTask extends AsyncTask<String, Void, File> {

    private RequestManager glide;
    private CacheCallback callback;

    public ImageCacheAsyncTask(Context context, CacheCallback callback) {
        glide = Glide.with(context);
        this.callback = callback;
    }

    public ImageCacheAsyncTask(RequestManager glide, CacheCallback callback) {
        this.glide = glide;
        this.callback = callback;
    }

    @Override
    protected File doInBackground(String... params) {
        String url = params[0];
        try {
            return
                    glide.download(url)
                            .submit()
                            .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        if (file == null) return;
        if (null != callback) callback.onCache(file);
    }
}
