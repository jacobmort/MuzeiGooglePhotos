package porqueno.muzeigooglephotos;

import com.google.api.client.util.DateTime;

import org.junit.Test;

import porqueno.muzeigooglephotos.models.PhotoModel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PhotoModelTest {
	@Test
	public void getCreatedTime() throws Exception {
		DateTime dt = new DateTime("2016-06-13T04:16:33.000Z");
		PhotoModel photo = new PhotoModel("", dt, false);
		assertThat(photo.getCreatedTime().getValue(), is(dt.getValue()));
	}
}