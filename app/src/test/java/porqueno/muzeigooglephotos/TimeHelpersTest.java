package porqueno.muzeigooglephotos;

import com.google.api.client.util.DateTime;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import porqueno.muzeigooglephotos.util.TimeHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * Created by jacob on 7/13/16.
 */
public class TimeHelpersTest {
	@Test
	public void getDateFromTimeMeta() throws Exception {
		//TODO make this not dependent on PDT/PST
		String metaTime = "2016:07:10 15:48:03";
		Date metaDate = TimeHelpers.getDateFromTimeMeta(metaTime);
		assertThat(metaDate.toString(), is("Sun Jul 10 15:48:03 PDT 2016"));
	}

	@Test
	public void getPrettyDateString() {
		DateTime dt = new DateTime("2016-06-13T04:16:33.000Z");
		// PST offset
		Assert.assertThat(TimeHelpers.getPrettyDateString(dt), is("Sun 12 Jun, 2016"));
	}

	@Test
	public void getPrettyTimeString() {
		DateTime dt = new DateTime("2016-06-13T04:16:33.000Z");
		// PST offset
		Assert.assertThat(TimeHelpers.getPrettyTimeString(dt), is("9:16 PM"));
	}
}
