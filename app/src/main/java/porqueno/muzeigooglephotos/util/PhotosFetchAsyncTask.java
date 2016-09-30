package porqueno.muzeigooglephotos.util;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;

/**
 * Created by jacob on 8/29/16.
 */
public class PhotosFetchAsyncTask extends AsyncTask<Void, FileList, Void> {
	private static final String PHOTO_FIELDS = "files(createdTime,id,imageMediaMetadata(location(latitude,longitude),time)),nextPageToken";
	private PhotosReceivedInterface mPhotosReceivedInterface;
	private String mStartingPageToken;
	private boolean mFetchAll = false;
	private com.google.api.services.drive.Drive mService = null;
	private Exception mLastError = null;

	public PhotosFetchAsyncTask(Context ctx, GoogleAccountCredential credential, String pageToken, boolean fetchAll) {
		mPhotosReceivedInterface = (PhotosReceivedInterface) ctx;
		mStartingPageToken = pageToken;
		mFetchAll = fetchAll;
		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		mService = new com.google.api.services.drive.Drive.Builder(
				transport, jsonFactory, credential)
				.setApplicationName("Muzei Google Photos")
				.build();
	}

	/**
	 * Background task to call Drive API.
	 * @param params no parameters needed for this task.
	 */
	@Override
	protected Void doInBackground(Void... params) {
		String pageToken = mStartingPageToken;
		FileList result;
		// No startingPageToken- run at least once
		// startingPageToken- run at least once then continue on mFetchAll
		boolean ranOnce = false;
		try {
			while ((pageToken != null && (mFetchAll || !ranOnce)) || (pageToken == null && !ranOnce)) {
				ranOnce = true;
				result = getDataFromApi(pageToken);
				mPhotosReceivedInterface.fetchedPhotos(result);
				pageToken = result.getNextPageToken();
			}
		} catch (IOException e) {
			mLastError = e;
			cancel(true);
		}
		return null;
	}

	/**
	 * Fetch a list of up to 1000 file names and IDs.
	 * @return List of Strings describing files, or an empty list if no files
	 *         found.
	 * @throws IOException
	 */
	private FileList getDataFromApi(String pageToken) throws IOException {
		Drive.Files.List apiCall = mService.files().list()
				.setSpaces("photos")
				.setOrderBy("createdTime")
				.setFields(PHOTO_FIELDS)
				.setQ("mimeType contains 'image/'")
				.setPageSize(1000);
		if (pageToken != null) {
			apiCall.setPageToken(pageToken);
		}
		return apiCall.execute();
	}


	@Override
	protected void onPreExecute() {
		mPhotosReceivedInterface.onStartFetch();
	}

	@Override
	protected void onPostExecute(Void v) {
		mPhotosReceivedInterface.doneFetching();
	}

	@Override
	protected void onCancelled() {
		mPhotosReceivedInterface.onCancel(mLastError);
	}
}
