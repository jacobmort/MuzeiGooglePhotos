package porqueno.muzeigooglephotos;

import android.content.Intent;
import android.net.Uri;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by jacob on 6/16/16.
 */
public class GooglePhotosRemoteMuzeiArtSource extends RemoteMuzeiArtSource {
	private static final String TAG = "GooglePhotosRemoteMuzeiArtSource";
	private static final int ROTATE_TIME_MILLIS = 2 * 60 * 60 * 1000; // rotate every 2 hours

	private GoogleAccountCredential mCredential;
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
		mCredential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(GooglePhotosAuthActivity.DRIVE_SCOPES))
				.setBackOff(new ExponentialBackOff());

		String accountName = GooglePhotosAuthActivity.getGoogleAccountName(getApplicationContext());

		if (accountName != null) {
			mCredential.setSelectedAccountName(accountName);
		}

		if (!GooglePhotosAuthActivity.isGooglePlayServicesAvailable(getApplicationContext()) ||
				mCredential.getSelectedAccountName() == null ) {
			Intent i = new Intent(this, GooglePhotosAuthActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} else {
			fetchNewPhoto();
		}
	}

	private void fetchNewPhoto() {
		try {
			token = mCredential.getToken();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			e.printStackTrace();
		}
		// Fetch URL from google photos
		String photo = "https://www.googleapis.com/drive/v3/files/1VUOucLBywhhneWMjoa5JYqZcVweKCjoS6Q?alt=media";
		photo += "&access_token=" + token;

		publishArtwork(new Artwork.Builder()
				.title("Photo title")
				.byline("Byline")
				.imageUri(Uri.parse(photo))
				.token("UNIQUE KEY FOR PHOTO")
				.viewIntent(new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://500px.com/photo/162495199")))
				.build());

		scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
	}
}
