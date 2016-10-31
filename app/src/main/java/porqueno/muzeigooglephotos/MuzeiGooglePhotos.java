package porqueno.muzeigooglephotos;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

/**
 * Created by jacob on 10/21/16.
 */

public class MuzeiGooglePhotos extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		AndroidThreeTen.init(this);
	}
}
