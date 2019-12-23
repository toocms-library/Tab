package cn.zero.android.common.voicerecorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.toocms.frame.tool.AppManager;
import com.toocms.frame.ui.BaseActivity;
import com.toocms.frame.ui.R;

import cn.zero.android.common.util.FileManager;

/**
 * 按住说话录制控件
 */
public class VoiceRecorderView extends RelativeLayout {

    protected Context context;
    protected Drawable[] micImages;
    protected VoiceRecorder voiceRecorder;

    protected PowerManager.WakeLock wakeLock;
    protected ImageView micImage;

    protected Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 切换msg切换图片
            micImage.setImageDrawable(micImages[msg.what]);
        }
    };

    public VoiceRecorderView(Context context) {
        super(context);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("InvalidWakeLockTag")
    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.widget_voice_recorder, this);

        micImage = (ImageView) findViewById(R.id.mic_image);

        voiceRecorder = new VoiceRecorder(micImageHandler);

        // 动画资源文件,用于录制语音时
        micImages = new Drawable[]{getResources().getDrawable(R.drawable.record_animate_01),
                getResources().getDrawable(R.drawable.record_animate_02),
                getResources().getDrawable(R.drawable.record_animate_03),
                getResources().getDrawable(R.drawable.record_animate_04),
                getResources().getDrawable(R.drawable.record_animate_05),
                getResources().getDrawable(R.drawable.record_animate_06),
                getResources().getDrawable(R.drawable.record_animate_07),
                getResources().getDrawable(R.drawable.record_animate_08),
                getResources().getDrawable(R.drawable.record_animate_09),
                getResources().getDrawable(R.drawable.record_animate_10)};

        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");
    }

    /**
     * 长按说话按钮touch事件
     *
     * @param v
     * @param event
     */
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event, EaseVoiceRecorderCallback recorderCallback) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                try {
                    if (ChatRowVoicePlayClickListener.isPlaying)
                        ChatRowVoicePlayClickListener.currentPlayListener.stopPlayVoice();
                    v.setPressed(true);
                    startRecording();
                } catch (Exception e) {
                    v.setPressed(false);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.getY() < 0) {
                    showReleaseToCancelHint();
                } else {
                    showMoveUpToCancelHint();
                }
                return true;
            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                if (event.getY() < 0) {
                    // discard the recorded audio.
                    voiceRecorder.setRecording(true);
                    discardRecording();
                } else {
                    // stop recording and send voice file
                    try {
                        int length = stopRecoding();
                        if (length > 0) {
                            if (recorderCallback != null) {
                                recorderCallback.onVoiceRecordComplete(getVoiceFilePath(), length);
                            }
                        } else if (length == VoiceRecorder.FILE_INVALID) {
                            ((BaseActivity) AppManager.getInstance().getTopActivity()).showToast(R.string.Recording_without_permission);
                        } else {
                            ((BaseActivity) AppManager.getInstance().getTopActivity()).showToast(R.string.The_recording_time_is_too_short);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((BaseActivity) AppManager.getInstance().getTopActivity()).showToast(R.string.send_failure_please);
                    }
                }
                return true;
            default:
                discardRecording();
                return false;
        }
    }

    public interface EaseVoiceRecorderCallback {
        /**
         * 录音完毕
         *
         * @param voiceFilePath   录音完毕后的文件路径
         * @param voiceTimeLength 录音时长
         */
        void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength);
    }

    public void startRecording() {
        if (!FileManager.hasSDCard()) {
            ((BaseActivity) AppManager.getInstance().getTopActivity()).showToast(R.string.Send_voice_need_sdcard_support);
            return;
        }
        try {
            wakeLock.acquire();
            this.setVisibility(View.VISIBLE);
            micImage.setImageResource(R.drawable.cancel_record);
            voiceRecorder.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            if (wakeLock.isHeld())
                wakeLock.release();
            if (voiceRecorder != null)
                voiceRecorder.discardRecording();
            this.setVisibility(View.INVISIBLE);
            ((BaseActivity) AppManager.getInstance().getTopActivity()).showToast(R.string.recoding_fail);
            return;
        }
    }

    public void showReleaseToCancelHint() {
//        recordingHint.setText(context.getString(R.string.release_to_cancel));
//        recordingHint.setBackgroundResource(R.drawable.ease_recording_text_hint_bg);
        voiceRecorder.setRecording(false);
        micImage.setImageResource(R.drawable.cancel_record);
    }

    public void showMoveUpToCancelHint() {
//        recordingHint.setText(context.getString(R.string.move_up_to_cancel));
//        recordingHint.setBackgroundColor(Color.TRANSPARENT);
        voiceRecorder.setRecording(true);
        micImage.setImageResource(R.drawable.cancel_record);
    }

    public void discardRecording() {
        if (wakeLock.isHeld())
            wakeLock.release();
        try {
            // 停止录音
            if (voiceRecorder.isRecording()) {
                voiceRecorder.discardRecording();
                this.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
    }

    public int stopRecoding() {
        this.setVisibility(View.INVISIBLE);
        if (wakeLock.isHeld())
            wakeLock.release();
        return voiceRecorder.stopRecoding();
    }

    public String getVoiceFilePath() {
        return voiceRecorder.getVoiceFilePath();
    }

    public String getVoiceFileName() {
        return voiceRecorder.getVoiceFileName();
    }

    public boolean isRecording() {
        return voiceRecorder.isRecording();
    }
}
