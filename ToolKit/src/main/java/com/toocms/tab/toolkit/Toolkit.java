package com.toocms.tab.toolkit;

import android.content.Context;
import android.text.ClipboardManager;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 *
 * @author Zero @date 2014年8月10日
 * @version 1.0
 */
public class Toolkit {

    /**
     * 得到手机IP地址
     *
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 实现文本复制功能
     *
     * @param content
     */
    public static void copy(Context context, String content) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 实现粘贴功能
     *
     * @param context
     * @return
     */
    public static String paste(Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }

    /**
     * 判断字符串内容是否为数字
     *
     * @param str
     * @return boolean
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断邮箱格式是否正确
     */
    public static boolean isEmail(String strEmail) {
        String strPattern = "^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\\.([a-zA-Z0-9_-])+)+$";

        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    /**
     * 判断手机号码
     */
    public static boolean isMobile(String mobiles) {
        Pattern pattern = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,1-9]))\\d{8}$");
        Matcher matcher = pattern.matcher(mobiles);
        return matcher.matches();
    }

    /**
     * 判断网址是否有效
     */
    public static boolean isUrl(String url) {
        Pattern pattern = Pattern.compile("^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+)\\.)+([A-Za-z]+)[/\\?\\:]?.*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    /**
     * 获取屏幕的宽
     *
     * @param context
     * @return <p/>
     * int
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕的高
     *
     * @param context
     * @return <p/>
     * int
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取手机状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 获取config.properties文件中的值 assets
     *
     * @param key
     * @return String
     */
    public static String getConfigProperties(String key) {
        String value = "";
        Properties props = new Properties();
        InputStream iStream = Toolkit.class.getResourceAsStream("/assets/config.properties");

        try {
            props.load(iStream);
            value = props.getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * <p/>
     * 获取图片资源的id
     *
     * @param context
     * @param resName 图片资源的名称
     * @return int 图片资源id
     */
    public static int getBitmapRes(Context context, String resName) {
        try {
            String str = context.getPackageName();
            Class localClass = Class.forName(str + ".R$drawable");
            return getResId(localClass, resName);
        } catch (Throwable localThrowable) {
            // CommonlyLog.c(localThrowable);
        }
        return 0;
    }

    /**
     * <p/>
     * 获取控件资源的id
     *
     * @param context
     * @param resName 图片资源的名称
     * @return int 图片资源id
     */
    public static int getViewRes(Context context, String resName) {
        try {
            String str = context.getPackageName();
            Class localClass = Class.forName(str + ".R$id");
            return getResId(localClass, resName);
        } catch (Throwable localThrowable) {
            // CommonlyLog.c(localThrowable);
        }
        return 0;
    }

    /**
     * <p/>
     * 获取文字资源的id
     *
     * @param context
     * @param resName 文字资源的名称
     * @return int 文字资源的id
     */
    public static int getStringRes(Context context, String resName) {
        try {
            String str = context.getPackageName();
            Class localClass = Class.forName(str + ".R$string");
            return getResId(localClass, resName);
        } catch (Throwable localThrowable) {
            // CommonlyLog.c(localThrowable);
        }
        return 0;
    }

    private static int getResId(Class<?> paramClass, String paramString) {
        int i = 0;
        if (paramString != null) {
            String str = paramString.toLowerCase();
            try {
                Field localField = paramClass.getField(str);
                localField.setAccessible(true);
                i = ((Integer) localField.get(null)).intValue();
            } catch (Throwable localThrowable) {
                // CommonlyLog.c(localThrowable);
                i = 0;
            }
        }
        // if (i <= 0)
        // System.err.println("resource " + paramClass.getName() + "." +
        // paramString + " not found!");
        return i;
    }
}
