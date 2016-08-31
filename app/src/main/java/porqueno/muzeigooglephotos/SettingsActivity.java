package porqueno.muzeigooglephotos;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import porqueno.muzeigooglephotos.databinding.SettingsActivityBinding;

/**
 * Created by jacob on 8/30/16.
 */
public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SettingsActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.settings_activity);
		binding.setHandlers(this);
	}

	public void onClickStartSync(View view) {
		Intent i = new Intent(this, GooglePhotosAuthActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
}
