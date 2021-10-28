package com.gallantrealm.modsynth;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import com.gallantrealm.android.ContentUriUtil;
import com.gallantrealm.android.FileSelectorDialog;
import com.gallantrealm.android.InputDialog;
import com.gallantrealm.android.KeyboardControl;
import com.gallantrealm.android.MessageDialog;
import com.gallantrealm.android.Scope;
import com.gallantrealm.android.SelectItemDialog;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.module.Amp;
import com.gallantrealm.modsynth.module.Arpeggiator;
import com.gallantrealm.modsynth.module.Compressor;
import com.gallantrealm.modsynth.module.Crusher;
import com.gallantrealm.modsynth.module.Delay;
import com.gallantrealm.modsynth.module.Divider;
import com.gallantrealm.modsynth.module.Envelope;
import com.gallantrealm.modsynth.module.Filter;
import com.gallantrealm.modsynth.module.Function;
import com.gallantrealm.modsynth.module.Glide;
import com.gallantrealm.modsynth.module.Keyboard;
import com.gallantrealm.modsynth.module.LFO;
import com.gallantrealm.modsynth.module.Melody;
import com.gallantrealm.modsynth.module.Mixer;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.MultiOsc;
import com.gallantrealm.modsynth.module.Operator;
import com.gallantrealm.modsynth.module.Oscillator;
import com.gallantrealm.modsynth.module.Output;
import com.gallantrealm.modsynth.module.PCM;
import com.gallantrealm.modsynth.module.Pad;
import com.gallantrealm.modsynth.module.Pan;
import com.gallantrealm.modsynth.module.Reverb;
import com.gallantrealm.modsynth.module.SampleHold;
import com.gallantrealm.modsynth.module.Sequencer;
import com.gallantrealm.modsynth.module.SpectralFilter;
import com.gallantrealm.modsynth.module.Unison;
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
import com.gallantrealm.modsynth.viewer.MelodyViewer;
import com.gallantrealm.modsynth.viewer.ModuleViewer;
import com.gallantrealm.modsynth.viewer.OutputViewer;
import com.gallantrealm.modsynth.viewer.PCMViewer;
import com.gallantrealm.mysynth.MySynth;
import com.gallantrealm.mysynth.MySynthMidi;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener, ClientModelChangedListener {

	public ClientModel clientModel = ClientModel.getClientModel();

	View mainLayout;
	public View operatorPane;
	public View instrumentPane;
	public View recordPane;
	public View modGraphPane;
	public ModGraph modGraph;

	public KeyboardControl keyboardPane;

	Button settingsButton;

	Button soundSelector;
	Button saveButton;
	Button recordButton;
	Button deleteButton;
	Button fullVersionButton;

	TextView recordTime;
	Button startRecordingButton;
	Button stopRecordingButton;
	Button replayRecordingButton;
	Button saveRecordingButton;

	Button editGraphButton;
	Button addModuleButton;
	Button deleteModuleButton;

	ViewGroup modViewGroup;
	TextView noModSelectedText;

	MySynth synth;
	MySynthMidi midi;

	Scope scope; // set when scope is showing

	PowerManager.WakeLock wakelock;

	String builtinSeparatorText;
	String customSeparatorText;

	ArrayAdapter<CharSequence> soundAdapter;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println(">>MainActivity.onCreate");
		super.onCreate(savedInstanceState);

		if(BuildConfig.DEBUG)
			StrictMode.enableDefaults();

		clientModel.setContext(this);

		// Restore the preferences. If this is the first time, set some of the
		// preferences to good defaults
		clientModel.loadPreferences();
		if (clientModel.getPlayCount() <= 1) {
			clientModel.setKeyboardSize(2); // keyboard size to 2 octaves
			clientModel.savePreferences();
		}

		// Set up the translator with the right language (before rendering view)
		setLanguage(clientModel.getLanguage());

		setContentView(R.layout.synth_screen);

		// Set sustained performance mode (Android 7.0+)
		if (Build.VERSION.SDK_INT >= 24) {
			getWindow().setSustainedPerformanceMode(true);
		}

		// Create the synthesizer
		int sampleRateReducer = Math.max(0, clientModel.getSampleRateReducer());
		int nbuffers = clientModel.getNBuffers();
		synth = MySynth.create(this, sampleRateReducer, nbuffers);
		synth.setCallbacks(new MySynth.Callbacks() {
			int t;
			public void onUpdateScope(float left, float right) {
				if (scope != null) {
					scope.scope((left + right) / 2.0f);
				}
				t += 1;
				if (modGraph != null && t >= 1000) {
					for (Module module : ((Instrument)synth.getInstrument()).modules) {
						module.lastmin = module.min;
						module.lastmax = module.max;
						module.min = 0.0;
						module.max = 0.0;
					}
					modGraph.updateLevels();
					t = 0;
				}
			}
		});

		// Start up the MIDI support
		midi = MySynthMidi.create(this, synth, new MySynthMidi.Callbacks() {
			public void onDeviceAttached(final String deviceName) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// Note: keeping keyboard visible as it seems some devices have midi devices attached..
						// ..they are probably other apps that support MIDI.
						//keyboardPane.setVisibility(View.GONE);
						Toast.makeText(MainActivity.this, deviceName + " attached", Toast.LENGTH_LONG).show();
					}
				});
			}
			public void onDeviceDetached(final String deviceName) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						keyboardPane.setVisibility(View.VISIBLE);
						Toast.makeText(MainActivity.this, deviceName + " detached", Toast.LENGTH_LONG).show();
					}
				});
			}
			public void onControlChange(int control, int value) {
				MidiControlDialog.controlChanged(MainActivity.this, control);
				if (synth.getInstrument() != null) {
					synth.getInstrument().controlChange(control, value / 127.0f);
				}
			}
			public void onProgramChange(final int programNum) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String soundName = clientModel.findObject("" + programNum + " ", ".modsynth");
						if (soundName == null) {
							Toast.makeText(MainActivity.this,
									"No instrument found with name starting with \"" + programNum + " \" ",
									Toast.LENGTH_LONG).show();
						} else {
							loadInstrument(soundName);
							ClientModel.getClientModel().setInstrumentName(soundName);
							ClientModel.getClientModel().savePreferences();
						}
					}
				});
			}
			public void onTimingClock() {
				if (synth.getInstrument() != null) {
					synth.getInstrument().midiclock();
				}
			}
			public void onSysex(byte[] data) {
				// TODO add support for load/save of instruments via sysex
			}
		});
		if (clientModel.isGoggleDogPass()) {
			midi.setLogMidi(true);
		}
		Settings.tuningCents = clientModel.getTuningCents();

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mainLayout = findViewById(R.id.mainLayout);
		operatorPane = findViewById(R.id.operatorPane);
		instrumentPane = findViewById(R.id.instumentPane);
		recordPane = findViewById(R.id.recordPane);

		keyboardPane = findViewById(R.id.keyboardPane);

		settingsButton = (Button) operatorPane.findViewById(R.id.settingsButton);

		soundSelector = (Button) instrumentPane.findViewById(R.id.soundSelector);
		saveButton = (Button) instrumentPane.findViewById(R.id.saveButton);
		deleteButton = (Button) instrumentPane.findViewById(R.id.deleteButton);

		recordButton = (Button) operatorPane.findViewById(R.id.recordButton);
		if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass()) {
			recordButton.setVisibility(View.GONE);
		}
		fullVersionButton = (Button) operatorPane.findViewById(R.id.fullVersionButton);

		recordTime = (TextView) recordPane.findViewById(R.id.recordTime);
		startRecordingButton = (Button) recordPane.findViewById(R.id.recordStartButton);
		stopRecordingButton = (Button) recordPane.findViewById(R.id.recordStopButton);
		replayRecordingButton = (Button) recordPane.findViewById(R.id.recordReplayButton);
		saveRecordingButton = (Button) recordPane.findViewById(R.id.recordSaveButton);

		modGraphPane = findViewById(R.id.modGraphPane);
		modGraph = (ModGraph) findViewById(R.id.modGraph);
		editGraphButton = (Button) findViewById(R.id.editGraphButton);
		addModuleButton = (Button) findViewById(R.id.addModuleButton);
		deleteModuleButton = (Button) findViewById(R.id.deleteModuleButton);

		modViewGroup = (ViewGroup) findViewById(R.id.modViewGroup);
		noModSelectedText = (TextView) findViewById(R.id.noModSelectedText);

		// personalize
		setTheme(clientModel.getBackgroundName(), clientModel.getCustomBackgroundPath());
		Typeface tf = clientModel.getTypeface(this);
		((TextView) instrumentPane.findViewById(R.id.instrumentLabel)).setTypeface(tf);
		soundSelector.setTypeface(tf);
		saveButton.setTypeface(tf);
		deleteButton.setTypeface(tf);
		fullVersionButton.setTypeface(tf);
		editGraphButton.setTypeface(tf);
		addModuleButton.setTypeface(tf);
		deleteModuleButton.setTypeface(tf);
		recordButton.setTypeface(tf);
		startRecordingButton.setTypeface(tf);
		stopRecordingButton.setTypeface(tf);
		replayRecordingButton.setTypeface(tf);
		saveRecordingButton.setTypeface(tf);

		settingsButton.setOnClickListener(this);

		// setDefaultSound();

		// select the sound from last session, or the first sound
		String soundName = clientModel.getInstrumentName();
		if (soundName == null || soundName.startsWith("com.gallantrealm")) { // it isn't a sound
			soundName = "BuiltIn/Basic";
		}
		loadInstrument(soundName);

		keyboardPane.setKeyboardSize(clientModel.getKeyboardSize());

		setMidiChannel(clientModel.getMidiChannel());

		// synth.tuning = clientModel.getScoreThree();

		setControlSide(clientModel.getControlSide());

		soundSelector.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
		recordButton.setOnClickListener(this);
		startRecordingButton.setOnClickListener(this);
		stopRecordingButton.setOnClickListener(this);
		replayRecordingButton.setOnClickListener(this);
		saveRecordingButton.setOnClickListener(this);
		addModuleButton.setOnClickListener(this);
		deleteModuleButton.setOnClickListener(this);

		if (clientModel.isFullVersion() || clientModel.isGoggleDogPass() || clientModel.isFree()) {
			fullVersionButton.setVisibility(View.GONE);
		} else {
			fullVersionButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					clientModel.setContext(MainActivity.this);
					final MessageDialog mdialog = new MessageDialog(MainActivity.this, "Full Version",
							"Add FM Operators, spectral filtering, PCM synthesis, harmonic editing and more with ModSynth full version!",
							new String[] { "Buy", "Later" });
					mdialog.show();
					mdialog.setOnDismissListener(new OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if (mdialog.getButtonPressed() == 0) {
								clientModel.buyFullVersion(MainActivity.this);
							}
						}
					});
				}
			});
		}

		// set up the modgraph selection handler
		modGraph.setOnSelectionListener(new ModGraph.OnSelectionListener() {
			public void selected(Module module) {
				selectModule(module);
			}
		});

		editGraphButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!modGraph.isEditing()) {
					if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass()
							&& ((Instrument) synth.getInstrument()) != null
							&& ((Instrument) synth.getInstrument()).hasAdvancedModules()) {
						MessageDialog dialog = new MessageDialog(MainActivity.this, "Full Version",
								"Full version is needed to edit this instrument.", new String[] { "OK" });
						dialog.show();
						return;
					}
					synth.pause();
					keyboardPane.setVisibility(View.GONE);
					modViewGroup.setVisibility(View.GONE);
					operatorPane.setVisibility(View.GONE);
					modGraph.setZoom(modGraph.getZoom() * 1.5f);
					modGraph.setEditing(true);
					// Animation graphAnimation = new ScaleAnimation(1.0f, 1.0f,
					// 2.0f, 2.0f);
					// graphAnimation.setDuration(2000);
					// modGraph.startAnimation(graphAnimation);
					editGraphButton.setText(Translator.getTranslator().translate("Done"));
					addModuleButton.setVisibility(View.VISIBLE);
					deleteModuleButton.setVisibility(View.VISIBLE);
				} else {
					if (!midi.isMidiDeviceAttached()) {
						keyboardPane.setVisibility(View.VISIBLE);
					}
					// keyboardPane.startAnimation(new ScaleAnimation(1.0f,
					// 1.0f, 0.0f, 1.0f));
					modViewGroup.setVisibility(View.VISIBLE);
					operatorPane.setVisibility(View.VISIBLE);
					modGraph.setZoom(modGraph.getZoom() / 1.5f);
					editGraphButton.setText(Translator.getTranslator().translate("Edit"));
					addModuleButton.setVisibility(View.GONE);
					deleteModuleButton.setVisibility(View.GONE);
					updateControls();
					try {
						modGraph.setEditing(false);
						synth.setInstrument(modGraph.getInstrument());
						try {
							synth.resume();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
						MessageDialog message = new MessageDialog(MainActivity.this, null,
								"Not enough memory to load this instrument.  Close other apps and try again.",
								new String[] { "OK" });
						message.show();
					}
				}
			}
		});
		addModuleButton.setVisibility(View.GONE);
		deleteModuleButton.setVisibility(View.GONE);

		modViewGroup.setClickable(true);
		modViewGroup.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		keyboardPane.setListener(new KeyboardControl.Listener() {
			public void onNotePressed(int note, float velocity) {
				if (synth.getInstrument() != null) {
					synth.getInstrument().notePress(note, velocity);
				}
			}
			public void onNoteReleased(int note) {
				if (synth.getInstrument() != null) {
					synth.getInstrument().noteRelease(note);
				}
			}
			public void onNoteAftertouch(int note, float pressure) {
				// synth.pressure(note, pressure);
			}
		});

		clientModel.addClientModelChangedListener(this);

		modGraph.setColorIcons(clientModel.getColorIcons() > 0);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");

		System.out.println("<<MainActivity.onCreate");
	}

	public void setColorIcons(boolean colorIcons) {
		modGraph.setColorIcons(colorIcons);
	}

	private View lastSelectedModView;

	@Override
	protected void onDestroy() {
		System.out.println(">>MainActivity.onDestroy");
		midi.terminate();
		midi = null;
		synth.stop();
		synth.terminate();
		synth = null;
		super.onDestroy();
		System.out.println("<<MainActivity.onDestroy");
	}

	boolean selectingCustomTheme;

	/**
	 * Needed for  file chooser and other actions that launch separate activities.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (selectingCustomTheme) {
			selectingCustomTheme = false;
			if (data != null) {
				try {
					String path = "";
					Uri mImageCaptureUri = data.getData();
					System.out.println("Selected Uri is " + mImageCaptureUri);
					path = ContentUriUtil.getPath(this, mImageCaptureUri);
					System.out.println("Image selected: " + path);
					ClientModel.getClientModel().setCustomBackgroundPath(path);
					ClientModel.getClientModel().savePreferences();
					setCustomBackground(path);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, "Custom background could not be opened.  Try a different image",
							Toast.LENGTH_LONG).show();
				}
			}
		} else if (data != null && synth != null && ((Instrument) synth.getInstrument()) != null
				&& ((Instrument) synth.getInstrument()).selectedModule != null) {
			((ModuleViewer) ((Instrument) synth.getInstrument()).selectedModule
					.getViewer((Instrument) synth.getInstrument())).onActivityResult(requestCode, resultCode, data);
			return;
		}

		// not handled, so let default handle
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressLint("NewApi")
	private void setCustomBackground(String path) {
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(
				Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
					REQUEST_PERMISSION_READ_IMAGE_EXTERNAL_STORAGE);
			selectingCustomTheme = true;
			return;
		}
		if (path == null || path.length() == 0) {
			return;
		}
		System.out.println("Loading custom background " + path);
		try {
			File file = new File(path);
			InputStream inStream = new BufferedInputStream(new FileInputStream(file), 65536);
			Bitmap tbitmap = BitmapFactory.decodeStream(inStream);
			inStream.close();
			Bitmap bitmap = tbitmap.copy(tbitmap.getConfig(), true);
			tbitmap.recycle();
			mainLayout.setBackground(new BitmapDrawable(bitmap));
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, "Custom background could not be loaded.", Toast.LENGTH_SHORT).show();
		}
	}

	boolean stopUpdateThread;
	UpdateThread updateThread;

	class UpdateThread extends Thread {

		boolean stopThread;;

		public void safeStop() {
			stopThread = true;
		}

		@Override
		public void run() {
			try {
				while (!stopThread) {
					if (synth != null) {
						final int time = synth.getRecordTime();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								int mins = time / 60;
								int secs = time % 60;
								if (secs < 10) {
									recordTime.setText("" + mins + ":0" + secs);
								} else {
									recordTime.setText("" + mins + ":" + secs);
								}
							}
						});
						Thread.sleep(250);
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == soundSelector) {
			selectInstrument();
		} else if (v == saveButton) {
			saveInstrument();
		} else if (v == deleteButton) {
			deleteInstrument();
		} else if (v == recordButton) {
			if (recordPane.getVisibility() == View.GONE) {
				record();
			} else {
				doneRecording();
			}
		} else if (v == startRecordingButton) {
			startRecording();
		} else if (v == stopRecordingButton) {
			stopRecording();
		} else if (v == replayRecordingButton) {
			playbackRecording();
		} else if (v == saveRecordingButton) {
			saveRecording();
		} else if (v == addModuleButton) {
			if (modGraph.isEditing()) {
				Class[] modClasses;
				if (clientModel.isFullVersion() || clientModel.isGoggleDogPass()) {
					if (modGraph.getInstrument().getKeyboardModule() == null) {
						modClasses = new Class[] { //
								Amp.class, //
								Arpeggiator.class, //
								Compressor.class, //
								Crusher.class, //
								Delay.class, //
								Divider.class, //
								Envelope.class, //
								Filter.class, //
								Function.class, //
								Glide.class, //
								Keyboard.class, //
								LFO.class, //
								Melody.class, //
								Mixer.class, //
								Operator.class, //
								Oscillator.class, //
								MultiOsc.class, //
								Pad.class, //
								Pan.class, //
								PCM.class, //
								Reverb.class, //
								SampleHold.class, //
								Sequencer.class, //
								SpectralFilter.class, //
								Unison.class //
						};
					} else {
						modClasses = new Class[] { //
								Amp.class, //
								Arpeggiator.class, //
								Compressor.class, //
								Crusher.class, //
								Delay.class, //
								Divider.class, //
								Envelope.class, //
								Filter.class, //
								Function.class, //
								Glide.class, //
								LFO.class, //
								Melody.class, //
								Mixer.class, //
								Operator.class, //
								Oscillator.class, //
								MultiOsc.class, //
								Pad.class, //
								Pan.class, //
								PCM.class, //
								Reverb.class, //
								SampleHold.class, //
								Sequencer.class, //
								SpectralFilter.class, //
								Unison.class //
						};
					}
				} else {
					if (modGraph.getInstrument().getKeyboardModule() == null) {
						modClasses = new Class[] { //
								Amp.class, //
								Delay.class, //
								Envelope.class, //
								Filter.class, //
								Keyboard.class, //
								LFO.class, //
								Mixer.class, //
								Oscillator.class, //
								Pad.class, //
								Sequencer.class //
						};
					} else {
						modClasses = new Class[] { //
								Amp.class, //
								Delay.class, //
								Envelope.class, //
								Filter.class, //
								LFO.class, //
								Mixer.class, //
								Oscillator.class, //
								Pad.class, //
								Sequencer.class //
						};
					}
				}
				final SelectItemDialog pickModuleDialog = new SelectItemDialog(this, "Select module", modClasses, null);
				pickModuleDialog.show();
				pickModuleDialog.setOnDismissListener(new OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						Class moduleClass = pickModuleDialog.getItemSelected();
						try {
							int a = 0;
							Module module = (Module) moduleClass.newInstance();
							modGraph.addModule(module);
						} catch (Exception e) {
						}
					}
				});
			}
		} else if (v == deleteModuleButton) {
			if (modGraph.isEditing()) {
				if (((Instrument) synth.getInstrument()).selectedModule == null) {
					new MessageDialog(this, "Delete", "No module selected.", null).show();
					return;
				}
				if (((Instrument) synth.getInstrument()).selectedModule instanceof Output) {
					new MessageDialog(this, "Delete", "You cannot delete the Output module.", null).show();
				} else {
					final MessageDialog promptForDelete = new MessageDialog(this, "Delete", "Delete module?",
							new String[] { "OK", "Cancel" });
					promptForDelete.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (promptForDelete.getButtonPressed() == 0) {
								modGraph.deleteModule(((Instrument) synth.getInstrument()).selectedModule);
							}
						}
					});
					promptForDelete.show();
				}
			}
		} else if (v == settingsButton) {
			final SettingsDialog settingsDialog = new SettingsDialog(this);
			settingsDialog.show();
		}
	}

	public void setKeyboardSize(int keysSelection) {
		keyboardPane.setKeyboardSize(keysSelection);
	}

	public void setLanguage(int language) {
		System.out.println("Setting language to " + language);
		Translator translator = new ModSynthTranslator();
		translator.setLanguage(language);
		Translator.setTranslator(translator); // Note: need to do this to rebuild the map inside.. fix someday if needed
		Translator.getTranslator().translate(this.getWindow().getDecorView());
//		this.getWindow().getDecorView().postInvalidate();
		if (modGraph != null) {
			modGraph.invalidate();
		}
	}

	public void setControlSide(int side) {
		if (side == 0) {
			modGraphPane.getParent().bringChildToFront(modViewGroup);
		} else {
			modGraphPane.getParent().bringChildToFront(modGraphPane);
		}
		modGraphPane.getParent().requestLayout();
	}

	public void setMidiChannel(int midiChannel) {
		midi.setMidiChannel(midiChannel);
	}

	public void setTuningCents(int cents) {
		Settings.tuningCents = cents;
	}

	public void saveInstrument() {
		if (synth == null || ((Instrument) synth.getInstrument()) == null) {
			return;
		}
		if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass()
				&& ((Instrument) synth.getInstrument()).hasAdvancedModules()) {
			MessageDialog dialog = new MessageDialog(MainActivity.this, "Full Version",
					"Full version is needed to save this instrument.", new String[] { "OK" });
			dialog.show();
			return;
		}
		String initialSoundName = soundSelector.getText().toString();
		if (initialSoundName.contains("/")) {
			initialSoundName = initialSoundName.substring(initialSoundName.lastIndexOf("/") + 1).trim();
		}
		if (initialSoundName == null) {
			initialSoundName = "Instrument";
		}
		if (initialSoundName.startsWith("file://")) {
			initialSoundName = initialSoundName.substring(initialSoundName.lastIndexOf("/") + 1);
			initialSoundName = initialSoundName.substring(0, initialSoundName.length() - ".modsynth".length());
		}
		final String finalInitialSoundName = initialSoundName;
		final InputDialog promptForName;
		if (clientModel.isGoggleDogPass()) {
			promptForName = new InputDialog(this, "Save", "Save instrument as ", finalInitialSoundName.trim(),
					new String[] { "Save", "Share", "Cancel" });
		} else {
			promptForName = new InputDialog(this, "Save", "Save instrument as ", finalInitialSoundName.trim(),
					new String[] { "Save", "Cancel" });
		}
		promptForName.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				System.out.println("MainActivity.saveInstrument: button pressed = " + promptForName.getButtonPressed());
				final String soundName = promptForName.getValue();
				if (promptForName.getButtonPressed() == 0) {
					clientModel.setContext(MainActivity.this);
					clientModel.saveObject(((Instrument) synth.getInstrument()), soundName + ".modsynth", true);
					if (synth != null && ((Instrument) synth.getInstrument()) != null) {
						((Instrument) synth.getInstrument()).clearDirty();
					}
					System.out.println("Saved sound as " + soundName + ".modsynth");

					// Uncomment to save for update
					// clientModel.exportObject(sound, soundName + ".modsynth");

					soundSelector.setText(soundName);
					ClientModel.getClientModel().setInstrumentName(soundName);
					ClientModel.getClientModel().savePreferences();
				}
				if (clientModel.isGoggleDogPass() && promptForName.getButtonPressed() == 1) {
					final MessageDialog verifyUpload = new MessageDialog(MainActivity.this, "Share",
							"This will upload your instrument to gallantrealm.com for sharing online.",
							new String[] { "OK" });
					verifyUpload.show();
					verifyUpload.setOnDismissListener(new OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if (verifyUpload.getButtonPressed() == 0) {
								Thread shareThread = new Thread() {
									public void run() {
										try {
											FTPClient ftpClient = new FTPClient();
											ftpClient.connect("gallantrealm.com");
											ftpClient.enterLocalPassiveMode();
											ftpClient.login("modsynth@gallantrealm.com", "A23R3&8C");
											ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
											ftpClient.setKeepAlive(true);
											ftpClient.changeWorkingDirectory("/ModSynth");
											String fileName = soundName + "("
													+ Secure.getString(getApplicationContext().getContentResolver(),
															Secure.ANDROID_ID)
													+ ").modsynth";
											System.out.println("Storing file as " + fileName);
											String[] names = ftpClient.listNames();
											for (int i = 0; i < names.length; i++) {
												if ((names[i].startsWith(soundName + "(")
														|| names[i].startsWith(soundName + "."))
														&& !names[i].equals(fileName)) {
													MainActivity.this.runOnUiThread(new Runnable() {
														public void run() {
															final MessageDialog verifyUpload = new MessageDialog(
																	MainActivity.this, "Exists",
																	"A shared file already exists by this name.  Choose a different name.",
																	new String[] { "OK" });
															verifyUpload.show();
														}
													});
													return;
												}
											}
											OutputStream outputStream = ftpClient.storeFileStream(fileName);
											ObjectOutputStream instrumentStream = new ObjectOutputStream(outputStream);
											instrumentStream.writeObject(((Instrument) synth.getInstrument()));
											outputStream.close();
											ftpClient.completePendingCommand();
											ftpClient.disconnect();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								};
								shareThread.start();
							}
						}
					});
				}
			}
		});
		promptForName.show();
	}

	public void record() {
		recordPane.setVisibility(View.VISIBLE);
		instrumentPane.setVisibility(View.GONE);
		recordButton.setText(ModSynthTranslator.translator.translate("  Done  "));
	}

	public void doneRecording() {
		stopRecording();
		recordPane.setVisibility(View.GONE);
		instrumentPane.setVisibility(View.VISIBLE);
		recordButton.setText(ModSynthTranslator.translator.translate("Record"));
	}

	public void sendSound() {
		final String soundName = soundSelector.getText().toString();
		try {
			File file = clientModel.exportObject(((Instrument) synth.getInstrument()), soundName + ".modsynth");
			System.out.println("Exported sound as " + soundName + ".modsynth");

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("application/vnd.gallantrealm.modsynth");
			Uri uri = Uri.fromFile(file);
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			intent.putExtra(Intent.EXTRA_SUBJECT, soundName);
			if (clientModel.isAmazon()) {
				intent.putExtra(Intent.EXTRA_TEXT,
						"Sharing an ModSynth instrument with you.  You can play it by installing ModSynth from the Amazon AppStore!");
			} else {
				intent.putExtra(Intent.EXTRA_TEXT,
						"Sharing an ModSynth instrument with you.  You can play it by installing ModSynth from Google Play at  http://play.google.com/store/apps/details?id=com.gallantrealm.modsynth");
			}
			clientModel.getContext().startActivity(intent);
		} catch (Exception e) { // might fail
			// ignore
		}
	}

	public void loadInstrument(final String soundName) {
		System.out.println("MainActivity.loadInstrument: " + soundName);
		soundSelector.setText(soundName);
		clientModel.setContext(MainActivity.this);
		Thread thread = new Thread() {
			public void run() {

				Instrument sound = null;
				if (soundName.startsWith("file://")) { // playing a sent sound
					sound = (Instrument) clientModel.loadObject(soundName, true);
				} else if (soundName.startsWith("BuiltIn/")) {
					try {
						String filename = soundName.substring("BuiltIn/".length()).trim();
						filename = "Instruments/" + filename;
						InputStream is = getAssets().open(filename + ".modsynth");
						ObjectInputStream inStream = new ObjectInputStream(is);
						sound = (Instrument) inStream.readObject();
						inStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (soundName.startsWith("Online/")) {
					try {
						FTPClient ftpClient = new FTPClient();
						ftpClient.connect("gallantrealm.com");
						ftpClient.enterLocalPassiveMode();
						ftpClient.login("modsynth@gallantrealm.com", "A23R3&8C");
						ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
						ftpClient.setKeepAlive(true);
						ftpClient.changeWorkingDirectory("/ModSynth");
						String fileName = soundName.substring("Online/".length());
						// find the file
						String[] names = ftpClient.listNames();
						for (int i = 0; i < names.length; i++) {
							if (names[i].startsWith(fileName)) {
								fileName = names[i];
								break;
							}
						}
						System.out.println(fileName);
						InputStream is = ftpClient.retrieveFileStream(fileName);
						ObjectInputStream inStream = new ObjectInputStream(is);
						sound = (Instrument) inStream.readObject();
						inStream.close();
						ftpClient.completePendingCommand();
						ftpClient.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					String fileName = soundName + ".modsynth";
					sound = (Instrument) clientModel.loadObject(fileName, true);
				}
				final Instrument newsound = sound;
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						if (newsound == null) {
							final MessageDialog loadMsg = new MessageDialog(MainActivity.this, "Load",
									soundName + " could not be loaded.", new String[] { "OK" });
							loadMsg.show();
						} else {
							applySound(newsound);
							if (lastSelectedModView != null) {
								lastSelectedModView.setVisibility(View.GONE);
							}
							if (synth != null && ((Instrument) synth.getInstrument()) != null) {
								if (((Instrument) synth.getInstrument()).selectedModule != null) {
									selectModule(((Instrument) synth.getInstrument()).selectedModule);
								} else {
									Module outputModule = ((Instrument) synth.getInstrument()).getOutputModule();
									if (outputModule != null) {
										selectModule(outputModule);
									}
								}
							}
						}

					}
				});
			}
		};
		thread.start();
	}

	public void deleteInstrument() {
		final String soundName = soundSelector.getText().toString();
		if (soundName.startsWith("BuiltIn/")) {
			MessageDialog cannotDelete = new MessageDialog(this, "Delete", "You cannot delete this instrument.",
					new String[] { "OK" });
			cannotDelete.show();
			return;
		}
		final MessageDialog promptForDelete = new MessageDialog(this, "Delete", "Delete instrument?",
				new String[] { "OK", "Cancel" });
		promptForDelete.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (promptForDelete.getButtonPressed() == 0) {
					if (soundName.startsWith("Online")) {
						// TODO implement delete for shared instruments
					} else {
						clientModel.deleteObject(soundName + ".modsynth", true);
					}
					System.out.println("Deleted sound " + soundName + ".modsynth");
					loadInstrument("BuiltIn/Basic");
					ClientModel.getClientModel().setInstrumentName(soundName);
					ClientModel.getClientModel().savePreferences();
					if (lastSelectedModView != null) {
						lastSelectedModView.setVisibility(View.GONE);
					}
				}
			}
		});
		promptForDelete.show();
	}

	InstrumentSelectorDialog instrumentSectoriDialog;

	public void selectInstrument() {
		if (instrumentSectoriDialog == null) {
			instrumentSectoriDialog = new InstrumentSelectorDialog(MainActivity.this, "ftp.gallantrealm.com",
					"modsynth@gallantrealm.com", "A23R3&8C", ".modsynth");
		}
		instrumentSectoriDialog.show("", new FileSelectorDialog.SelectionListener() {
			public void onFileselected(final String filename) {
				final String soundName = filename.substring(0, filename.indexOf(".modsynth"));
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						try {
							loadInstrument(soundName);
							ClientModel.getClientModel().setInstrumentName(soundName);
							ClientModel.getClientModel().savePreferences();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}

	public boolean unsavedRecording;

	public void startRecording() {
		if (unsavedRecording) {
			final MessageDialog message = new MessageDialog(MainActivity.this, null, "Erase unsaved recording?",
					new String[] { "OK", "Cancel" });
			message.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (message.getButtonPressed() == 0) {
						synth.startRecording();
						if (updateThread == null) {
							stopUpdateThread = false;
							updateThread = new UpdateThread();
							updateThread.start();
						}
						unsavedRecording = true;
					}
				}
			});
			message.show();
		} else {
			boolean started = synth.startRecording();
			if (!started) {
				MessageDialog message = new MessageDialog(MainActivity.this, null,
						"Not enough memory to record.  Close other apps and try again.", new String[] { "OK" });
				message.show();
			}
			if (updateThread == null) {
				stopUpdateThread = false;
				updateThread = new UpdateThread();
				updateThread.start();
			}
			unsavedRecording = true;
		}
	}

	public void stopRecording() {
		synth.stopRecording();
		if (updateThread != null) {
			updateThread.safeStop();
			updateThread = null;
		}
	}

	public void playbackRecording() {
		synth.playbackRecording();
		if (updateThread == null) {
			stopUpdateThread = false;
			updateThread = new UpdateThread();
			updateThread.start();
		}
	}

	static final public int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 123;
	static final public int REQUEST_PERMISSION_READ_PCM_EXTERNAL_STORAGE = 124;
	static final public int REQUEST_PERMISSION_READ_IMAGE_EXTERNAL_STORAGE = 125;
	static final public int REQUEST_PERMISSION_READ_MIDIFILE_EXTERNAL_STORAGE = 126;

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (grantResults == null || grantResults.length < 1) { // request was cancelled
			return;
		}
		switch (requestCode) {
		case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				saveRecording();
			} else {
				Toast.makeText(MainActivity.this, "Cannot save.  Permission denied.", Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_PERMISSION_READ_IMAGE_EXTERNAL_STORAGE:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				setCustomBackground(clientModel.getCustomBackgroundPath());
			} else {
				Toast.makeText(MainActivity.this, "Cannot open image.  Permission denied.", Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_PERMISSION_READ_PCM_EXTERNAL_STORAGE:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (((Instrument) synth.getInstrument()).selectedModule != null) {
					((PCMViewer) ((Instrument) synth.getInstrument()).selectedModule
							.getViewer((Instrument) synth.getInstrument())).onContinuePCMSelect(this);
					return;
				}
			} else {
				Toast.makeText(MainActivity.this, "Cannot open PCM.  Permission denied.", Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_PERMISSION_READ_MIDIFILE_EXTERNAL_STORAGE:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (((Instrument) synth.getInstrument()).selectedModule != null) {
					((MelodyViewer) ((Instrument) synth.getInstrument()).selectedModule
							.getViewer((Instrument) synth.getInstrument())).onContinueMidiFileSelect(this);
					return;
				}
			} else {
				Toast.makeText(MainActivity.this, "Cannot open MIDI file.  Permission denied.", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		default:
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	public String lastRecordName = "jam";

	@SuppressLint("NewApi")
	public void saveRecording() {
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(
				Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
					REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
			return;
		}

		File rootDir = new File("/sdcard");
		if (!rootDir.exists()) {
			rootDir = new File("/mnt/sdcard");
			if (!rootDir.exists()) {
				rootDir = Environment.getExternalStorageDirectory();
			}
		}
		final File modSynthDir = new File(rootDir.getPath() + "/ModSynth");
		if (!modSynthDir.exists()) {
			modSynthDir.mkdir();
		}
		final InputDialog inputDialog = new InputDialog(this, "Save Recording", "Recording name:", lastRecordName,
				new String[] { "Save", "Cancel" });
		inputDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (inputDialog.getButtonPressed() == 0) {
					final String recordname = inputDialog.getValue();
					lastRecordName = recordname;
					final String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ModSynth/"
							+ recordname + ".wav";
					AsyncTask.execute(new Runnable() {
						public void run() {
							try {
								File file = new File(filename);
								synth.saveRecording(filename);
								sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
								System.out.println("SAVED RECORDING to " + filename);
								unsavedRecording = false;
								runOnUiThread(new Runnable() {
									public void run() {
										Toast toast = Toast.makeText(MainActivity.this, "Saved to " + filename,
												Toast.LENGTH_LONG);
										toast.show();
									}
								});
							} catch (IOException e) {
								e.printStackTrace();
								runOnUiThread(new Runnable() {
									public void run() {
										Toast toast = Toast.makeText(MainActivity.this, "Failed to save to " + filename,
												Toast.LENGTH_LONG);
										toast.show();
									}
								});
							}
						}
					});

				}
			}
		});
		inputDialog.show();
	}

	boolean applyingASound = false;

	private void applySound(Instrument sound) {
		if (sound == null || synth == null) {
			System.out.println("Sound or synth is null!");
			return;
		}
		applyingASound = true;
		// holdButton.setChecked(sound.hold);
		if (sound.requiresUpgrade() != null) {
			MessageDialog message = new MessageDialog(MainActivity.this, null, "The " + sound.requiresUpgrade()
					+ " module has significantly changed since this instrument was last saved.  The settings may need adjustment.",
					new String[] { "OK" });
			message.show();
		}
		try {
			synth.setInstrument(sound);
			updateControls();
			applyingASound = false;
			modGraph.setInstrument(sound, synth);
		} catch (OutOfMemoryError e) {
			MessageDialog message = new MessageDialog(MainActivity.this, null,
					"Not enough memory to load this instrument.  Close other apps and try again.",
					new String[] { "OK" });
			message.show();
			applyingASound = false;
		}
	}

	private void updateControls() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				applyingASound = true;
				Instrument instrument = (Instrument) synth.getInstrument();
				for (Module module : instrument.modules) {
					((ModuleViewer) module.getViewer(instrument)).dropView();
				}
				if (instrument.getKeyboardModule() == null) {
					keyboardPane.setVisibility(View.GONE);
				} else if (!midi.isMidiDeviceAttached()) {
					keyboardPane.setVisibility(View.VISIBLE);
				}
				selectModule(((Instrument) synth.getInstrument()).selectedModule);
				applyingASound = false;
			}
		});
	}

	@Override
	protected void onStart() {
		System.out.println(">>MainActivity.onStart");
		setLanguage(clientModel.getLanguage());
		super.onStart();
		if (getIntent().getData() != null) {
			System.out.println("Invocation params: " + getIntent().getData());
			loadInstrument(getIntent().getData().toString());
		}
		wakelock.acquire();
//		if (googleApiClient != null) {
//			googleApiClient.connect();
//			System.out.println("Started connection to Google Play Services");
//		}
		if (Build.VERSION.SDK_INT >= 24) {
			getWindow().setSustainedPerformanceMode(true);
		}
		System.out.println("<<MainActivity.onStart");
	}

	@Override
	protected void onStop() {
		System.out.println(">>MainActivity.onStop");
		super.onStop();
		if (updateThread != null) {
			stopRecording();
		}
		synth.stop();
		wakelock.release();
//		if (googleApiClient != null) {
//			googleApiClient.disconnect();
//		}
		System.out.println("<<MainActivity.onStop");
// Note: although it would be good to finish, it causes problems for file pickers
//		finish();  // force complete destroy to keep from eating resources
	}

	@Override
	protected void onResume() {
		System.out.println(">>MainActivity.onResume");
		super.onResume();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();
		if (isScreenOn) {
			try {
				synth.start();
			} catch (Exception e) {
				final MessageDialog dialog = new MessageDialog(this, null, e.getMessage(), null);
				dialog.show();
			}
		}
		System.out.println("<<MainActivity.onResume");
	}

	@Override
	protected void onPause() {
		System.out.println(">>MainActivity.onPause");
		super.onPause();
		// synth.stop(); this is done in onStop so multi-window will still run the synth
		System.out.println("<<MainActivity.onPause");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void selectModule(final Module module) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (!modGraph.isEditing()) {
					if (module != null) {
						module.isSelected = true; // not correct when initially loading an instrument
					}
					modGraph.invalidate();
					if (lastSelectedModView != null) {
						lastSelectedModView.setVisibility(View.GONE);
					}
					if (module != null) {
						noModSelectedText.setVisibility(View.GONE);
						lastSelectedModView = ((ModuleViewer) module.getViewer((Instrument) synth.getInstrument()))
								.getView(MainActivity.this, modViewGroup);
						lastSelectedModView.setVisibility(View.VISIBLE);
					} else {
						noModSelectedText.setVisibility(View.VISIBLE);
					}
					if (module instanceof Output) {
						scope = ((OutputViewer) module.getViewer((Instrument) synth.getInstrument())).scope;
					} else {
						scope = null;
					}
				} else {
					scope = null;
				}
			}
		});
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			nKeyDown(event.getKeyCode(), event);
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			nKeyUp(event.getKeyCode(), event);
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed() {
		promptForFinish();
	}

	/**
	 * Override the back button to prompt for quit.
	 */
	public boolean nKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() > 0) {
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_SPACE) {
			upperOctave = true;
			return true;
		}
		int note = getNoteForKeyCode(keyCode);
		float velocity = 1.0f;
		if (note >= 0) {
			if (synth.getInstrument() != null) {
				synth.getInstrument().notePress(note, velocity);
			}
			return true;
		}
		return false;
	}

	public boolean nKeyUp(int keyCode, KeyEvent event) {
		int note = getNoteForKeyCode(keyCode);
		if (keyCode == KeyEvent.KEYCODE_SPACE) {
			upperOctave = false;
			return true;
		}
		if (note >= 0) {
			if (synth.getInstrument() != null) {
				synth.getInstrument().noteRelease(note);
			}
		}
		return false;
	}

	private void promptForFinish() {
		final MessageDialog dialog = new MessageDialog(this, null, "Are you sure you want to quit?", new String[] { //
				Translator.getTranslator().translate("Yes"), //
				Translator.getTranslator().translate("No") }, null);
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface d) {
				int rc = dialog.getButtonPressed();
				if (rc == 0) {
					System.out.println("Calling finish..");
					MainActivity.this.finish();
				}
			}
		});
		dialog.show();
	}

	boolean upperOctave;

	private int getNoteForKeyCode(int keyCode) {
		int note = -1;
		if (keyCode == KeyEvent.KEYCODE_A) {
			note = 0;
		} else if (keyCode == KeyEvent.KEYCODE_W) {
			note = 1;
		} else if (keyCode == KeyEvent.KEYCODE_S) {
			note = 2;
		} else if (keyCode == KeyEvent.KEYCODE_E) {
			note = 3;
		} else if (keyCode == KeyEvent.KEYCODE_D) {
			note = 4;
		} else if (keyCode == KeyEvent.KEYCODE_F) {
			note = 5;
		} else if (keyCode == KeyEvent.KEYCODE_T) {
			note = 6;
		} else if (keyCode == KeyEvent.KEYCODE_G) {
			note = 7;
		} else if (keyCode == KeyEvent.KEYCODE_Y) {
			note = 8;
		} else if (keyCode == KeyEvent.KEYCODE_H) {
			note = 9;
		} else if (keyCode == KeyEvent.KEYCODE_U) {
			note = 10;
		} else if (keyCode == KeyEvent.KEYCODE_J) {
			note = 11;
		} else if (keyCode == KeyEvent.KEYCODE_K) {
			note = 12;
		} else if (keyCode == KeyEvent.KEYCODE_O) {
			note = 13;
		} else if (keyCode == KeyEvent.KEYCODE_L) {
			note = 14;
		} else if (keyCode == KeyEvent.KEYCODE_P) {
			note = 15;
		} else if (keyCode == KeyEvent.KEYCODE_SEMICOLON) {
			note = 16;
		} else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE) {
			note = 17;
		} else if (keyCode == KeyEvent.KEYCODE_LEFT_BRACKET) {
			note = 18;
		}
		if (note >= 0 && upperOctave) {
			note += 12;
		}
		int midiNote = note + 60 - 12;
		return midiNote;
	}

	public void setTheme(String option, String customImagePath) {
		if (option == null || option.equals(BlueTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.blue_background);
		} else if (option.equals(GreenTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.green_background);
		} else if (option.equals(OnyxTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.onyx_background);
		} else if (option.equals(MetalTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.metal_background);
		} else if (option.equals(WoodTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.wood_background);
		} else if (option.equals(AuraTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.aura_background);
		} else if (option.equals(IceTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.ice_background);
		} else if (option.equals(CircuitTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.circuit_background);
		} else if (option.equals(SpaceTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.space_background);
		} else if (option.equals(SunsetTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.sunset_background);
		} else if (option.equals(TropicalTheme.class.getName())) {
			mainLayout.setBackgroundResource(R.raw.tropical_background);
		} else if (option.equals(CustomTheme.class.getName())) {
			if (customImagePath != null) {
				setCustomBackground(customImagePath);
			} else {
				mainLayout.setBackgroundResource(R.raw.onyx_background);
				selectingCustomTheme = true;
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Complete action using"), 0);
			}
		}
	}

	@Override
	public void clientModelChanged(ClientModelChangedEvent event) {
		if (event.getEventType() == ClientModelChangedEvent.EVENT_TYPE_FULLVERSION_CHANGED) {
			if (clientModel.isFullVersion()) {
				fullVersionButton.setVisibility(View.GONE);
			}
		}
	}

}
