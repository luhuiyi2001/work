package com.tencent.liteav.demo.cap.manager;

import android.content.Context;
import android.media.MediaPlayer;

import com.tencent.liteav.demo.DemoApplication;
import com.tencent.liteav.demo.R;

public class CapAudioManager {
    private static final String TAG = CapAudioManager.class.getSimpleName();
    private static final int TYPE_SOS = 1;
    private static final int TYPE_WAIT_RECEIVE = 2;
    private static final int TYPE_RECEIVED_VOICE = 3;
    private static final int TYPE_VIDEO_SHOT = 4;
    private static final int TYPE_KEY_VOLUMN = 5;

    private static CapAudioManager sMgr;
    private MediaPlayer mMediaPlayer;
    private Context mContext;

    public static CapAudioManager getInstance() {
        if (sMgr == null) {
            sMgr = new CapAudioManager();
        }
        return sMgr;
    }

    private CapAudioManager() {
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void playSos() {
        play(TYPE_SOS);
    }

    public void playWaitReceiveVoice() {
        play(TYPE_WAIT_RECEIVE);
    }

    public void playReceivedVoice() {
        play(TYPE_RECEIVED_VOICE);
    }

    public void playVideoShotVoice() {
        play(TYPE_VIDEO_SHOT);
    }

    public void playVolumnKeyVoice() {
        play(TYPE_KEY_VOLUMN);
    }

    private void play(int type) {
        if (mMediaPlayer != null) {
            stop();
        }
        int audioResId = R.raw.sos;
        if (TYPE_WAIT_RECEIVE == type) {
            audioResId = R.raw.wait_receive_voice;
        } else if (TYPE_RECEIVED_VOICE == type) {
            audioResId = R.raw.received_voice;
        } else if (TYPE_VIDEO_SHOT == type) {
            audioResId = R.raw.video_shot;
        } else if (TYPE_KEY_VOLUMN == type) {
            audioResId = R.raw.volumn_key;
        }

        this.mMediaPlayer = MediaPlayer.create(DemoApplication.getApplication(), audioResId);
        this.mMediaPlayer.start();
    }


    private void stop() {
        if (mMediaPlayer == null) {
            return;
        }
        this.mMediaPlayer.stop();
        this.mMediaPlayer.release();
        this.mMediaPlayer = null;
    }
}
