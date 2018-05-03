package com.toocms.frame.image;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.toocms.frame.image.cache.CacheCallback;
import com.toocms.frame.image.cache.ImageCacheAsyncTask;
import com.toocms.frame.image.progress.OnProgressListener;
import com.toocms.frame.image.progress.ProgressManager;
import com.toocms.frame.image.transform.GlideCircleTransform;
import com.toocms.frame.image.transform.GlideRoundTransform;

import org.xutils.x;

import java.io.File;

import cn.zero.android.common.util.StringUtils;

/**
 * 异步加载图片类
 * <p>
 * <font color='red'>废弃第一个参数为Context的所有方法</font>
 * <p>
 * <font color='red'>废弃原因：当Activity/Fragment销毁时Glide的实例没有被销毁，导致出现崩溃异常</font>
 *
 * @author Zero @date 2017年3月18日
 * @version 4.0
 */
public class ImageLoader {

    /**
     * 通过URL加载图片
     *
     * @param glide
     * @param url
     * @param imageView
     * @param loadingImage
     */
    public static void loadUrl2Image(RequestManager glide, String url, ImageView imageView, @DrawableRes int loadingImage) {
        create().load(glide, url, imageView, requestOptions(loadingImage));
    }

    /**
     * 加载资源图片
     *
     * @param glide
     * @param resId
     * @param imageView
     * @param loadingImage
     */
    public static void loadResId2Image(RequestManager glide, @DrawableRes int resId, ImageView imageView, @DrawableRes int loadingImage) {
        create().load(glide, resId, imageView, requestOptions(loadingImage));
    }

    /**
     * 通过FILE加载图片
     *
     * @param glide
     * @param file
     * @param imageView
     * @param loadingImage
     */
    public static void loadFile2Image(RequestManager glide, File file, ImageView imageView, @DrawableRes int loadingImage) {
        create().load(glide, FILE + file.getAbsolutePath(), imageView, requestOptions(loadingImage));
    }

    /**
     * 通过FILE加载图片
     *
     * @param glide
     * @param file
     * @param imageView
     * @param loadingImage
     */
    public static void loadFile2Image(RequestManager glide, String localPath, ImageView imageView, @DrawableRes int loadingImage) {
        create().load(glide, FILE + localPath, imageView, requestOptions(loadingImage));
    }

    /**
     * 通过URL加载GIF图片
     *
     * @param glide
     * @param url
     * @param imageView
     * @param loadingImage
     */
    public static void loadUrl2Gif(RequestManager glide, String url, ImageView imageView, @DrawableRes int loadingImage) {
        create().load(glide, url, imageView, requestOptions(loadingImage));
    }

    /**
     * 通过URL加载圆形图片
     *
     * @param glide
     * @param url
     * @param imageView
     * @param loadingImage
     */
    public static void loadUrl2CircleImage(RequestManager glide, String url, ImageView imageView, @DrawableRes int loadingImage) {
        create().load(glide, url, imageView, circleRequestOptions(loadingImage));
    }

    /**
     * 加载资源文件为圆形图片
     *
     * @param glide
     * @param resId
     * @param imageView
     * @param loadingImage
     */
    public static void loadResId2CircleImage(RequestManager glide, @DrawableRes int resId, ImageView imageView, @DrawableRes int loadingImage) {
        create().load(glide, resId, imageView, circleRequestOptions(loadingImage));
    }

    /**
     * 通过URL加载圆角图片
     *
     * @param context
     * @param url
     * @param imageView
     * @param loadingImage
     * @param radius
     */
    public static void loadUrl2RoundImage(RequestManager glide, String url, ImageView imageView, @DrawableRes int loadingImage, int radius) {
        create().load(glide, url, imageView, roundRequestOptions(loadingImage, radius));
    }

    /**
     * 加载资源文件为圆角图片
     *
     * @param glide
     * @param resId
     * @param imageView
     * @param loadingImage
     * @param radius
     */
    public static void loadResId2RoundImage(RequestManager glide, @DrawableRes int resId, ImageView imageView, @DrawableRes int loadingImage, int radius) {
        create().load(glide, resId, imageView, roundRequestOptions(loadingImage, radius));
    }

    /**
     * 通过URL获取图片文件
     *
     * @param glide
     * @param url
     * @param callback
     */
    public static void loadUrl2File(RequestManager glide, String url, CacheCallback callback) {
        new ImageCacheAsyncTask(glide, callback).execute(url);
    }

