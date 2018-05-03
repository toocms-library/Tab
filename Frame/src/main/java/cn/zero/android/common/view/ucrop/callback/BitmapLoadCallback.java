package cn.zero.android.common.view.ucrop.callback;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public interface BitmapLoadCallback {

    void onBitmapLoaded(@NonNull Bitmap bitmap);

    void onFailure(@NonNull Exception bitmapWorkerException);

}