package com.toocms.tab.toolkit.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;

import androidx.fragment.app.Fragment;

import com.toocms.tab.toolkit.AppManager;
import com.toocms.tab.toolkit.permission.internal.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by namee on 2015. 11. 17..
 */
public class PermissionGen {

    /**
     * 权限广播标识
     */
    public static final String ACTION_PERMISSIONS = "com.toocms.permissions";

    public static final String PERMISSIONS_RESULT = "PERMISSIONS_RESULT";
    public static final String PERMISSIONS_REQUESTCODE = "PERMISSIONS_REQUESTCODE";

    private String[] mPermissions;
    private int mRequestCode;
    private Object object;

    private PermissionGen(Object object) {
        this.object = object;
    }

    public static PermissionGen with(Activity activity) {
        return new PermissionGen(activity);
    }

    public static PermissionGen with(Fragment fragment) {
        return new PermissionGen(fragment);
    }

    public PermissionGen permissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    public PermissionGen addRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    public void request() {
        requestPermissions(object, mRequestCode, mPermissions);
    }

    public static void needPermission(Activity activity, int requestCode, String[] permissions) {
        requestPermissions(activity, requestCode, permissions);
    }

    public static void needPermission(Fragment fragment, int requestCode, String[] permissions) {
        requestPermissions(fragment, requestCode, permissions);
    }

    public static void needPermission(Activity activity, int requestCode, String permission) {
        needPermission(activity, requestCode, new String[]{permission});
    }

    public static void needPermission(Fragment fragment, int requestCode, String permission) {
        needPermission(fragment, requestCode, new String[]{permission});
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private static void requestPermissions(Object object, int requestCode, String[] permissions) {
        if (!Utils.isOverMarshmallow()) {
            doExecuteSuccess(object, requestCode);
            return;
        }
        List<String> deniedPermissions = Utils.findDeniedPermissions(Utils.getActivity(object), permissions);

        if (deniedPermissions.size() > 0) {
            if (object instanceof Activity) {
                ((Activity) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else if (object instanceof Fragment) {
                ((Fragment) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported");
            }

        } else {
            doExecuteSuccess(object, requestCode);
        }
    }


    private static void doExecuteSuccess(Object activity, int requestCode) {
        Method executeMethod = Utils.findMethodWithRequestCode(activity.getClass(),
                PermissionSuccess.class, requestCode);

        executeMethod(activity, executeMethod);
        sendBroadcast(requestCode, true);
    }

    private static void doExecuteFail(Object activity, int requestCode) {
        Method executeMethod = Utils.findMethodWithRequestCode(activity.getClass(),
                PermissionFail.class, requestCode);

        executeMethod(activity, executeMethod);
        sendBroadcast(requestCode, false);
    }

    private static void executeMethod(Object activity, Method executeMethod) {
        if (executeMethod != null) {
            try {
                if (!executeMethod.isAccessible()) executeMethod.setAccessible(true);
                executeMethod.invoke(activity, new Object[]{});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions,
                                                  int[] grantResults) {
        requestResult(activity, requestCode, permissions, grantResults);
    }

    public static void onRequestPermissionsResult(Fragment fragment, int requestCode, String[] permissions,
                                                  int[] grantResults) {
        requestResult(fragment, requestCode, permissions, grantResults);
    }

    private static void requestResult(Object obj, int requestCode, String[] permissions,
                                      int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                // 请求权限失败之后再次通过ops验证是否真的获取失败，针对MIUI系统
                Activity activity;
                String packageName;
                if (obj instanceof Activity) {
                    activity = (Activity) obj;
                    packageName = ((Activity) obj).getPackageName();
                } else if (obj instanceof Fragment) {
                    activity = ((Fragment) obj).getActivity();
                    packageName = ((Fragment) obj).getActivity().getPackageName();
                } else {
                    throw new IllegalArgumentException(obj.getClass().getName() + " is not supported");
                }
                AppOpsManager appOpsManager = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
                int checkOps = appOpsManager.checkOp(permissions[i], Binder.getCallingUid(), packageName);
                if (checkOps == AppOpsManager.MODE_IGNORED) {    // 依然是被拒绝授权
                    deniedPermissions.add(permissions[i]);
                }
            }
        }

        if (deniedPermissions.size() > 0) {
            doExecuteFail(obj, requestCode);
        } else {
            doExecuteSuccess(obj, requestCode);
        }
    }

    private static void sendBroadcast(int requestCode, boolean result) {
        Intent intent = new Intent(ACTION_PERMISSIONS);
        intent.putExtra(PERMISSIONS_REQUESTCODE, requestCode);
        intent.putExtra(PERMISSIONS_RESULT, result);
        AppManager.getInstance().getTopActivity().sendBroadcast(intent);
    }
}
