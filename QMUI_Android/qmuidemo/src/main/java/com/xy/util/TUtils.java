package com.xy.util;

import android.support.annotation.NonNull;

import com.google.utilcode.util.TimeUtils;
import com.qmuiteam.qmuidemo.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TUtils {
    public static final int EXCHAGE_TYPE_CN = 0;
    public static final int EXCHAGE_TYPE_SH = 1;
    public static final int EXCHAGE_TYPE_SZ = 2;
    public static final int EXCHAGE_TYPE_CY = 3;
    public static SimpleDateFormat SDF_CN_DATE = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
    public static SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    public static SimpleDateFormat SDF_SIMPLE_DATE = new SimpleDateFormat("yyMMdd", Locale.getDefault());
    public static long TIME_ONE_DAY = 24 * 60 * 60 * 1000;

    public static String getExchageTypeName(int type) {
        switch (type) {
            case EXCHAGE_TYPE_CN:
                return "CN";
            case EXCHAGE_TYPE_SH:
                return "SH";
            case EXCHAGE_TYPE_SZ:
                return "SZ";
            case EXCHAGE_TYPE_CY:
                return "CY";
        }
        return "ALL";
    }

    public static String getExchageName(int type) {
        switch (type) {
            case EXCHAGE_TYPE_CN:
                return "沪深股通";
            case EXCHAGE_TYPE_SH:
                return "沪股通";
            case EXCHAGE_TYPE_SZ:
                return "深股通";
            case EXCHAGE_TYPE_CY:
                return "创业板";
        }
        return "所有";
    }

}
