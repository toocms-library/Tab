package cn.zero.android.common.util;

import android.os.Environment;

import com.bumptech.glide.Glide;

import org.xutils.x;

import java.io.File;

/**
 * 缓存文件位置管理类
 *
 * @author Zero @date 2014年8月10日
 * @version 1.0
 */
public class FileManager {

    /**
     * 获取缓存图片文件路径
     *
     * @return String
     */
    public static String getSaveFilePath() {
        return getRootFilePath() + x.dataSet().getAppConfig().getProgectFolder() + File.separator + "picture_cache" + File.separator;
    }

    /**
     * 获取压缩文件临时存储路径
     *
     * @return String
     */
    public static String getCompressFilePath() {
        return getRootFilePath() + x.dataSet().getAppConfig().getProgectFolder() + File.separator + "compress_cache" + File.separator;
    }

    /**
     * 获取崩溃日志存储路径
     *
     * @return String
     */
    public static String getCrashLogFilePath() {
        return getRootFilePath() + x.dataSet().getAppConfig().getProgectFolder() + File.separator + "crash_log" + File.separator;
    }

    /**
     * 获取录音文件临时存储路径
     *
     * @return String
     */
    public static String getVoiceFilePath() {
        return getRootFilePath() + x.dataSet().getAppConfig().getProgectFolder() + File.separator + "voice" + File.separator;
    }

    /**
     * 获取下载文件存储路径
     *
     * @return
     */
    public static String getDownloadPath() {
        return getRootFilePath() + x.dataSet().getAppConfig().getProgectFolder() + File.separator + "download" + File.separator;
    }

    /**
     * 获取缓存文件存储路径
     *
     * @return
     */
    public static String getCachePath() {
        return getRootFilePath() + x.dataSet().getAppConfig().getProgectFolder() + File.separator + "cache" + File.separator;
    }

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getRootFilePath() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";// filePath:/sdcard/
        } else {
            return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath:
            // /data/data/
        }
    }

    /**
     * 清除内存中的缓存
     */
    public static void clearMemCache() {
        Glide.get(x.app()).clearMemory();
    }

    /**
     * 清除手机中的缓存
     */
    public static void clearCacheFiles() {
        Glide.get(x.app()).clearDiskCache();
    }

}
