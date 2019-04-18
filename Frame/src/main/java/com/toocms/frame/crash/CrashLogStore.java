package com.toocms.frame.crash;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lzy.okgo.model.HttpParams;
import com.toocms.frame.ui.R;

import org.xutils.common.util.LogUtil;
import org.xutils.x;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.zero.android.common.util.StringUtils;
import cn.zero.android.common.util.TimeUtils;

/**
 * Author：Zero
 * Date：2017/8/31 16:46
 *
 * @version v4.3
 */

public class CrashLogStore {

    private CrashLogStore() {
    }

    public static void writeLogData(StringBuffer sb, File logFile, HttpParams params) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(logFile));
            String line = br.readLine();
            StringBuffer flag = new StringBuffer();
            int lineNum = 0;
            while (line != null) {
                if (lineNum == 0) {
                    flag.append(line);
                    flag.append("&");
                    params.put("bengkuiyuanyin", line);
                } else if (lineNum == 1) {
                    flag.append(line);
                    LogUtil.e(flag.toString());
                    params.put("flag_md5", CrashLogUtils.toMD5(flag.toString()));
                } else {
                    sb.append(line);
                    sb.append("<br/>");
                }
                line = br.readLine();
                lineNum++;
            }
            params.put("xiangxixinxi", sb.toString());
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public static void writeDataHeader(Application app, HttpParams params) {
//        sb.append("----------------------------- 系统 ---------------------------<br/>");
////        String model = android.os.Build.MODEL;
//        sb.append("手机型号：" + Build.MODEL + "<br/>");
////        int sdkLevel = android.os.Build.VERSION.SDK_INT;
//        sb.append("SDK版本：" + Build.VERSION.SDK_INT + "<br/>");
////        String sdkRelease = android.os.Build.VERSION.RELEASE;
//        sb.append("系统版本：" + Build.VERSION.RELEASE + "<br/>");
////        String packageName = app.getPackageName();
//        sb.append("包名：" + app.getPackageName() + "<br/>");
        params.put("p_flag", app.getString(R.string.app_name)); // 项目名
        params.put("shoujixinghao", Build.BRAND);   // 手机型号
        params.put("xitongbanben", Build.VERSION.RELEASE);  // 系统版本
        params.put("banbenmingcheng", CrashLogUtils.getVersionName(app));  // 版本名称
        params.put("create_time", TimeUtils.getCurrentTimeInString());  // 系统版本
    }

    public static void deleteLogFiles(File[] logFiles) {
        if (logFiles == null) {
            return;
        }
        for (int i = 0; i < logFiles.length; i++) {
            logFiles[i].delete();
        }
    }

    public synchronized static void saveLogToFile(Application app, String request, Throwable throwable, Thread thread) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss",
                Locale.getDefault());
        String fileName = CrashConfig.LOG_FILE_PREFIX
                + dateFormat.format(new Date()) + CrashConfig.LOG_FILE_EXT;

        File dir = new File(CrashLogUtils.getLogStorageDir(app));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        StringBuffer sb;
        BufferedWriter bw = null;
        try {
            sb = new StringBuffer();
            bw = new BufferedWriter(new FileWriter(file));

//            //crash id
//            int versionCode = CrashLogUtils.getVersionCode(app);
//            sb.append("App版本号：" + versionCode + "\n");
//            String versionName = CrashLogUtils.getVersionName(app);
//            sb.append("App版本名称：" + versionName + "\n");
//            String crashId = CrashLogUtils.getCrashId(versionName, throwable.getClass()
//                            .getName(), throwable.getLocalizedMessage(),
//                    throwable.getStackTrace());
//            sb.append("日志编号：" + crashId + "\n");
//
//            // time
//            sb.append("时间：" + TimeUtils.getCurrentTimeInString() + "\n\n");
//
//            sb.append("----------------------------- 内存 ---------------------------\n");
//            // memory
//            Runtime rt = Runtime.getRuntime();
//            long allocMem;
//            if (throwable instanceof OutOfMemoryError) {
//                allocMem = rt.maxMemory();
//            } else {
//                allocMem = rt.totalMemory() - rt.freeMemory();
//            }
//            sb.append("分配内存：" + (allocMem / 1024 / 1024) + "M\n");
//            sb.append("最大内存：" + (rt.maxMemory() / 1024 / 1024) + "M\n\n");
//
//            sb.append("----------------------------- 存储 ---------------------------\n");
//            // storage
//            sb.append("内部存储可用大小：" + CrashLogUtils.getInternalAvailableSize() / 1024 / 1024 + "M\n");
//            sb.append("内部存储总大小：" + CrashLogUtils.getInternalTotalSize() / 1024 / 1024 + "M\n");
//            sb.append("外部存储可用大小：" + CrashLogUtils.getExternalAvailableSize() / 1024 / 1024 + "M\n");
//            sb.append("外部存储总大小：" + CrashLogUtils.getExternalTotalSize() / 1024 / 1024 + "M\n\n");
//
//            sb.append("----------------------------- 线程&进程 ---------------------------\n");
//            // thread&process
//            sb.append("线程名称：" + thread.getName() + "\n");
//            sb.append("进程名称：" + CrashLogUtils.getProcessName(app, android.os.Process.myPid()) + "\n\n");
//
//            sb.append("----------------------------- 崩溃原因 ---------------------------\n");
//            //message
            sb.append(throwable.getClass().getName() + "\n");   // 错误名称
            sb.append(throwable.getLocalizedMessage() + "\n");  // 详细信息
            if (!StringUtils.isEmpty(request)) sb.append("网络请求：" + request + "\n\n");

            sb.append("----------------------------- 错误日志 ---------------------------\n");
            // stack trace
            Writer writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
            Throwable cause = throwable.getCause();
            while (cause != null) {
                cause.printStackTrace(pw);
                cause = cause.getCause();
            }
            pw.close();
            sb.append(writer.toString());
            sb.append("\n\n");

            sb.append("----------------------------- 网络 ---------------------------\n");
            // network
            ConnectivityManager connectMgr = (ConnectivityManager) app.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
            sb.append("是否有网络：" + NetworkState.hasNetwork(netInfo != null) + "\n");
            if (netInfo != null) {
                sb.append("网络类型：" + NetworkState.getNetworkTypeName(app, netInfo) + "\n");
                sb.append("网络状态：" + NetworkState.getNetworkState(netInfo.getDetailedState()) + "\n");
//                Log.d(CrashConfig.TAG, netInfo.toString());
            }
            bw.write(sb.toString());
            bw.newLine();
            bw.flush();
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }
}