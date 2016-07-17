package porqueno.muzeigooglephotos;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;

import org.junit.Test;

import porqueno.muzeigooglephotos.models.PhotosModelDbHelper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by jacob on 7/16/16.
 */
public class PhotosModelDbHelperTest {
	@Test
	public void getMetaTimeOrCreatedTimeMeta() throws Exception {
		File file = new File();
		File.ImageMediaMetadata img = new File.ImageMediaMetadata();
		img.setTime("2004:03:29 11:13:48");
		file.setImageMediaMetadata(img);

		// Should ignore createdTime
		int epochTime = 1279317788; // Fri, 16 Jul 2010 22:03:08 GMT;
		DateTime date = new DateTime(epochTime);
		file.setCreatedTime(date);

		DateTime parsedDate = PhotosModelDbHelper.getMetaTimeOrCreatedTime(file);
		assertThat(parsedDate.toString(), is("2004-03-29T11:13:48.000-08:00"));
	}

	@Test
	public void getMetaTimeOrCreatedTimeCreated() throws Exception {
		File file = new File();
		int epochTime = 1279317788; // Fri, 16 Jul 2010 22:03:08 GMT;
		DateTime date = new DateTime(epochTime);
		file.setCreatedTime(date);

		DateTime parsedDate = PhotosModelDbHelper.getMetaTimeOrCreatedTime(file);
		assertThat(date.getValue(), is(parsedDate.getValue()));
	}
}
