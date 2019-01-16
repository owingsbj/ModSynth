package com.gallantrealm.modsynth;

import java.util.Timer;
import java.util.TimerTask;
import com.gallantrealm.android.GallantActivity;
import com.gallantrealm.android.Translator;
import com.gallantrealm.android.themes.Theme;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GallantSplashActivity extends GallantActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ClientModel.getClientModel().loadPreferences(this); // to prepare model for use later

		setContentView(R.layout.gallant_splash);
		Typeface typeface = Theme.getTheme().getTypeface(this);
		((TextView) findViewById(R.id.goggleDog)).setTypeface(typeface);
		((TextView) findViewById(R.id.appTagline)).setTypeface(typeface);

		View goggleDog = findViewById(R.id.goggleDog);
		if (goggleDog != null) {
			goggleDog.setClickable(true);
			goggleDog.setOnLongClickListener(new View.OnLongClickListener() {
				public boolean onLongClick(View v) {
					ClientModel.getClientModel().setGoggleDogPass(true);
					return false;
				}
			});
		}

// HeyzapLib.setFlags(1 << 23); // turn off Heyzap notification

		Translator.getTranslator().translate(this.getWindow().getDecorView());
	}

	public void showMainMenu() {
		try {
			Intent intent = new Intent(GallantSplashActivity.this, GallantSplashActivity.this.getClassLoader().loadClass(getString(R.string.mainMenuClassName)));
			intent.setData(getIntent().getData()); // pass along invokation params
			startActivity(intent);
			GallantSplashActivity.this.finish();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	Timer t;

	@Override
	protected void onStart() {
		super.onStart();
		t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				ClientModel.getClientModel().updatePlayCount(GallantSplashActivity.this);
				showMainMenu();
			}
		}, 2000l);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (t != null) {
			t.cancel();
			t = null;
		}
	}

}
