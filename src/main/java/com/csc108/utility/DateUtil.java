package com.csc108.utility;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
	public final static DateTimeFormatter Date_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");	
	public final static DateTimeFormatter Date_YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
	public final static DateTimeFormatter Date_YYYYMMDDHHMMSS2 = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
	public final static DateTimeFormatter Date_YYYYMMDDHHMMSS_SSS = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss:SSS");
	public final static DateTimeFormatter Date_YYYYMMDDHHMMSS_SSS2 = DateTimeFormatter.ofPattern("yyyyMMdd HHmmssSSS");
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
	
	public static String dateTime2Str(LocalDateTime dt_) {
		return dt_.format(Date_YYYYMMDDHHMMSS);
	}
	
	public static String dateTime2Str2(LocalDateTime dt_) {
		return dt_.format(Date_YYYYMMDDHHMMSS_SSS);
	}
	
	public static String date2Str(LocalDateTime dt_) {
		return dt_.format(Date_YYYYMMDD);
	}
	
	//15:00:00 is counted to next start
	public static LocalDateTime getFrom(LocalDateTime t_, Duration d_) {	
		int minute = t_.getMinute();		
		int from = (int)Util.roundQtyDown(minute, d_.toMinutes());		
		LocalDateTime fromDt = t_.withMinute(from).withSecond(0).withNano(0);		
		return fromDt;
	}
	
	//15:00:00 is counted to 
	public static LocalDateTime getTo(LocalDateTime t_, Duration d_) {		
		int minute = t_.getMinute();			
		int to =(int)Util.roundQtyUp(minute + 1, d_.toMinutes());		
		if(to < 60)
			return t_.withMinute(to).withSecond(0).withNano(0);
		else 
			return t_.plusHours(1).withMinute(0).withSecond(0).withNano(0);
	}
}
