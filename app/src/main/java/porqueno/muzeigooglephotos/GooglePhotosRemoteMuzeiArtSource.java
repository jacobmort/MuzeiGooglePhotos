package porqueno.muzeigooglephotos;

import android.content.Intent;
import android.net.Uri;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.gms.auth.GoogleAuthException;

import java.io.IOException;

import porqueno.muzeigooglephotos.models.AppSharedPreferences;
import porqueno.muzeigooglephotos.models.PhotoModel;
import porqueno.muzeigooglephotos.models.PhotosModelDbHelper;
import porqueno.muzeigooglephotos.util.GoogleCredentialHelpers;
import porqueno.muzeigooglephotos.util.TimeHelpers;

/**
 * Created by jacob on 6/16/16.
 */
public class GooglePhotosRemoteMuzeiArtSource extends RemoteMuzeiArtSource {
	private static final String TAG = "GooglePhotosRemoteMuzeiArtSource";
	private String token;

	public GooglePhotosRemoteMuzeiArtSource() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
	}

	@Override
	protected void onTryUpdate(int reason) throws RetryException {
		String accountName = AppSharedPreferences.getGoogleAccountName(getApplicationContext());

		if (accountName != null) {
			GoogleCredentialHelpers.get(getApplicationContext()).setSelectedAccountName(accountName);
		}

		if (!GooglePhotosAuthActivity.isGooglePlayServicesAvailable(getApplicationContext()) ||
				GoogleCredentialHelpers.get(getApplicationContext()).getSelectedAccountName() == null ) {
			Intent i = new Intent(this, GooglePhotosAuthActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} else {
			fetchNewPhoto();
		}
	}

	private void fetchNewPhoto() {
		try {
			token = GoogleCredentialHelpers.get(getApplicationContext()).getToken();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			e.printStackTrace();
		}
		// Fetch URL from google photos
		PhotosModelDbHelper pdb = PhotosModelDbHelper.getHelper(getApplicationContext());
		PhotoModel photo = pdb.getNextPhoto();

		publishArtwork(new Artwork.Builder()
				.title(TimeHelpers.getPrettyDateString(photo.getCreatedTime()))
				.byline(TimeHelpers.getPrettyTimeString(photo.getCreatedTime()))
				.imageUri(Uri.parse(photo.getUrl(token)))
				.token(photo.getId())
				.viewIntent(new Intent(Intent.ACTION_VIEW,
						Uri.parse(photo.getUrl(token))))
				.build());

		setNextRefresh();
	}

	private void setNextRefresh(){
		long refreshMs = AppSharedPreferences.getRefreshDurationMs(getApplicationContext());
		scheduleUpdate(System.currentTimeMillis() + refreshMs);
	}
}
