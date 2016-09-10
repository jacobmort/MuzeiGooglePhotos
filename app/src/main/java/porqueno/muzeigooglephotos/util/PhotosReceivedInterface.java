package porqueno.muzeigooglephotos.util;

import com.google.api.services.drive.model.FileList;

/**
 * Created by jacob on 8/30/16.
 */
public interface PhotosReceivedInterface {
	void fetchedPhotos(FileList photos);
	void doneFetching();
	void onCancel(Exception exception);
	void onStartFetch();
}
