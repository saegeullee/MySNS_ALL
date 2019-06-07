package com.example.saegeullee.applicationoneproject.Utility;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class TimeManager {

    private static final String TAG = "TimeManager";

    public TimeManager() {
    }

    public static String compareTime(String timestamp) {

        StringTokenizer st = new StringTokenizer(timestamp, " ");
        String yearMonthDay = st.nextToken();
        String hourMinuteSecond = st.nextToken();

        StringTokenizer YMD = new StringTokenizer(yearMonthDay, "-");
        String year = YMD.nextToken();
        String month = YMD.nextToken();
        String day = YMD.nextToken();

        StringTokenizer HMS = new StringTokenizer(hourMinuteSecond, ":");
        String hour = HMS.nextToken();
        String minute = HMS.nextToken();
        String second = HMS.nextToken();

        Log.d(TAG, "compareTime: year : " + year + " month : " + month + " day : " + day +
                " hour : " + hour + " minute : " + minute + " second : " + second);

//        String localTime = getTimeStampForServer();
//        StringTokenizer stLocal = new StringTokenizer(localTime, " ");
//        String yearMonthDayLocal = stLocal.nextToken();
//        String hourMinuteSecondLocal = stLocal.nextToken();
//
//        StringTokenizer YMDLocal = new StringTokenizer(yearMonthDayLocal, "-");
//        String yearLocal = YMDLocal.nextToken();
//        String monthLocal = YMDLocal.nextToken();
//        String dayLocal = YMDLocal.nextToken();
//
//        StringTokenizer HMSLodal = new StringTokenizer(hourMinuteSecondLocal, ":");
//        String hourLocal = HMSLodal.nextToken();
//        String minuteLocal = HMSLodal.nextToken();
//        String secondLocal = HMSLodal.nextToken();
//
//        if(year.equals(yearLocal) && month.equals(monthLocal) && day.equals(dayLocal)) {
//
//            if(hour.equals(hourLocal)) {
//
//                if(Integer.parseInt(minuteLocal) - Integer.parseInt(minute) < 1) {
//                    return hourLocal + ":" + minuteLocal;
//                } else if(Integer.parseInt(minuteLocal) - Integer.parseInt(minute) < 10) {
//                    return hourLocal + ":" + minuteLocal;
//                }
//            } else {
//                return (Integer.parseInt(hourLocal) - Integer.parseInt(hour)) + "시간 전";
//            }
//        }

        return hour + ":" + minute;
    }

    public static String getTimeStampForServer() {
        SimpleDateFormat sdfServer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);

        sdfServer.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        return sdfServer.format(new Date());
    }


}
