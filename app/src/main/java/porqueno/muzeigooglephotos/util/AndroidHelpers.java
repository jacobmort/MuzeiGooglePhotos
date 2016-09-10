package porqueno.muzeigooglephotos.util;

import android.os.Build;

/**
 * Created by jacob on 9/9/16.
 */
public class AndroidHelpers {

	public static boolean supportsJobScheduler() {
		return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}
}
