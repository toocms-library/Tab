package com.toocms.tab.imageloader.progress;

/**
 * Author：Zero
 * Date：2018/4/27 9:46
 *
 * @version v1.0
 */
public interface OnProgressListener {

    void onProgress(boolean isComplete, int percentage, long bytesRead, long totalBytes);
}
