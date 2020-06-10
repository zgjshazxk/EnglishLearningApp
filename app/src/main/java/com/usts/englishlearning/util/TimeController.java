package com.usts.englishlearning.util;

import android.util.Log;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*时间管理*/
public class TimeController {

    public static final String FORMAT_NOTIME = "yyyy-MM-dd";

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_NOTIME);

    public static long todayDate;

    private static final String TAG = "TimeController";

    /*----日期类----*/

    // 得到当前日期戳(不带时间，只有日期)
    public static long getCurrentDateStamp() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        int currentDate = cal.get(Calendar.DATE);
        long time = 0;
        try {
            time = simpleDateFormat.parse(currentYear + "-" + currentMonth + "-" + currentDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getCurrentDateStamp: " + time);
        return time;
    }

    // 根据指定日期戳解析成日期形式（yyyy-MM-dd）
    public static String getStringDate(long timeStamp) {
        return simpleDateFormat.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
    }

    // 根据指定日期戳解析成日期形式（yyyy-MM-dd）
    public static String getStringDateDetail(long timeStamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
    }

    // 得到当前日期的指定间隔后的日期
    public static long getDateByDays(long time, int intervalDay) throws ParseException {
        // 转换成Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.DATE, intervalDay);
        return calendar.getTimeInMillis();
    }

    // 返回两个日期之间相隔多少天
    public static int daysInternal(long time1, long time2) throws ParseException {
        Date date1 = simpleDateFormat.parse(getStringDate(time1));
        Date date2 = simpleDateFormat.parse(getStringDate(time2));
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    /*----时间类----*/

    // 得到当前时间戳（有日期与时间）
    public static long getCurrentTimeStamp() {
        return System.currentTimeMillis();
    }

    // 判断两个时间戳是否为同一天
    public static boolean isTheSameDay(long time1, long time2) {
        return getStringDate(time1).equals(getStringDate(time2));
    }

    // 返回过去第几天的日期
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        String result = format.format(today);
        return result;
    }

    // 返回过去第几天的日期（有年份）
    public static String getPastDateWithYear(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        return result;
    }

    // 获取n天以后的日期
    public static String getDayAgoOrAfterString(int num) {
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
        calendar1.add(Calendar.DATE, num);
        return sdf1.format(calendar1.getTime());
    }
}
