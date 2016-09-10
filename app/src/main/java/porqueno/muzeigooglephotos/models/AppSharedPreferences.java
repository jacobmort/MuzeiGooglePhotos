package porqueno.muzeigooglephotos.models;

import android.content.Context;
import android.content.SharedPreferences;

import porqueno.muzeigooglephotos.util.TimeHelpers;

/**
 * Created by jacob on 7/13/16.
 */
public class AppSharedPreferences {
	private static final String PREF_FILE_NAME = "fileName";
	private static final String PREF_ACCOUNT_NAME = "accountName";
	private static final String PREF_PAGE_TOKEN = "pageToken";
	private static final String PREF_REFRESH_DURATION = "refreshDuration";

	private static final int DEFAULT_ROTATE_TIME_MILLIS = 2 * 60 * 60 * 1000; // rotate every 2 hours

	public static void setGoogleAccountName(Context ctx, String accountName) {
		SharedPreferences settings =
				ctx.getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_ACCOUNT_NAME, accountName);
		editor.apply();
	}

	public static String getGoogleAccountName(Context ctx) {
		return ctx.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getString(PREF_ACCOUNT_NAME, null);
	}

	public static void setLastPageToken(Context ctx, String pageToken) {
		SharedPreferences settings =
				ctx.getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_PAGE_TOKEN, pageToken);
		editor.apply();
	}

	public static String getLastPageToken(Context ctx) {
		return ctx.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getString(PREF_PAGE_TOKEN, null);
	}

	public static void setRefreshDurationMs(Context ctx, int hours){
		SharedPreferences settings =
				ctx.getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(PREF_REFRESH_DURATION, TimeHelpers.getHoursToMs(hours));
		editor.apply();
	}

	public static long getRefreshDurationMs(Context ctx) {
		return ctx.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getLong(PREF_REFRESH_DURATION, DEFAULT_ROTATE_TIME_MILLIS);
	}
}
