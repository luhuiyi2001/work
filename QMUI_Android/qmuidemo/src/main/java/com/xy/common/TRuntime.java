package com.xy.common;

import com.xy.util.TUtils;

import java.util.Calendar;

public class TRuntime {

    private long mStartTime;
    private long mEndTime;
    private int mExchage = TUtils.EXCHAGE_TYPE_CN;

    private static TRuntime sInstance;

    private static TRuntime getRuntime() {
        if (sInstance == null) {
            sInstance = new TRuntime();
        }
        return sInstance;
    }

    private TRuntime() {
        Calendar calendar = Calendar.getInstance();
        //set yesterday
        calendar.setTimeInMillis(calendar.getTimeInMillis() - TUtils.TIME_ONE_DAY);
        mEndTime = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        mStartTime = calendar.getTimeInMillis();
    }

    public static long getStartTime() {
        return getRuntime().mStartTime;
    }

    public static void setStartTime(long startTime) {
        getRuntime().mStartTime = startTime;
    }

    public static long getEndTime() {
        return getRuntime().mEndTime;
    }

    public static void setEndTime(long endTime) {
        getRuntime().mEndTime = endTime;
    }

    public static int getExchage() {
        return getRuntime().mExchage;
    }

    public static void setExchage(int exchage) {
        getRuntime().mExchage = exchage;
    }

}
