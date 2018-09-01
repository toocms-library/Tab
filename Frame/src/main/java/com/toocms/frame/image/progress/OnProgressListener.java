package com.toocms.frame.image.progress;

import com.bumptech.glide.load.engine.GlideException;

/**
 * Author：Zero
 * Date：2018/4/27 9:46
 *
 * @version v1.0
 */
public interface OnProgressListener {

    void onProgress(boolean isComplete, int percentage, long bytesRead, long totalBytes);
}
