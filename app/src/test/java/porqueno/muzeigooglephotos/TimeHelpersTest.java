package porqueno.muzeigooglephotos;

import com.google.api.client.util.DateTime;

import org.junit.Test;
import org.threeten.bp.LocalDateTime;

import porqueno.muzeigooglephotos.util.TimeHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by jacob on 7/13/16.
 */
public class TimeHelpersTest {
	@Test
	public void getDateFromTimeMeta() throws Exception {
		String metaTime = "2016:07:10 15:48:03";
		LocalDateTime metaDate = TimeHelpers.getLocalDateFromTimeMeta(metaTime);;
		assertThat(metaDate.toString(), is("2016-07-10T15:48:03"));
	}

	@Test
	public void convertFromZuluDateTime(){
		long epochTime = 1279317788000L; // Fri, 16 Jul 2010 22:03:08 GMT;
		DateTime date = new DateTime(epochTime);
		LocalDateTime dt = TimeHelpers.convertFromDateTimeZulu(date);
		assertThat(dt.toString(), is("2010-07-16T22:03:08"));
	}

	@Test
	public void getPrettyDateString() {
		LocalDateTime dt = LocalDateTime.of(2016, 6, 13, 4, 16, 33);
		assertThat(TimeHelpers.getPrettyDateString(dt), is("Mon 13 Jun, 2016"));
	}

	@Test
	public void getPrettyTimeString() {
		LocalDateTime dt = LocalDateTime.of(2016, 6, 13, 12, 16, 33);
		assertThat(TimeHelpers.getPrettyTimeString(dt), is("12:16 PM"));
	}
}
