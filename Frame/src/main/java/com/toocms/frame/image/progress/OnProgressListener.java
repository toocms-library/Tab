package com.toocms.frame.image.progress;

import com.bumptech.glide.load.engine.GlideException;

/**
 * Author：Zero
 * Date：2018/4/27 9:46
 *
 * @version v1.0
 */
public interface OnProgressListener {

    void onProgress(String imageUrl, long bytesRead, long totalBytes, boolean isDone, GlideException exception);
}
