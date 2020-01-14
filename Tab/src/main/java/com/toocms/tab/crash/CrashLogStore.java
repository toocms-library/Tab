package com.toocms.tab.crash;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.lzy.okgo.model.HttpParams;
import com.toocms.tab.R;
import com.toocms.tab.toolkit.DigestUtils;
import com.toocms.tab.toolkit.LogUtil;
import com.toocms.tab.toolkit.StringUtils;
import com.toocms.tab.toolkit.TimeUtils;

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
                    params.put("flag_md5", DigestUtils.md5(flag.toString()));
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

    public synchronized static void saveLogToFile(Application app, String request, Throwable throwable) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
        String fileName = CrashConfig.LOG_FILE_PREFIX + dateFormat.format(new Date()) + CrashConfig.LOG_FILE_EXT;

        File dir = new File(CrashLogUtils.getLogStorageDir());
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

            //message
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