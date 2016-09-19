package porqueno.muzeigooglephotos.services;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import porqueno.muzeigooglephotos.models.AppSharedPreferences;
import porqueno.muzeigooglephotos.models.PhotosModelDbHelper;
import porqueno.muzeigooglephotos.util.GoogleCredentialHelpers;
import porqueno.muzeigooglephotos.util.PhotosFetchAsyncTask;
import porqueno.muzeigooglephotos.util.PhotosReceivedInterface;

/**
 * Created by jacob on 8/29/16.
 */

@TargetApi(21)
public class PhotosFetchJobService extends JobService implements PhotosReceivedInterface {
	private static final long HOW_FREQ_TO_RUN_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
	private static final int JOB_ID = 2001;
	private static final String TAG = "PhotosFetchJobService";
	private PhotosFetchAsyncTask mPhotosFetchAsyncTask;
	private JobParameters mParams;

	@Override
	public boolean onStartJob(JobParameters params) {
		Log.i(TAG, "onStartJob PhotosFetchJobService");
		mParams = params;
		mPhotosFetchAsyncTask = new PhotosFetchAsyncTask(
				this,
				GoogleCredentialHelpers.get(getApplicationContext()),
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
		jobFinished(mParams, false);
	}

	public void onStartFetch(){
	}

	public static void scheduleJob(Context ctx, JobScheduler scheduler){
		JobInfo jobInfo = new JobInfo.Builder(PhotosFetchJobService.JOB_ID, new ComponentName(ctx, PhotosFetchJobService.class))
				.setRequiresCharging(true)
				.setPersisted(true)
				.setPeriodic(PhotosFetchJobService.HOW_FREQ_TO_RUN_MS)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
				.build();
		scheduler.schedule(jobInfo);
	}

}
