package porqueno.muzeigooglephotos;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GooglePhotosAuthActivity extends Activity
		implements EasyPermissions.PermissionCallbacks {
	static final int REQUEST_ACCOUNT_PICKER = 1000;
	static final int REQUEST_AUTHORIZATION = 1001;
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
	static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

	public static final String PREF_FILE_NAME = "fileName";
	public static final String PREF_ACCOUNT_NAME = "accountName";
	public static final String[] DRIVE_SCOPES = { DriveScopes.DRIVE_PHOTOS_READONLY };

	private static final String PHOTO_FIELDS = "files(id),nextPageToken";
	private GoogleAccountCredential mCredential;
	private ProgressDialog mProgress;


	/**
	 * Create the main activity.
	 * @param savedInstanceState previously saved instance data.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("Calling Drive API ...");

		// Initialize credentials and service object.
		mCredential = GoogleAccountCredential.usingOAuth2(
				getApplicationContext(), Arrays.asList(DRIVE_SCOPES))
				.setBackOff(new ExponentialBackOff());
		getResultsFromApi();
	}

	/**
	 * Attempt to call the API, after verifying that all the preconditions are
	 * satisfied. The preconditions are: Google Play Services installed, an
	 * account was selected and the device currently has online access. If any
	 * of the preconditions are not satisfied, the app will prompt the user as
	 * appropriate.
	 */
	private void getResultsFromApi() {
		if (! isGooglePlayServicesAvailable(this)) {
			acquireGooglePlayServices(this);
		} else if (mCredential.getSelectedAccountName() == null) {
			chooseAccount();
		} else if (! isDeviceOnline()) {
			Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show();
		} else {
			new MakeRequestTask(mCredential).execute();
		}
	}

	/**
	 * Attempts to set the account used with the API credentials. If an account
	 * name was previously saved it will use that one; otherwise an account
	 * picker dialog will be shown to the user. Note that the setting the
	 * account to use with the credentials object requires the app to have the
	 * GET_ACCOUNTS permission, which is requested here if it is not already
	 * present. The AfterPermissionGranted annotation indicates that this
	 * function will be rerun automatically whenever the GET_ACCOUNTS permission
	 * is granted.
	 */
	@AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
	private void chooseAccount() {
		if (EasyPermissions.hasPermissions(
				this, Manifest.permission.GET_ACCOUNTS)) {
			String accountName = getGoogleAccountName(getApplicationContext());
			if (accountName != null) {
				mCredential.setSelectedAccountName(accountName);
				getResultsFromApi();
			} else {
				// Start a dialog from which the user can choose an account
				startActivityForResult(
						mCredential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
			}
		} else {
			// Request the GET_ACCOUNTS permission via a user dialog
			EasyPermissions.requestPermissions(
					this,
					"This app needs to access your Google account (via Contacts).",
					REQUEST_PERMISSION_GET_ACCOUNTS,
					Manifest.permission.GET_ACCOUNTS);
		}
	}

	/**
	 * Called when an activity launched here (specifically, AccountPicker
	 * and authorization) exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 * @param requestCode code indicating which activity result is incoming.
	 * @param resultCode code indicating the result of the incoming
	 *     activity result.
	 * @param data Intent (containing result data) returned by incoming
	 *     activity result.
	 */
	@Override
	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
			case REQUEST_GOOGLE_PLAY_SERVICES:
				if (resultCode != RESULT_OK) {
					Toast.makeText(this, R.string.missing_google_services, Toast.LENGTH_SHORT).show();
				} else {
					getResultsFromApi();
				}
				break;
			case REQUEST_ACCOUNT_PICKER:
				if (resultCode == RESULT_OK && data != null &&
						data.getExtras() != null) {
					String accountName =
							data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					if (accountName != null) {
						SharedPreferences settings =
								getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(PREF_ACCOUNT_NAME, accountName);
						editor.apply();
						mCredential.setSelectedAccountName(accountName);
						getResultsFromApi();
					}
				}
				break;
			case REQUEST_AUTHORIZATION:
				if (resultCode == RESULT_OK) {
					getResultsFromApi();
				}
				break;
		}
	}

	/**
	 * Respond to requests for permissions at runtime for API 23 and above.
	 * @param requestCode The request code passed in
	 *     requestPermissions(android.app.Activity, String, int, String[])
	 * @param permissions The requested permissions. Never null.
	 * @param grantResults The grant results for the corresponding permissions
	 *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(
				requestCode, permissions, grantResults, this);
	}

	/**
	 * Callback for when a permission is granted using the EasyPermissions
	 * library.
	 * @param requestCode The request code associated with the requested
	 *         permission
	 * @param list The requested permission list. Never null.
	 */
	@Override
	public void onPermissionsGranted(int requestCode, List<String> list) {
		// Do nothing.
	}

	/**
	 * Callback for when a permission is denied using the EasyPermissions
	 * library.
	 * @param requestCode The request code associated with the requested
	 *         permission
	 * @param list The requested permission list. Never null.
	 */
	@Override
	public void onPermissionsDenied(int requestCode, List<String> list) {
		// Do nothing.
	}

	/**
	 * Checks whether the device currently has a network connection.
	 * @return true if the device has a network connection, false otherwise.
	 */
	private boolean isDeviceOnline() {
		ConnectivityManager connMgr =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	/**
	 * Check that Google Play services APK is installed and up to date.
	 * @return true if Google Play Services is available and up to
	 *     date on this device; false otherwise.
	 */
	public static boolean isGooglePlayServicesAvailable(Context ctx) {
		GoogleApiAvailability apiAvailability =
				GoogleApiAvailability.getInstance();
		final int connectionStatusCode =
				apiAvailability.isGooglePlayServicesAvailable(ctx);
		return connectionStatusCode == ConnectionResult.SUCCESS;
	}

	/**
	 * Attempt to resolve a missing, out-of-date, invalid or disabled Google
	 * Play Services installation via a user dialog, if possible.
	 */
	private void acquireGooglePlayServices(Context ctx) {
		GoogleApiAvailability apiAvailability =
				GoogleApiAvailability.getInstance();
		final int connectionStatusCode =
				apiAvailability.isGooglePlayServicesAvailable(ctx);
		if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
		}
	}


	/**
	 * Display an error dialog showing that Google Play Services is missing
	 * or out of date.
	 * @param connectionStatusCode code describing the presence (or lack of)
	 *     Google Play Services on this device.
	 */
	void showGooglePlayServicesAvailabilityErrorDialog(
			final int connectionStatusCode) {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		Dialog dialog = apiAvailability.getErrorDialog(
				this,
				connectionStatusCode,
				REQUEST_GOOGLE_PLAY_SERVICES);
		dialog.show();
	}

	/**
	 * An asynchronous task that handles the Drive API call.
	 * Placing the API calls in their own task ensures the UI stays responsive.
	 */
	private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
		private com.google.api.services.drive.Drive mService = null;
		private Exception mLastError = null;

		public MakeRequestTask(GoogleAccountCredential credential) {
			HttpTransport transport = AndroidHttp.newCompatibleTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			mService = new com.google.api.services.drive.Drive.Builder(
					transport, jsonFactory, credential)
					.setApplicationName("Drive API Android Quickstart")
					.build();
		}

		/**
		 * Background task to call Drive API.
		 * @param params no parameters needed for this task.
		 */
		@Override
		protected List<String> doInBackground(Void... params) {
			try {
				return getDataFromApi();
			} catch (Exception e) {
				mLastError = e;
				cancel(true);
				return null;
			}
		}

		/**
		 * Fetch a list of up to 10 file names and IDs.
		 * @return List of Strings describing files, or an empty list if no files
		 *         found.
		 * @throws IOException
		 */
		private List<String> getDataFromApi() throws IOException {
			// Get a list of up to 10 files.
			List<String> fileInfo = new ArrayList<String>();
			FileList result = mService.files().list()
					.setSpaces("photos")
					.setFields(PHOTO_FIELDS)
					.setPageSize(1000)
					.execute();
			List<File> files = result.getFiles();
			if (files != null) {
				for (File file : files) {
					fileInfo.add(file.getWebContentLink());
				}
			}
			return fileInfo;
		}


		@Override
		protected void onPreExecute() {
			mProgress.show();
		}

		@Override
		protected void onPostExecute(List<String> output) {
			mProgress.dismiss();
			finish();
		}

		@Override
		protected void onCancelled() {
			mProgress.hide();
			if (mLastError != null) {
				if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
					showGooglePlayServicesAvailabilityErrorDialog(
							((GooglePlayServicesAvailabilityIOException) mLastError)
									.getConnectionStatusCode());
				} else if (mLastError instanceof UserRecoverableAuthIOException) {
					startActivityForResult(
							((UserRecoverableAuthIOException) mLastError).getIntent(),
							GooglePhotosAuthActivity.REQUEST_AUTHORIZATION);
				} else {
					Toast.makeText(getApplicationContext(),"The following error occurred:\n"
							+ mLastError.getMessage(), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getApplicationContext(), "Request cancelled.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static String getGoogleAccountName(Context ctx) {
		return ctx.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
				.getString(PREF_ACCOUNT_NAME, null);
	}
}