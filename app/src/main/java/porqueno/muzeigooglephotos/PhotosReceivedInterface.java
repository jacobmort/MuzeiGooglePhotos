package porqueno.muzeigooglephotos;

import com.google.api.services.drive.model.FileList;

/**
 * Created by jacob on 8/30/16.
 */
public interface PhotosReceivedInterface {
	public void fetchedPhotos(FileList photos);
	public void doneFetching();
	public void onCancel(Exception exception);
	public void onStartFetch();
}
