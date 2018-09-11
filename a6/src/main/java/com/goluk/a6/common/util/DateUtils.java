
package com.goluk.a6.common.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {

    public static long getUnixStamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static long getTodayBeginTimestamp() {
        Date date = new Date();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        Date date2 = new Date(date.getTime() - gc.get(Calendar.HOUR_OF_DAY) * 60 * 60
                * 1000 - gc.get(Calendar.MINUTE) * 60 * 1000 - gc.get(Calendar.SECOND)
                * 1000);
        return date2.getTime() / 1000;
    }

    public static long getYesterdayBeginTimestamp() {
        return getTodayBeginTimestamp() - 24 * 60 * 60;
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "00:00";
        }
        DateFormat dateTimeformat;
        if (isToday(0, date)) {
            dateTimeformat = new SimpleDateFormat("HH:mm");
        } else {
            dateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
        return dateTimeformat.format(new Date());
    }

    public static String formatDateTime(Date date) {
        if (date == null) {
            return "00:00";
        }
        DateFormat dateTimeformat;
        dateTimeformat = new SimpleDateFormat("HH:mm");
        return dateTimeformat.format(date);
    }

    public static String formatDateAndTime(Date date) {
        if (date == null) {
            return "00:00";
        }
        DateFormat dateTimeformat;
        if (isToday(0, date)) {
            dateTimeformat = new SimpleDateFormat("HH:mm");
        } else {
            dateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
        String strBeginDate = dateTimeformat.format(date);
        return strBeginDate;
    }

    public static String getDate(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("HH:mm");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    public static String parseToMinite(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            TimeZone timeZone = TimeZone.getDefault();
            Date netDate = (new Date(timeStamp - timeZone.getRawOffset()));
            String result = sdf.format(netDate);
            if (result.startsWith("00:"))
                result = result.substring(3, result.length());
            return result;
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getTime(int seconds) {
        if (seconds < 0 || seconds > 2 * 60 * 60) {
            seconds = 0;
        }
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        String hrStr = (hr < 10 ? "0" : "") + hr;
        String mnStr = (mn < 10 ? "0" : "") + mn;
        String secStr = (sec < 10 ? "0" : "") + sec;
        return hrStr + ":" + mnStr + ":" + secStr;
    }

    public static String getDateAndTime(long timeStamp) {
        DateFormat sdf;
        try {
            if (isToday(timeStamp, null)) {
                sdf = new SimpleDateFormat("HH:mm");
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            }
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    public static boolean isToday(long timeStamp, Date tempDate) {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date;
        if (tempDate == null) {
            date = new Date(timeStamp);
        } else {
            date = tempDate;
        }
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
            if (diffDay == 0) {
                return true;
            }
        }
        return false;
    }

    private static final int MIN_CLICK_DELAY_TIME = 500;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) < MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    /**
     * 毫秒值 --> xx月xx日 xx:xx
     *
     * @param time 毫秒值
     */
    public static String parseMillesToTimeString(long time) {
        Date date = new Date(time);
        SimpleDateFormat formater = new SimpleDateFormat("MM-dd HH:mm");
        return formater.format(date);
    }

    /**
     * 时间转星期几
     */
    public static String getWeek(String sdate) {
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()) + " "
                + new SimpleDateFormat("EEEE").format(c.getTime());
    }

    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    public static String parseToDayStr(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    public static String formatDateString(long time) {
        Date date = new Date(time);
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return formater.format(date);
    }

    public static String formatDateNomalString(long time) {
        Date date = new Date(time);
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return formater.format(date);
    }

}
