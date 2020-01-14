package com.toocms.tab.control.update;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Progress;
import com.toocms.tab.control.R;
import com.toocms.tab.control.Toasty;
import com.toocms.tab.network.ApiListener;
import com.toocms.tab.network.ApiTool;
import com.toocms.tab.network.modle.TooCMSResponse;
import com.toocms.tab.toolkit.ApkUtils;
import com.toocms.tab.toolkit.AppManager;
import com.toocms.tab.toolkit.FileManager;
import com.toocms.tab.toolkit.FileUtils;
import com.toocms.tab.toolkit.StringUtils;
import com.toocms.tab.toolkit.permission.PermissionGen;
import com.toocms.tab.toolkit.x;

import java.io.File;

import okhttp3.Call;
import okhttp3.Response;

public class UpdateManager {

    private final int REQUEST = 0x1A;

    private static UpdateManager manager = new UpdateManager();
    private ProgressDialog progressDialog;
    private UpdateReceiver receiver;

    private String url;
    private int version;
    private boolean isDownloading;

    /**
     * 检查更新
     *
     * @param hasHint  如果是最新版本是否显示提示
     * @param listener 以后再说按钮的点击监听
     */
    public static final void checkUpdate(boolean hasHint, OnUpdateLaterClickListener... listener) {
        manager.check(hasHint, (listener != null && listener.length != 0) ? listener[0] : null);
    }

    private void check(final boolean hasHint, final OnUpdateLaterClickListener listener) {
        HttpParams params = new HttpParams();
        params.put("package", x.app().getPackageName().replace(".", "_"));
        new ApiTool<TooCMSResponse<Version>>().postApi(x.dataSet().getUrls().getUpdateUrl(),
                params, new ApiListener<TooCMSResponse<Version>>() {

                    @Override
                    public void onComplete(TooCMSResponse<Version> data, Call call, Response response) {
                        final Version version = data.getData();
                        UpdateManager.this.url = version.getUrl();
                        try {
                            UpdateManager.this.version = Integer.parseInt(version.getVersion());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Toasty.error(x.app(), R.string.update_catch, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // 判断版本号
                        if (getVersionCode() >= UpdateManager.this.version) {
                            if (hasHint)
                                Toasty.info(x.app(), R.string.update_newest, Toast.LENGTH_SHORT).show();
                            FileUtils.deleteFile(FileManager.getDownloadPath());    // 删除所有安装包
                        } else {
                            View view = View.inflate(AppManager.getInstance().getTopActivity(), R.layout.dialog_update, null);
                            ((TextView) view.findViewById(R.id.update_description)).setText(version.getDescription());
                            AlertDialog.Builder builder = new AlertDialog.Builder(AppManager.getInstance().getTopActivity());
                            builder.setTitle(R.string.update_has_new);
                            builder.setView(view);
                            File file = getInstallApk();    //  获取安装包
                            builder.setPositiveButton(null == file ? R.string.update_now_update : R.string.update_now_install, (dialog, which) -> {
                                if (null == file) {
                                    receiver = new UpdateReceiver();
                                    IntentFilter intentFilter = new IntentFilter(PermissionGen.ACTION_PERMISSIONS);
                                    AppManager.getInstance().getTopActivity().registerReceiver(receiver, intentFilter);
                                    PermissionGen.needPermission(AppManager.getInstance().getTopActivity(), REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                } else {
                                    ApkUtils.install(x.app(), file);
                                }
                            });
                            builder.setNegativeButton(R.string.update_later, (dialog, which) -> {
                                if (listener != null) listener.onUpdateLaterClick();
                                if (StringUtils.equals(version.getIs_force(), "1")) {
                                    AppManager.getInstance().AppExit(x.app());
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
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

        progressDialog.setOnDismissListener(dialog -> AppManager.getInstance().getTopActivity().unregisterReceiver(receiver));
        if (!progressDialog.isShowing())
            progressDialog.show();
        downloadFile(url);
    }

    // 下载文件
    private synchronized void downloadFile(String url) {
        ApiTool.downloadApi(url, null, new FileCallback(FileManager.getDownloadPath(), getApkName()) {

            @Override
            public void onSuccess(com.lzy.okgo.model.Response<File> response) {
                progressDialog.dismiss();
                isDownloading = false;
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

    private String getApkName() {
        return x.app().getResources().getString(R.string.app_name) + version + ".apk";
    }

    private File getInstallApk() {
        File file = new File(FileManager.getDownloadPath());
        if (!file.exists()) return null;
        File[] files = file.listFiles();
        if (file.listFiles().length <= 0) return null;
        for (int i = 0; i < files.length; i++) {
            if (TextUtils.equals(files[i].getName(), getApkName())) return files[i];
        }
        return null;
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PermissionGen.ACTION_PERMISSIONS) && intent.getIntExtra(PermissionGen.PERMISSIONS_REQUESTCODE, 0) == REQUEST) {
                if (intent.getBooleanExtra(PermissionGen.PERMISSIONS_RESULT, false))
                    startDownload(url);
            } else {
                Toasty.error(x.app(), R.string.update_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}