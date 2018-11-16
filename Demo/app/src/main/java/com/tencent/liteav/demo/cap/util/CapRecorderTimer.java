package com.tencent.liteav.demo.cap.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by melo on 2017/9/21.
 */

public class CapRecorderTimer {

    private Timer timer;
    private TimerTask task;
    private OnScheduleListener mListener;

    public CapRecorderTimer() {
        timer = new Timer();
    }

    public void startTimer(long delay, long period) {
        task = new TimerTask() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onSchedule();
                }
            }
        };
        timer.schedule(task, delay, period);
    }

    public void exit() {
        if (task != null) {
            task.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    public interface OnScheduleListener {
        void onSchedule();
    }

    public void setOnScheduleListener(OnScheduleListener listener) {
        this.mListener = listener;
    }
}
