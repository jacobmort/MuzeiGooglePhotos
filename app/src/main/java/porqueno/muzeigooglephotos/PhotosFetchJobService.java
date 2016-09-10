package porqueno.muzeigooglephotos;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import porqueno.muzeigooglephotos.models.AppSharedPreferences;
import porqueno.muzeigooglephotos.models.PhotosModelDbHelper;
import porqueno.muzeigooglephotos.util.GoogleCredentialHelper;

/**
 * Created by jacob on 8/29/16.
 */
public class PhotosFetchJobService extends JobService implements PhotosReceivedInterface{
	static final long HOW_FREQ_TO_RUN_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
	static final int JOB_ID = 2001;
	private static final String TAG = "PhotosFetchJobService";
	private PhotosFetchAsyncTask mPhotosFetchAsyncTask;
	private JobParameters mParams;

	@Override
	public boolean onStartJob(JobParameters params) {
		Log.i(TAG, "onStartJob PhotosFetchJobService");
		mParams = params;
		mPhotosFetchAsyncTask = new PhotosFetchAsyncTask(
				this,
				GoogleCredentialHelper.get(getApplicationContext()),
				AppSharedPreferences.getLastPageToken(getApplicationContext()),
				true
		);
		mPhotosFetchAsyncTask.execute();
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		Log.i(TAG, "onStopJob PhotosFetchJobService");
		mPhotosFetchAsyncTask.cancel(true);
		return false;
	}

	public void fetchedPhotos(FileList photos){
		Log.i(TAG, "fetchedPhotos PhotosFetchJobService");
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
		Log.i(TAG, "doneFetching PhotosFetchJobService");
		jobFinished(mParams, false);
	}

	public void onCancel(Exception exception){
		if (exception != null) {
			Log.e(TAG, exception.getMessage());
		}
		jobFinished(null, false);
	}

	public void onStartFetch(){
	}

}
