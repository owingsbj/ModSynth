package com.gallantrealm.modsynth;

import com.gallantrealm.android.GallantDialog;
import com.gallantrealm.android.MessageDialog;
import com.gallantrealm.android.SelectItemDialog;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.theme.AuraTheme;
import com.gallantrealm.modsynth.theme.BlueTheme;
import com.gallantrealm.modsynth.theme.CircuitTheme;
import com.gallantrealm.modsynth.theme.CustomTheme;
import com.gallantrealm.modsynth.theme.GreenTheme;
import com.gallantrealm.modsynth.theme.IceTheme;
import com.gallantrealm.modsynth.theme.MetalTheme;
import com.gallantrealm.modsynth.theme.OnyxTheme;
import com.gallantrealm.modsynth.theme.SpaceTheme;
import com.gallantrealm.modsynth.theme.SunsetTheme;
import com.gallantrealm.modsynth.theme.TropicalTheme;
import com.gallantrealm.modsynth.theme.WoodTheme;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import de.viktorreiser.toolbox.widget.NumberPicker;

public class SettingsDialog extends GallantDialog {
	ClientModel clientModel = ClientModel.getClientModel();

	Spinner languageSpinner;
	Spinner keysSpinner;
	Spinner midiChannelSpinner;
	TextView titleText;
	Button okButton;
	Button chooseBackgroundButton;
	NumberPicker tuningCents;
	int buttonPressed = -1;
	String title;
	String message;
	String initialValue;
	String[] options;
	MainActivity activity;

	public SettingsDialog(MainActivity context) {
		super(context, R.style.Theme_Dialog);
		activity = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.title = "Settings";
		this.message = "test";
		this.initialValue = "";
		this.options = null;
		setContentView(R.layout.settings_dialog);
		setCancelable(true);
		setCanceledOnTouchOutside(true);
	}

	boolean sampleRateFirstSet = true;
	boolean buffersFirstSet = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		titleText = (TextView) findViewById(R.id.titleText);
		languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
		keysSpinner = (Spinner) findViewById(R.id.keysSpinner);
		midiChannelSpinner = (Spinner) findViewById(R.id.midiChannelSpinner);
		final Spinner sampleRateSpinner = (Spinner) findViewById(R.id.sampleRateSpinner);
		final Spinner buffersSpinner = (Spinner) findViewById(R.id.buffersSpinner);
		okButton = (Button) findViewById(R.id.okButton);
		chooseBackgroundButton = (Button) findViewById(R.id.chooseBackgroundButton);

		Typeface typeface = clientModel.getTypeface(getContext());
		if (typeface != null) {
			titleText.setTypeface(typeface);
			okButton.setTypeface(typeface);
			chooseBackgroundButton.setTypeface(typeface);
		}

