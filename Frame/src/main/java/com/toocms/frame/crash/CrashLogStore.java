package com.toocms.frame.crash;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

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

    public static void writeLogData(StringBuffer sb, File logFile) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(logFile));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("<br/>");
                line = br.readLine();
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public static void writeDataHeader(Application app, StringBuffer sb) throws IOException {
        sb.append("----------------------------- 系统 ---------------------------<br/>");
        String model = android.os.Build.MODEL;
        sb.append("手机型号：" + Build.MODEL + "<br/>");
        int sdkLevel = android.os.Build.VERSION.SDK_INT;
        sb.append("SDK版本：" + Build.VERSION.SDK_INT + "<br/>");
        String sdkRelease = android.os.Build.VERSION.RELEASE;
        sb.append("系统版本：" + Build.VERSION.RELEASE + "<br/>");
        String packageName = app.getPackageName();
        sb.append("包名：" + app.getPackageName() + "<br/>");
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

            //crash id
            int versionCode = CrashLogUtils.getVersionCode(app);
            sb.append("App版本号：" + versionCode + "\n");
            String versionName = CrashLogUtils.getVersionName(app);
            sb.append("App版本名称：" + versionName + "\n");
            String crashId = CrashLogUtils.getCrashId(versionName, throwable.getClass()
                            .getName(), throwable.getLocalizedMessage(),
                    throwable.getStackTrace());
            sb.append("日志编号：" + crashId + "\n");

            // time
            sb.append("时间：" + TimeUtils.getCurrentTimeInString() + "\n\n");

            sb.append("----------------------------- 内存 ---------------------------\n");
            // memory
            Runtime rt = Runtime.getRuntime();
            long allocMem;
            if (throwable instanceof OutOfMemoryError) {
                allocMem = rt.maxMemory();
            } else {
                allocMem = rt.totalMemory() - rt.freeMemory();
            }
            sb.append("分配内存：" + (allocMem / 1024 / 1024) + "M\n");
            sb.append("最大内存：" + (rt.maxMemory() / 1024 / 1024) + "M\n\n");

            sb.append("----------------------------- 存储 ---------------------------\n");
            // storage
            sb.append("内部存储可用大小：" + CrashLogUtils.getInternalAvailableSize() / 1024 / 1024 + "M\n");
            sb.append("内部存储总大小：" + CrashLogUtils.getInternalTotalSize() / 1024 / 1024 + "M\n");
            sb.append("外部存储可用大小：" + CrashLogUtils.getExternalAvailableSize() / 1024 / 1024 + "M\n");
            sb.append("外部存储总大小：" + CrashLogUtils.getExternalTotalSize() / 1024 / 1024 + "M\n\n");

            sb.append("----------------------------- 线程&进程 ---------------------------\n");
            // thread&process
            sb.append("线程名称：" + thread.getName() + "\n");
            sb.append("进程名称：" + CrashLogUtils.getProcessName(app, android.os.Process.myPid()) + "\n\n");

            sb.append("----------------------------- 崩溃原因 ---------------------------\n");
            //message
            sb.append("错误名称：" + throwable.getClass().getName() + "\n");
            sb.append("详细信息：" + throwable.getLocalizedMessage() + "\n");
            if (StringUtils.isEmpty(request)) sb.append("\n");
            else sb.append("网络请求：" + request + "\n\n");

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
            sb.append("是否有网络：" + (netInfo != null) + "\n");
            if (netInfo != null) {
                sb.append("网络类型：" + netInfo.getTypeName() + "\n");
                sb.append("网络状态：" + netInfo.getDetailedState() + "\n");
                Log.d(CrashConfig.TAG, netInfo.toString());
                if (CrashLogUtils.isNetworkTypeMobile(netInfo.getType())) {
                    TelephonyManager telMgr = (TelephonyManager) app.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    sb.append("移动网络类型：" + telMgr.getNetworkType() + "\n");
                }
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