package porqueno.muzeigooglephotos.util;

import com.google.api.client.util.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jacob on 7/13/16.
 */
public class TimeHelpers {
	public static String getCurrentGMTOffset() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
		String   timeZone = new SimpleDateFormat("Z", Locale.getDefault()).format(calendar.getTime());
		return timeZone.substring(0, 3) + ":"+ timeZone.substring(3, 5);
	}

	public static Date getDateFromTimeMeta(String metaTime) {
		// metaTime ex 2016:07:10 15:48:03 does not include timezone
		// comes from camera so let's tack on current timezone assuming user wants to see time
		// that they actually took it regardless of timezone
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss", Locale.getDefault());
		sdf.setTimeZone(TimeZone.getDefault());
		Date date;
		try{
			date = sdf.parse(metaTime);
		} catch (java.text.ParseException e) {
			date = null;
		}
		return date;
	}

	public static String getPrettyDateString(DateTime dateTime) {
		DateFormat formatter = new SimpleDateFormat("EEE d LLL, yyyy", Locale.US);
		return formatter.format(dateTime.getValue());
	}

	public static String getPrettyTimeString(DateTime dateTime) {
		DateFormat formatter = new SimpleDateFormat("h:m a", Locale.US);
		return formatter.format(dateTime.getValue());
	}

}
