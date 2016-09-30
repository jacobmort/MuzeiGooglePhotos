package porqueno.muzeigooglephotos.util;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;

import porqueno.muzeigooglephotos.models.AppSharedPreferences;

/**
 * Created by jacob on 7/14/16.
 */
public class GoogleCredentialHelpers {
	private static final String[] DRIVE_SCOPES = { DriveScopes.DRIVE_PHOTOS_READONLY };

	public static GoogleAccountCredential get(Context ctx){
		String accountName = AppSharedPreferences.getGoogleAccountName(ctx);
		return GoogleAccountCredential.usingOAuth2(
				ctx, Arrays.asList(DRIVE_SCOPES))
				.setBackOff(new ExponentialBackOff())
				.setSelectedAccountName(accountName);
	}
}