		ArrayAdapter<CharSequence> languageAdapter = new ArrayAdapter(activity, R.layout.spinner_item, new String[] { "Default", "English", "français", "Deutsche", "Español", "русский" });
		languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSpinner.setAdapter(languageAdapter);
		languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				int languageSelection = languageSpinner.getSelectedItemPosition();
				if (languageSelection != clientModel.getLanguage()) {
					clientModel.setLanguage(languageSelection);
					clientModel.savePreferences();
					activity.setLanguage(languageSelection);
					new MessageDialog(SettingsDialog.this.getContext(), "Language", "Quit and relaunch ModSynth to apply the change.", null).show();
				}
			}

			@Override
			public void onNothingSelected(AdapterView av) {
			}
		});
		int languageSelection = clientModel.getLanguage();
		languageSpinner.setSelection(languageSelection);

		ArrayAdapter<CharSequence> keysAdapter = new ArrayAdapter(activity, R.layout.spinner_item, new String[] { "13", "20", "25", "32" });
		keysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		keysSpinner.setAdapter(keysAdapter);
		keysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				int keysSelection = keysSpinner.getSelectedItemPosition();
				activity.setKeyboardSize(keysSelection);
				clientModel.setKeyboardSize(keysSelection);
				clientModel.savePreferences();
			}

			@Override
			public void onNothingSelected(AdapterView av) {
			}
		});
		int keySelection = clientModel.getKeyboardSize();
		keysSpinner.setSelection(keySelection);

		ArrayAdapter<CharSequence> midiChannelAdapter = Translator.getArrayAdapter(activity, R.layout.spinner_item, new String[] { "Any", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" });
		midiChannelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		midiChannelSpinner.setAdapter(midiChannelAdapter);
		midiChannelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				int midiChannel = midiChannelSpinner.getSelectedItemPosition();
				activity.setMidiChannel(midiChannel);
				clientModel.setMidiChannel(midiChannel);
				clientModel.savePreferences();
			}

			@Override
			public void onNothingSelected(AdapterView av) {
			}
		});
		midiChannelSpinner.setSelection(clientModel.getMidiChannel());

		chooseBackgroundButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				chooseBackground();
			}
		});

		okButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonPressed = 0;
				SettingsDialog.this.dismiss();
				SettingsDialog.this.cancel();
				return true;
			}

		});

		String[] sampleRates = new String[3];
		sampleRates[0] = String.valueOf(AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC));
		sampleRates[1] = String.valueOf(AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC) / 2);
		sampleRates[2] = String.valueOf(AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC) / 4);
		ArrayAdapter<CharSequence> sampleRateAdapter = new ArrayAdapter(activity, R.layout.spinner_item, sampleRates);
		sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sampleRateSpinner.setAdapter(sampleRateAdapter);
		sampleRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				int sampleRateReducer = sampleRateSpinner.getSelectedItemPosition();
				clientModel.setSampleRateReducer(sampleRateReducer);
				clientModel.savePreferences();
				if (!sampleRateFirstSet) {
					new MessageDialog(SettingsDialog.this.getContext(), "Sample Rate", "Quit and relaunch ModSynth to apply the change.", null).show();
				}
				sampleRateFirstSet = false;
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		sampleRateSpinner.setSelection(clientModel.getSampleRateReducer());

		String[] buffers = new String[4];
		buffers[0] = "1";
		buffers[1] = "2";
		buffers[2] = "5";
		buffers[3] = "10";
		ArrayAdapter<CharSequence> buffersAdapter = new ArrayAdapter(activity, R.layout.spinner_item, buffers);
		buffersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		buffersSpinner.setAdapter(buffersAdapter);
		buffersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				int b = buffersSpinner.getSelectedItemPosition();
				int nbuffers = 2;
				if (b == 0) {
					nbuffers = 1;
				} else if (b == 1) {
					nbuffers = 2;
				} else if (b == 2) {
					nbuffers = 5;
				} else if (b == 3) {
					nbuffers = 10;
				}
				clientModel.setNBuffers(nbuffers);
				clientModel.savePreferences();
				if (!buffersFirstSet) {
					new MessageDialog(SettingsDialog.this.getContext(), "Buffers", "Quit and relaunch ModSynth to apply the change in buffers.", null).show();
				}
				buffersFirstSet = false;
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		int nbuffers = clientModel.getNBuffers();
		int b = 1;
		if (nbuffers == 1) {
			b = 0;
		} else if (nbuffers == 2) {
			b = 1;
		} else if (nbuffers == 5) {
			b = 2;
		} else if (nbuffers == 10) {
			b = 3;
		}
		buffersSpinner.setSelection(b);

		tuningCents = (NumberPicker) findViewById(R.id.tuningCents);
		tuningCents.setCurrent(clientModel.getTuningCents());
		tuningCents.setOnChangeListener(new NumberPicker.OnChangedListener() {
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				activity.setTuningCents(tuningCents.getCurrent());
				clientModel.setTuningCents(tuningCents.getCurrent());
				clientModel.savePreferences();
			}
		});

		final Spinner controlsSpinner = (Spinner) findViewById(R.id.controlsSpinner);
		ArrayAdapter<CharSequence> controlsAdapter = Translator.getArrayAdapter(activity, R.layout.spinner_item, new String[] { "Right", "Left" });
		controlsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		controlsSpinner.setAdapter(controlsAdapter);
		controlsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				int controlsSelection = controlsSpinner.getSelectedItemPosition();
				activity.setControlSide(controlsSelection);
				clientModel.setControlSide(controlsSelection);
				clientModel.savePreferences();
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		int controlsSelection = clientModel.getControlSide();
		controlsSpinner.setSelection(controlsSelection);

		final CheckBox colorIconsCheckBox = (CheckBox) findViewById(R.id.colorIcons);
		colorIconsCheckBox.setChecked(clientModel.getColorIcons() > 0);
		colorIconsCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					clientModel.setColorIcons(1);
				} else {
					clientModel.setColorIcons(0);
				}
				activity.setColorIcons(isChecked);
				clientModel.savePreferences();
			}
		});

		Translator.getTranslator().translate(this.getWindow().getDecorView());
	}

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	public void chooseBackground() {
		Class[] themes;
		if (Build.VERSION.SDK_INT >= 16) { // needed for custom background
			themes = new Class[] { //
					BlueTheme.class, GreenTheme.class, //
					OnyxTheme.class, MetalTheme.class, WoodTheme.class, //
					AuraTheme.class, IceTheme.class, CircuitTheme.class, //
					SpaceTheme.class, SunsetTheme.class, TropicalTheme.class, //
					CustomTheme.class //
			};
		} else {
			themes = new Class[] { //
					BlueTheme.class, GreenTheme.class, //
					OnyxTheme.class, MetalTheme.class, WoodTheme.class, //
					AuraTheme.class, IceTheme.class, CircuitTheme.class, //
					SpaceTheme.class, SunsetTheme.class, TropicalTheme.class //
			};
		}
		final SelectItemDialog selectItemDialog = new SelectItemDialog(activity, "Choose a background", themes, null);
		selectItemDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (selectItemDialog.getItemSelected() != null) {
					String option = selectItemDialog.getItemSelected().getName();
					clientModel.setBackgroundName(option);
					clientModel.savePreferences();
					activity.setTheme(option, null);
				}
			}
		});
		selectItemDialog.show();
	}

}
