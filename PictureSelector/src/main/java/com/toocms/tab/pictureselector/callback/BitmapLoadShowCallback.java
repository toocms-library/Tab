package com.toocms.tab.pictureselector.callback;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public interface BitmapLoadShowCallback {

    void onBitmapLoaded(@NonNull Bitmap bitmap);

    void onFailure(@NonNull Exception bitmapWorkerException);

}