    private static final String ANDROID_RESOURCE = "android.resource://";
    private static final String FILE = "file://";
    private static final String SEPARATOR = "/";
    private static final String HTTP = "http";

    private Object mImageUrlObj;
    private long mTotalBytes = 0;
    private long mLastBytesRead = 0;
    private boolean mLastStatus = false;

    private Handler mainThreadHandler;

    private OnProgressListener internalProgressListener;
    private OnProgressListener onProgressListener;

    public static ImageLoader create() {
        return new ImageLoader();
    }

    private ImageLoader() {
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public String getImageUrl() {
        if (mImageUrlObj == null) return null;
        if (!(mImageUrlObj instanceof String)) return null;
        return (String) mImageUrlObj;
    }

    public Uri resId2Uri(int resourceId) {
        return Uri.parse(ANDROID_RESOURCE + x.app().getPackageName() + SEPARATOR + resourceId);
    }

    public void load(RequestManager glide, int resId, ImageView imageView, RequestOptions options) {
        load(glide, resId2Uri(resId), imageView, options);
    }

    public void load(RequestManager glide, Uri uri, ImageView imageView, RequestOptions options) {
        if (uri == null) return;
        requestBuilder(glide, uri, options).into(imageView);
    }

    public void load(RequestManager glide, String url, ImageView imageView, RequestOptions options) {
        if (StringUtils.isEmpty(url)) return;
        requestBuilder(glide, url, options).into(imageView);
    }

    public static RequestOptions requestOptions(int placeholderResId) {
        return requestOptions(placeholderResId, placeholderResId);
    }

    public static RequestOptions requestOptions(int placeholderResId, int errorResId) {
        return new RequestOptions()
                .placeholder(placeholderResId)
                .error(errorResId);
    }

    public static RequestOptions circleRequestOptions(int placeholderResId) {
        return circleRequestOptions(placeholderResId, placeholderResId);
    }

    public static RequestOptions circleRequestOptions(int placeholderResId, int errorResId) {
        return requestOptions(placeholderResId, errorResId)
                .transform(new GlideCircleTransform());
    }

    public static RequestOptions roundRequestOptions(int placeholderResId, int radius) {
        return roundRequestOptions(placeholderResId, placeholderResId, radius);
    }

    public static RequestOptions roundRequestOptions(int placeholderResId, int errorResId, int radius) {
        return requestOptions(placeholderResId, errorResId)
                .transform(new GlideRoundTransform(radius));
    }

    public RequestBuilder<Drawable> requestBuilder(RequestManager glide, Object obj, RequestOptions options) {
        mImageUrlObj = obj;
        return glide.load(obj)
                .apply(options)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mainThreadCallback(mLastBytesRead, mTotalBytes, true, e);
                        ProgressManager.removeProgressListener(internalProgressListener);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mainThreadCallback(mLastBytesRead, mTotalBytes, true, null);
                        ProgressManager.removeProgressListener(internalProgressListener);
                        return false;
                    }
                });
    }

    private void addProgressListener() {
        if (StringUtils.isEmpty(getImageUrl())) return;
        final String url = getImageUrl();
        if (!url.startsWith(HTTP)) return;

        internalProgressListener = new OnProgressListener() {
            @Override
            public void onProgress(String imageUrl, long bytesRead, long totalBytes, boolean isDone, GlideException exception) {
                if (totalBytes == 0) return;
                if (!url.equals(imageUrl)) return;
                if (mLastBytesRead == bytesRead && mLastStatus == isDone) return;

                mLastBytesRead = bytesRead;
                mTotalBytes = totalBytes;
                mLastStatus = isDone;
                mainThreadCallback(bytesRead, totalBytes, isDone, exception);

                if (isDone) {
                    ProgressManager.removeProgressListener(this);
                }
            }
        };
        ProgressManager.addProgressListener(internalProgressListener);
    }

    private void mainThreadCallback(final long bytesRead, final long totalBytes, final boolean isDone, final GlideException exception) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                int percent = (int) ((bytesRead * 1.0f / totalBytes) * 100.0f);
                if (onProgressListener != null)
                    onProgressListener.onProgress((String) mImageUrlObj, bytesRead, totalBytes, isDone, exception);
            }
        });
    }

    public void setOnProgressListener(String imageUrl, OnProgressListener onProgressListener) {
        mImageUrlObj = imageUrl;
        this.onProgressListener = onProgressListener;
        addProgressListener();
    }
}
