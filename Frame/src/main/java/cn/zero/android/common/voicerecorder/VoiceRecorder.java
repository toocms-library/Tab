package cn.zero.android.common.voicerecorder;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Time;

import org.xutils.common.util.LogUtil;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import cn.zero.android.common.util.FileManager;

public class VoiceRecorder {

    public static final int FILE_INVALID = 401;
    static final String PREFIX = "voice";
    static final String EXTENSION = ".amr";

    MediaRecorder recorder;

    private boolean isRecording = false;
    private long startTime;
    private String voiceFilePath = null;
    private String voiceFileName = null;
    private File file;
    private Handler handler;

    public VoiceRecorder(Handler handler) {
        this.handler = handler;
    }

    /**
     * 开始录音
     *
     * @return
     */
    public String startRecording() {
        file = null;
        try {
            // need to create recorder every time, otherwise, will got exception
            // from setOutputFile when try to reuse
            if (recorder != null) {
                recorder.release();
                recorder = null;
            }
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioChannels(1); // MONO
            recorder.setAudioSamplingRate(8000); // 8000Hz
            recorder.setAudioEncodingBitRate(64); // seems if change this to
            // 128, still got same file
            // size.
            // one easy way is to use temp file
            // file = File.createTempFile(PREFIX + userId, EXTENSION,
            // User.getVoicePath());
            voiceFileName = buildVoiceFileName();
            voiceFilePath = FileManager.getRootFilePath() + x.dataSet().getAppConfig().getProgectFolder() + File.separator + PREFIX + File.separator + voiceFileName;
            file = new File(voiceFilePath);
            recorder.setOutputFile(file.getAbsolutePath());
            recorder.prepare();
            isRecording = true;
            recorder.start();
        } catch (IOException e) {
            LogUtil.e("prepare() failed");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRecording) {
                        android.os.Message msg = new android.os.Message();
                        msg.what = recorder.getMaxAmplitude() * 9 / 0x7FFF;
                        handler.sendMessage(msg);
                        SystemClock.sleep(100);
                    }
                } catch (Exception e) {
                    // from the crash report website, found one NPE crash from
                    // one android 4.0.4 htc phone
                    // maybe handler is null for some reason
                    LogUtil.e(e.toString());
                }
            }
        }).start();
        startTime = new Date().getTime();
        LogUtil.d("start voice recording to file:" + file.getAbsolutePath());
        return file == null ? null : file.getAbsolutePath();
    }

    /**
     * 停止并删除录音
     *
     * @return seconds of the voice recorded
     */
    public void discardRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                if (file != null && file.exists() && !file.isDirectory()) {
                    file.delete();
                }
            } catch (IllegalStateException e) {
            } catch (RuntimeException e) {
            }
            isRecording = false;
        }
    }

    /**
     * 停止录音
     *
     * @return 录音的时间（秒）, 401 - 表示录音失败
     */
    public int stopRecoding() {
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;

            if (file == null || !file.exists() || !file.isFile()) {
                return FILE_INVALID;
            }
            if (file.length() == 0) {
                file.delete();
                return FILE_INVALID;
            }
            int seconds = (int) (new Date().getTime() - startTime) / 1000;
            LogUtil.d("voice recording finished. seconds:" + seconds + " file length:" + file.length());
            return seconds;
        }
        return 0;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (recorder != null) {
            recorder.release();
        }
    }

    /**
     * 根据时间戳生成录音的文件名
     *
     * @return
     */
    public String buildVoiceFileName() {
        Time localTime = new Time();
        localTime.setToNow();
        return localTime.toString().substring(0, 15) + EXTENSION;
    }

    /**
     * 是否正在录音
     *
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    /**
     * 获取录音文件的路径
     *
     * @return
     */
    public String getVoiceFilePath() {
        return voiceFilePath;
    }

    /**
     * 获取文件名
     *
     * @return
     */
    public String getVoiceFileName() {
        return voiceFileName;
    }
}
