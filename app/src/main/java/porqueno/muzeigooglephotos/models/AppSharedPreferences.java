package porqueno.muzeigooglephotos.models;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jacob on 7/13/16.
 */
public class AppSharedPreferences {
	public static final String PREF_FILE_NAME = "fileName";
	public static final String PREF_ACCOUNT_NAME = "accountName";
	public static final String PREF_PAGE_TOKEN = "pageToken";

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
}
