package porqueno.muzeigooglephotos;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;

import porqueno.muzeigooglephotos.models.PhotoModel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PhotoModelTest {
	@Test
	public void getCreatedTime() throws Exception {
		LocalDateTime dt = LocalDateTime.of(2016,6,13,4,16,33);
		PhotoModel photo = new PhotoModel("", dt, 0, 0, false);
		assertThat(
				photo.getCreatedTime().toString(),
				is(dt.toString())
		);
	}
}