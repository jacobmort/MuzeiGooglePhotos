package porqueno.muzeigooglephotos;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

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
		long epochTime = 1279317788000L; // Fri, 16 Jul 2010 22:03:08 GMT;
		DateTime date = new DateTime(epochTime);
		file.setCreatedTime(date);

		LocalDateTime parsedDate = PhotosModelDbHelper.getMetaTimeOrCreatedTime(file);
		assertThat(parsedDate.toString(), is("2004-03-29T11:13:48"));
	}

	@Test
	public void getMetaTimeOrCreatedTimeCreated() throws Exception {
		File file = new File();
		long epochTime = 1279317788000L; // Fri, 16 Jul 2010 22:03:08 GMT;
		DateTime date = new DateTime(epochTime);
		file.setCreatedTime(date);

		LocalDateTime parsedDate = PhotosModelDbHelper.getMetaTimeOrCreatedTime(file);
		assertThat(date.getValue(), is(parsedDate.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000));
	}
}
