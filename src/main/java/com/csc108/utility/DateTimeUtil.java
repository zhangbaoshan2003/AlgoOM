package com.csc108.utility;

import com.csc108.log.LogFactory;
import quickfix.FieldNotFound;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Created by LEGEN on 2016/5/2.
 */
public class DateTimeUtil {
    public final static DateTimeFormatter Date_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public final static DateTimeFormatter Date_YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
    public final static DateTimeFormatter Date_YYYYMMDDHHMMSS2 = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
    public final static DateTimeFormatter Date_YYYYMMDDHHMMSS_SSS = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss:SSS");
    public final static DateTimeFormatter Date_YYYYMMDDHHMMSS_SSS2 = DateTimeFormatter.ofPattern("yyyyMMdd HHmmssSSS");
    public final static DateTimeFormatter Date_YYYYMMDD_HHMMSS = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss");

    public final static DateTimeFormatter TIME_HHMMSS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public final static LocalDateTime MaxValue = LocalDateTime.MAX;
    public final static LocalDateTime MinValue = LocalDateTime.MIN;

    public static LocalDate getDate(String dateStr_) {
        return LocalDate.parse(dateStr_, Date_YYYYMMDD);
    }

    /*with million second HH:mm:ss*/
    public static LocalDateTime getDateTime(String dateStr_) {
        return LocalDateTime.parse(dateStr_, Date_YYYYMMDDHHMMSS);
    }

    /*with million second HHmmss*/
    public static LocalDateTime getDateTime2(String dateStr_) {
        return LocalDateTime.parse(dateStr_, Date_YYYYMMDDHHMMSS2);
    }

    public static LocalDateTime getDateTime3(String dateStr_) {
        return LocalDateTime.parse(dateStr_, Date_YYYYMMDDHHMMSS_SSS);
    }

    public static LocalDateTime getDateTime4(String dateStr_) {
        return LocalDateTime.parse(dateStr_, Date_YYYYMMDDHHMMSS_SSS2);
    }

    public static LocalDateTime getDateTime5(String dateStr_) {
        return LocalDateTime.of(LocalDate.now(),LocalDateTime.parse(dateStr_, Date_YYYYMMDD_HHMMSS).toLocalTime()) ;
    }

    public static LocalDateTime getDateTime6(String dateStr_) {
        return LocalDateTime.parse(dateStr_, DateTimeFormatter.ofPattern("HH:mm:ss")).plusHours(8);
    }

    public static String dateTime2Str(LocalDateTime dt_) {
        return dt_.format(Date_YYYYMMDD_HHMMSS);
    }

    public static String allDayStartTradingTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+"-09:15:00";
    }

    public static String allDayEndTradingTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+"-15:00:00";
    }


    public static String time2Str(LocalTime dt_) {
        return dt_.format(TIME_HHMMSS);
    }

    public static String dateTime2Str2(LocalDateTime dt_) {
        return dt_.format(Date_YYYYMMDDHHMMSS_SSS);
    }

    public static String date2Str(LocalDateTime dt_) {
        return dt_.format(Date_YYYYMMDD);
    }

    public static long getMinutesDifference(long from, long to){
        return (to-from)/ (60 * 1000);
    }

    public static void EvaluateEffetiveTime(quickfix.Message order) {
        Boolean isset = Boolean.valueOf(order.isSetField(168));
        if(isset) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hh:mm:ss");
                String effectivetime = order.getString(168);
                String e = effectivetime.substring(9, 11);
                String min = effectivetime.substring(12, 14);
                if(e != null && !"".equals(e) && min != null && !"".equals(min)) {
                    int innerhours = Integer.parseInt(e);
                    int innermin = Integer.parseInt(min);
                    if(innerhours < 9 && innermin < 30) {
                        order.setString(168, sdf.format(new Date()));
                    }
                }
            } catch (FieldNotFound var8) {
                var8.printStackTrace();
                LogFactory.error("Calling fixtools.EditTime error!", var8);
            }
        }
    }
}
