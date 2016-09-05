package porqueno.muzeigooglephotos;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.List;

import porqueno.muzeigooglephotos.models.AppSharedPreferences;
import porqueno.muzeigooglephotos.models.PhotosModelDbHelper;
import porqueno.muzeigooglephotos.util.GoogleCredentialHelper;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GooglePhotosAuthActivity extends Activity
		implements EasyPermissions.PermissionCallbacks, PhotosReceivedInterface {
	static final int REQUEST_ACCOUNT_PICKER = 1000;
	static final int REQUEST_AUTHORIZATION = 1001;
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
	static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

	private ProgressDialog mProgress;
	private static final boolean JOB_SCHEDULER_AVAILABLE = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getApplicationContext().getString(R.string.photo_fetch));

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
		} else if (GoogleCredentialHelper.get(getApplicationContext()).getSelectedAccountName() == null) {
			chooseAccount();
		} else if (! isDeviceOnline()) {
			Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show();
		} else {
			new PhotosFetchAsyncTask(
					this,
					GoogleCredentialHelper.get(getApplicationContext()),
					AppSharedPreferences.getLastPageToken(getApplicationContext()),
					!JOB_SCHEDULER_AVAILABLE
			).execute();
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
			String accountName = AppSharedPreferences.getGoogleAccountName(getApplicationContext());
			if (accountName != null) {
				GoogleCredentialHelper.setAccoutName(accountName);
				getResultsFromApi();
			} else {
				// Start a dialog from which the user can choose an account
				startActivityForResult(
						GoogleCredentialHelper.get(getApplicationContext()).newChooseAccountIntent(),
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
						AppSharedPreferences.setGoogleAccountName(getApplicationContext(), accountName);
						GoogleCredentialHelper.setAccoutName(accountName);
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

	public void fetchedPhotos(FileList photos){
		String pageToken = photos.getNextPageToken();
		List<File> files = photos.getFiles();

		if (pageToken != null) {
			AppSharedPreferences.setLastPageToken(getApplicationContext(), pageToken);
		}
		if (files != null) {
			PhotosModelDbHelper pdb = PhotosModelDbHelper.getHelper(getApplicationContext());
			pdb.savePhotos(files);
		}
	}

	public void doneFetching(){
		if (JOB_SCHEDULER_AVAILABLE){
			JobInfo jobInfo = new JobInfo.Builder(PhotosFetchJobService.JOB_ID, new ComponentName(getBaseContext(), PhotosFetchJobService.class))
					.setRequiresCharging(true)
					.setPersisted(true)
					.setPeriodic(PhotosFetchJobService.HOW_FREQ_TO_RUN_MS)
					.setRequiresDeviceIdle(true)
					.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
					.build();
			JobScheduler scheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
			scheduler.schedule(jobInfo);
		}

		mProgress.dismiss();
		finish();
	}

	public void onCancel(Exception exception){
		mProgress.hide();
		if (exception != null) {
			if (exception instanceof GooglePlayServicesAvailabilityIOException) {
				showGooglePlayServicesAvailabilityErrorDialog(
						((GooglePlayServicesAvailabilityIOException) exception)
								.getConnectionStatusCode());
			} else if (exception instanceof UserRecoverableAuthIOException) {
				startActivityForResult(
						((UserRecoverableAuthIOException) exception).getIntent(),
						GooglePhotosAuthActivity.REQUEST_AUTHORIZATION);
			} else {
				Toast.makeText(getApplicationContext(),"The following error occurred:\n"
						+ exception.getMessage(), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), "Request cancelled.", Toast.LENGTH_SHORT).show();
		}
	}

	public void onStartFetch(){
		mProgress.show();
	}
}