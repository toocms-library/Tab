package com.toocms.tab.toolkit;

import android.os.Environment;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * 缓存文件位置管理类
 *
 * @author Zero @date 2014年8月10日
 * @version 1.0
 */
public class FileManager {

    /**
     * 获取崩溃日志存储路径
     *
     * @return String
     */
    public static String getCrashLogFilePath() {
        return getDirectoryPath(getRootFilePath("crash_log"));
    }

    /**
     * 获取图片文件临时存储路径
     *
     * @return String
     */
    public static String getImageFilePath() {
        return getDirectoryPath(getRootFilePath(Environment.DIRECTORY_PICTURES));
    }

    /**
     * 获取音频文件临时存储路径
     *
     * @return String
     */
    public static String getAudioFilePath() {
        return getDirectoryPath(getRootFilePath(Environment.DIRECTORY_MUSIC));
    }

    /**
     * 获取视频文件临时存储路径
     *
     * @return String
     */
    public static String getVideoFilePath() {
        return getDirectoryPath(getRootFilePath(Environment.DIRECTORY_MOVIES));
    }

    /**
     * 获取下载文件存储路径
     *
     * @return
     */
    public static String getDownloadPath() {
        return getDirectoryPath(getRootFilePath("download"));
    }

    /**
     * 获取缓存文件存储路径
     *
     * @return
     */
    public static String getCachePath() {
        return getDirectoryPath(getRootCachePath());
    }

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取目录的路径
     *
     * @param file
     * @return
     */
    public static String getDirectoryPath(File file) {
        if (file == null) {
            throw new IllegalStateException("无法获取外部存储文件目录");
        } else if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + "已经存在并且不是目录");
            }
        } else {
            if (!file.mkdirs()) {
                throw new IllegalStateException("无法创建目录: " + file.getAbsolutePath());
            }
        }
        return file.getAbsolutePath();
    }

    private static File getRootCachePath() {
        return x.app().getExternalCacheDir();
    }

    private static File getRootFilePath(String dir) {
        return x.app().getExternalFilesDir(dir);
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
        File dirCache = getRootCachePath();
        if (dirCache != null) {
            File[] files = dirCache.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }

        File dirPictures = getRootFilePath(Environment.DIRECTORY_PICTURES);
        if (dirPictures != null) {
            File[] files = dirPictures.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }

        File dirMovies = getRootFilePath(Environment.DIRECTORY_MOVIES);
        if (dirMovies != null) {
            File[] files = dirMovies.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }

        File dirMusic = getRootFilePath(Environment.DIRECTORY_MUSIC);
        if (dirMusic != null) {
            File[] files = dirMusic.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

}
