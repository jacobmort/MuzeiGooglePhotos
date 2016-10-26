package porqueno.muzeigooglephotos.util;

import com.google.api.client.util.DateTime;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jacob on 7/13/16.
 */
public class TimeHelpers {
	private static final int MS_IN_AN_HOUR = 60 * 60 * 1000;

	public static String getCurrentGMTOffset() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
		String   timeZone = new SimpleDateFormat("Z", Locale.getDefault()).format(calendar.getTime());
		return timeZone.substring(0, 3) + ":"+ timeZone.substring(3, 5);
	}

	public static LocalDateTime getLocalDateFromTimeMeta(String metaTime) {
		// metaTime ex 2016:07:10 15:48:03 does not include timezone
		// comes from camera so let's tack on current timezone assuming user wants to see time
		// that they actually took it regardless of timezone
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
		LocalDateTime date;
		try{
			date = LocalDateTime.parse(metaTime, formatter);
		} catch (DateTimeParseException e) {
			date = null;
		}
		return date;
	}

	public static LocalDateTime convertFromDateTimeZulu(DateTime dateTime) {
		DateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone("Zulu"));
		return getLocalDateFromTimeMeta(formatter.format(dateTime.getValue()));
	}

	public static String getPrettyDateString(LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE d LLL, yyyy", Locale.US);
		return formatter.format(dateTime);
	}

	public static String getPrettyTimeString(LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
		return formatter.format(dateTime);
	}

	public static long getHoursToMs(int hours){
		return  hours * MS_IN_AN_HOUR;
	}

	public static int getHoursFromMs(long ms){
		return Math.round(ms / MS_IN_AN_HOUR);
	}

	public static long getEpochMs(LocalDateTime dt){
		return dt.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
	}
}
