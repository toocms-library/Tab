package com.toocms.frame.update;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Progress;
import com.toocms.frame.tool.AppManager;
import com.toocms.frame.ui.R;
import com.toocms.frame.view.PromptInfo;
import com.toocms.frame.web.ApiListener;
import com.toocms.frame.web.ApiTool;
import com.toocms.frame.web.modle.TooCMSResponse;

import org.xutils.x;

import java.io.File;

import cn.zero.android.common.permission.PermissionGen;
import cn.zero.android.common.util.ApkUtils;
import cn.zero.android.common.util.FileManager;
import okhttp3.Call;
import okhttp3.Response;

public class UpdateManager {

    private final int REQUEST = 0x1A;

    private static UpdateManager manager = new UpdateManager();
    private ProgressDialog progressDialog;
    private UpdateReceiver receiver;

    private String url;
    private String path = FileManager.getDownloadPath() + x.app().getResources().getString(R.string.app_name) + ".apk";
    private boolean isDownloading;

    /**
     * 检查更新
     *
     * @param hasHint 如果是最新版本是否显示提示
     */
    public static final void checkUpdate(boolean hasHint) {
        manager.check(hasHint);
    }

    private void check(final boolean hasHint) {
        HttpParams params = new HttpParams();
        params.put("package", x.app().getPackageName().replace(".", "_"));
        new ApiTool<TooCMSResponse<Version>>().postApi(x.dataSet().getUrls().getUpdateUrl(),
                params, new ApiListener<TooCMSResponse<Version>>() {
                    @Override
                    public void onComplete(TooCMSResponse<Version> data, Call call, Response response) {
                        Version version = data.getData();
                        UpdateManager.this.url = version.getUrl();
                        // 判断版本号
                        if (getVersionCode() >= Integer.parseInt(version.getVersion())) {
                            if (hasHint)
                                PromptInfo.getInstance().showToast(AppManager.getInstance().getTopActivity(), R.string.update_newest);
                        } else {
                            View view = View.inflate(AppManager.getInstance().getTopActivity(), R.layout.dialog_update, null);
                            ((TextView) view.findViewById(R.id.update_description)).setText(version.getDescription());
                            new AlertDialog.Builder(AppManager.getInstance().getTopActivity()).setTitle(R.string.update_has_new).setView(view).setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    receiver = new UpdateReceiver();
                                    IntentFilter intentFilter = new IntentFilter(PermissionGen.ACTION_PERMISSIONS);
                                    AppManager.getInstance().getTopActivity().registerReceiver(receiver, intentFilter);
                                    PermissionGen.needPermission(AppManager.getInstance().getTopActivity(), REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                }
                            }).setNegativeButton(R.string.update_later, null).create().show();
                        }
                    }
                });
    }

    private void startDownload(String url) {
        // 防止重复弹框
        if (isDownloading) return;
        isDownloading = true;
        progressDialog = new ProgressDialog(AppManager.getInstance().getTopActivity());
        // 设置不可取消
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setMessage(x.app().getResources().getString(R.string.update_downloading));

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AppManager.getInstance().getTopActivity().unregisterReceiver(receiver);
            }
        });
        if (!progressDialog.isShowing())
            progressDialog.show();
        downloadFile(url);
    }

    // 下载文件
    private synchronized void downloadFile(String url) {
        ApiTool.downloadApi(url, null, new FileCallback(FileManager.getDownloadPath(), x.app().getResources().getString(R.string.app_name) + ".apk") {

            @Override
            public void onSuccess(com.lzy.okgo.model.Response<File> response) {
                progressDialog.dismiss();
                ApkUtils.install(x.app(), response.body());
            }

            @Override
            public void downloadProgress(Progress progress) {
                progressDialog.setProgress((int) (progress.fraction * 100));
            }
        });
    }

    // 获取版本号
    private int getVersionCode() {
        try {
            PackageInfo packageInfo = x.app().getPackageManager().getPackageInfo(x.app().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PermissionGen.ACTION_PERMISSIONS) && intent.getIntExtra(PermissionGen.PERMISSIONS_REQUESTCODE, 0) == REQUEST) {
                if (intent.getBooleanExtra(PermissionGen.PERMISSIONS_RESULT, false))
                    startDownload(url);
            } else {
                PromptInfo.getInstance().showToast(AppManager.getInstance().getTopActivity(), R.string.update_fail);
            }
        }
    }
}