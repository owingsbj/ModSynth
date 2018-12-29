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
import com.gallantrealm.modsynth.viewer.ModuleViewer;
import com.gallantrealm.modsynth.viewer.PCMViewer;
import com.gallantrealm.mysynth.ClientModel;
import com.gallantrealm.mysynth.ClientModelChangedEvent;
import com.gallantrealm.mysynth.ClientModelChangedListener;
import com.gallantrealm.mysynth.FastMath;
import com.gallantrealm.mysynth.InputDialog;
import com.gallantrealm.mysynth.MessageDialog;
import com.gallantrealm.mysynth.MySynth;
import com.gallantrealm.mysynth.MySynthAAudio;
import com.gallantrealm.mysynth.MySynthOpenSL;
import com.gallantrealm.mysynth.SelectItemDialog;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.PendingResult;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.drive.Drive;
//import com.google.android.gms.drive.DriveApi;
//import com.google.android.gms.drive.DriveApi.DriveContentsResult;
//import com.google.android.gms.drive.DriveApi.DriveIdResult;
//import com.google.android.gms.drive.DriveContents;
//import com.google.android.gms.drive.DriveFile;
//import com.google.android.gms.drive.DriveId;
//import com.google.android.gms.drive.OpenFileActivityBuilder;
import android.Manifest;
import android.annotation.SuppressLint;
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
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import jp.kshoji.driver.midi.activity.AbstractSingleMidiActivity;
import jp.kshoji.driver.midi.device.MidiInputDevice;

public class MainActivity extends AbstractSingleMidiActivity implements OnTouchListener, View.OnClickListener, ClientModelChangedListener //
//		,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener 
{

	public ClientModel clientModel = ClientModel.getClientModel();

	View mainLayout;
	public View operatorPane;
	public View instrumentPane;
	public View recordPane;
	public View modGraphPane;
	public ModGraph modGraph;

	public View keyboardPane;

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

	View keyboard;
	int keyboardLocation[];
	Button key1;
	Button key2;
	Button key3;
	Button key4;
	Button key5;
	Button key6;
	Button key7;
	Button key8;
	Button key9;
	Button key10;
	Button key11;
	Button key12;
	Button key13;
	Button key14;
	Button key15;
	Button key16;
	Button key17;
	Button key18;
	Button key19;
	Button key20;
	Button key21;
	Button key22;
	Button key23;
	Button key24;
	Button key25;
	Button key26;
	Button key27;
	Button key28;
	Button key29;
	Button key30;
	Button key31;
	Button key32;

	int[] keyvoice = new int[25];

	MySynth synth;

	int myMidiChannel;

	PowerManager.WakeLock wakelock;

	String builtinSeparatorText;
	String customSeparatorText;

//	SapaService sapaService; // Samsung Professional Audio

	ArrayAdapter<CharSequence> soundAdapter;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println(">>MainActivity.onCreate");
		super.onCreate(savedInstanceState);

		// Restore the preferences. If this is the first time, set some of the
		// preferences to good defaults
		clientModel.loadPreferences(this);
		if (clientModel.getPlayCount() <= 1) {
			clientModel.setKeyboardSize(2); // keyboard size to 2 octaves
			clientModel.savePreferences(this);
		}

		// Set up the translator with the right language (before rendering view)
		setLanguage(clientModel.getLanguage());

		setContentView(R.layout.synth_screen);

		// Set sustained performance mode (Android 7.0+)
		if (Build.VERSION.SDK_INT >= 24) {
			getWindow().setSustainedPerformanceMode(true);
		}

		// Determine if AAudio is available and stable. If so, use ModSynthAAudio. Else use ModSynthOpenSL
		if (Build.VERSION.SDK_INT >= 27) {
			int sampleRateReducer = Math.max(0, clientModel.getSampleRateReducer());
			int nbuffers = clientModel.getNBuffers();
			if (nbuffers == 0) {
				nbuffers = 5;
			}
			synth = new MySynthAAudio(sampleRateReducer, nbuffers);
		} else {
			int sampleRateReducer = Math.max(0, clientModel.getSampleRateReducer());
			int nbuffers = clientModel.getNBuffers();
			if (nbuffers == 0) {
				nbuffers = 5;
			}
			synth = new MySynthOpenSL(sampleRateReducer, nbuffers);
		}
		synth.setCallbacks(new MySynth.Callbacks() {
			@Override
			public void updateLevels() {
				if (modGraph != null) {
					modGraph.updateLevels();
				}
			}
		});
//		}

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

		keyboard = keyboardPane.findViewById(R.id.keyboard);
		key1 = (Button) keyboardPane.findViewById(R.id.key1);
		key2 = (Button) keyboardPane.findViewById(R.id.key2);
		key3 = (Button) keyboardPane.findViewById(R.id.key3);
		key4 = (Button) keyboardPane.findViewById(R.id.key4);
		key5 = (Button) keyboardPane.findViewById(R.id.key5);
		key6 = (Button) keyboardPane.findViewById(R.id.key6);
		key7 = (Button) keyboardPane.findViewById(R.id.key7);
		key8 = (Button) keyboardPane.findViewById(R.id.key8);
		key9 = (Button) keyboardPane.findViewById(R.id.key9);
		key10 = (Button) keyboardPane.findViewById(R.id.key10);
		key11 = (Button) keyboardPane.findViewById(R.id.key11);
		key12 = (Button) keyboardPane.findViewById(R.id.key12);
		key13 = (Button) keyboardPane.findViewById(R.id.key13);
		key14 = (Button) keyboardPane.findViewById(R.id.key14);
		key15 = (Button) keyboardPane.findViewById(R.id.key15);
		key16 = (Button) keyboardPane.findViewById(R.id.key16);
		key17 = (Button) keyboardPane.findViewById(R.id.key17);
		key18 = (Button) keyboardPane.findViewById(R.id.key18);
		key19 = (Button) keyboardPane.findViewById(R.id.key19);
		key20 = (Button) keyboardPane.findViewById(R.id.key20);
		key21 = (Button) keyboardPane.findViewById(R.id.key21);
		key22 = (Button) keyboardPane.findViewById(R.id.key22);
		key23 = (Button) keyboardPane.findViewById(R.id.key23);
		key24 = (Button) keyboardPane.findViewById(R.id.key24);
		key25 = (Button) keyboardPane.findViewById(R.id.key25);
		key26 = (Button) keyboardPane.findViewById(R.id.key26);
		key27 = (Button) keyboardPane.findViewById(R.id.key27);
		key28 = (Button) keyboardPane.findViewById(R.id.key28);
		key29 = (Button) keyboardPane.findViewById(R.id.key29);
		key30 = (Button) keyboardPane.findViewById(R.id.key30);
		key31 = (Button) keyboardPane.findViewById(R.id.key31);
		key32 = (Button) keyboardPane.findViewById(R.id.key32);

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

		setKeyboardSize(clientModel.getKeyboardSize());

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
		keyboard.setOnTouchListener(this);

		if (clientModel.isFullVersion() || clientModel.isGoggleDogPass() || clientModel.isFree()) {
			fullVersionButton.setVisibility(View.GONE);
		} else {
			fullVersionButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					clientModel.setContext(MainActivity.this);
					final MessageDialog mdialog = new MessageDialog(MainActivity.this, "Full Version", "Add FM Operators, spectral filtering, PCM synthesis, harmonic editing and more with ModSynth full version!",
							new String[] { "Buy", "Later" });
					mdialog.show();
					mdialog.setOnDismissListener(new OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if (mdialog.getButtonPressed() == 0) {
								clientModel.buyFullVersion();
							}
						}
					});
				}
			});
		}

		// set up the modgraph selection handler
		modGraph.setOnSelectionListener(new ModGraph.OnSelectionListener() {
			public void selected(Module module) {
//				if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass() && ((Instrument)synth.getInstrument()).hasAdvancedModules()) {
//					MessageDialog dialog = new MessageDialog(MainActivity.this, "Full Version", "Full version is needed to edit this instrument.", new String[] {"OK"});
//					dialog.show();
//					return;
//				}
				selectModule(module);
			}
		});

		editGraphButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!modGraph.isEditing()) {
					if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass() && ((Instrument)synth.getInstrument()) != null && ((Instrument)synth.getInstrument()).hasAdvancedModules()) {
						MessageDialog dialog = new MessageDialog(MainActivity.this, "Full Version", "Full version is needed to edit this instrument.", new String[] { "OK" });
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
					if (!midiDeviceAttached) {
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
						MessageDialog message = new MessageDialog(MainActivity.this, null, "Not enough memory to load this instrument.  Close other apps and try again.", new String[] { "OK" });
						message.show();
					}
				}
			}
		});
		addModuleButton.setVisibility(View.GONE);
		deleteModuleButton.setVisibility(View.GONE);

		modViewGroup.setClickable(true);
		modViewGroup.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
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
		synth.stop();
		synth.destroy();
		synth = null;
//		if (sapaService != null) {
//			System.out.println("Stopping SapaService");
//			sapaService.stop(true);
//		}
		super.onDestroy();
		System.out.println("<<MainActivity.onDestroy");
	}

	boolean selectingCustomTheme;

	/**
	 * Needed for in-app purchase, file chooser, and other actions that launch separate activities.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Pass on the activity result to clientModel for proper in-app billing behavior
		if (clientModel.handleActivityResult(requestCode, resultCode, data)) {
			return;
		}

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
					ClientModel.getClientModel().savePreferences(this);
					setCustomBackground(path);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, "Custom background could not be opened.  Try a different image", Toast.LENGTH_LONG).show();
				}
			}
		} else if (data != null && synth != null && ((Instrument)synth.getInstrument()) != null && ((Instrument)synth.getInstrument()).selectedModule != null) {
			((ModuleViewer) ((Instrument)synth.getInstrument()).selectedModule.getViewer(synth)).onActivityResult(requestCode, resultCode, data);
			return;
		}

		// not handled, so let default handle
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressLint("NewApi")
	private void setCustomBackground(String path) {
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_PERMISSION_READ_IMAGE_EXTERNAL_STORAGE);
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

	// private void setDefaultSound() {
	// System.out.println("Setting default sound");
	// Instrument instrument = new Instrument();
	//
	// Keyboard keyboardModule = new Keyboard();
	// keyboardModule.xPosition = 50;
	// keyboardModule.yPosition = 100;
	//
	// Oscillator oscModule = new Oscillator();
	// oscModule.waveForm = WaveForm.SAWTOOTH_WAVE;
	// oscModule.xPosition = 200;
	// oscModule.yPosition = 50;
	// oscModule.link(1, keyboardModule, 1);
	//
	// Envelope envelopeModule = new Envelope();
	// envelopeModule.xPosition = 200;
	// envelopeModule.yPosition = 200;
	// envelopeModule.link(1, keyboardModule, 2);
	//
	// Filter filterModule = new Filter();
	// filterModule.xPosition = 350;
	// filterModule.yPosition = 50;
	// filterModule.resonance = 8;
	// filterModule.cutoff = 50;
	// filterModule.sweep = 50;
	// filterModule.link(1, oscModule, 1);
	// filterModule.link(2, envelopeModule, 1);
	//
	// Amp ampModule = new Amp();
	// ampModule.xPosition = 500;
	// ampModule.yPosition = 50;
	// ampModule.link(1, filterModule, 1);
	// ampModule.link(2, envelopeModule, 1);
	//
	// Output outputModule = new Output();
	// outputModule.xPosition = 650;
	// outputModule.yPosition = 50;
	// outputModule.link(1, ampModule, 1);
	//
	// instrument.modules.add(keyboardModule);
	// instrument.modules.add(oscModule);
	// instrument.modules.add(filterModule);
	// instrument.modules.add(envelopeModule);
	// instrument.modules.add(ampModule);
	// instrument.modules.add(outputModule);
	//
	// applySound(instrument);
	// }

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
				if (((Instrument)synth.getInstrument()).selectedModule == null) {
					new MessageDialog(this, "Delete", "No module selected.", null).show();
					return;
				}
				if (((Instrument)synth.getInstrument()).selectedModule instanceof Output) {
					new MessageDialog(this, "Delete", "You cannot delete the Output module.", null).show();
				} else {
					final MessageDialog promptForDelete = new MessageDialog(this, "Delete", "Delete module?", new String[] { "OK", "Cancel" });
					promptForDelete.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (promptForDelete.getButtonPressed() == 0) {
								modGraph.deleteModule(((Instrument)synth.getInstrument()).selectedModule);
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

		// } else if (v == recordStartButton) {
		// startRecording();
		// } else if (v == recordStopButton) {
		// stopRecording();
		// } else if (v == recordPlayButton) {
		// playbackRecording();
		// } else if (v == recordSaveButton) {
		// saveRecording();
	}

	public void setKeyboardSize(int keysSelection) {
		if (keysSelection == 0) { // 13
			key14.setVisibility(View.GONE);
			key15.setVisibility(View.GONE);
			key16.setVisibility(View.GONE);
			keyboardPane.findViewById(R.id.key16spacer).setVisibility(View.GONE);
			key17.setVisibility(View.GONE);
			key18.setVisibility(View.GONE);
			key19.setVisibility(View.GONE);
			key20.setVisibility(View.GONE);
			key21.setVisibility(View.GONE);
			key22.setVisibility(View.GONE);
			key23.setVisibility(View.GONE);
			keyboardPane.findViewById(R.id.key23spacer).setVisibility(View.GONE);
			key24.setVisibility(View.GONE);
			key25.setVisibility(View.GONE);
			key26.setVisibility(View.GONE);
			key27.setVisibility(View.GONE);
			key28.setVisibility(View.GONE);
			keyboardPane.findViewById(R.id.key28spacer).setVisibility(View.GONE);
			key29.setVisibility(View.GONE);
			key30.setVisibility(View.GONE);
			key31.setVisibility(View.GONE);
			key32.setVisibility(View.GONE);
		} else if (keysSelection == 1) { // 20
			key14.setVisibility(View.VISIBLE);
			key15.setVisibility(View.VISIBLE);
			key16.setVisibility(View.VISIBLE);
			keyboardPane.findViewById(R.id.key16spacer).setVisibility(View.VISIBLE);
			key17.setVisibility(View.VISIBLE);
			key18.setVisibility(View.VISIBLE);
			key19.setVisibility(View.VISIBLE);
			key20.setVisibility(View.VISIBLE);
			key21.setVisibility(View.GONE);
			key22.setVisibility(View.GONE);
			key23.setVisibility(View.GONE);
			keyboardPane.findViewById(R.id.key23spacer).setVisibility(View.GONE);
			key24.setVisibility(View.GONE);
			key25.setVisibility(View.GONE);
			key26.setVisibility(View.GONE);
			key27.setVisibility(View.GONE);
			key28.setVisibility(View.GONE);
			keyboardPane.findViewById(R.id.key28spacer).setVisibility(View.GONE);
			key29.setVisibility(View.GONE);
			key30.setVisibility(View.GONE);
			key31.setVisibility(View.GONE);
			key32.setVisibility(View.GONE);
		} else if (keysSelection == 2) { // 25
			key14.setVisibility(View.VISIBLE);
			key15.setVisibility(View.VISIBLE);
			key16.setVisibility(View.VISIBLE);
			keyboardPane.findViewById(R.id.key16spacer).setVisibility(View.VISIBLE);
			key17.setVisibility(View.VISIBLE);
			key18.setVisibility(View.VISIBLE);
			key19.setVisibility(View.VISIBLE);
			key20.setVisibility(View.VISIBLE);
			key21.setVisibility(View.VISIBLE);
			key22.setVisibility(View.VISIBLE);
			key23.setVisibility(View.VISIBLE);
			keyboardPane.findViewById(R.id.key23spacer).setVisibility(View.VISIBLE);
			key24.setVisibility(View.VISIBLE);
			key25.setVisibility(View.VISIBLE);
			key26.setVisibility(View.GONE);
			key27.setVisibility(View.GONE);
			key28.setVisibility(View.GONE);
			keyboardPane.findViewById(R.id.key28spacer).setVisibility(View.GONE);
			key29.setVisibility(View.GONE);
			key30.setVisibility(View.GONE);
			key31.setVisibility(View.GONE);
			key32.setVisibility(View.GONE);
		} else if (keysSelection == 3) { // 32
			key14.setVisibility(View.VISIBLE);
			key15.setVisibility(View.VISIBLE);
			key16.setVisibility(View.VISIBLE);
			keyboardPane.findViewById(R.id.key16spacer).setVisibility(View.VISIBLE);
			key17.setVisibility(View.VISIBLE);
			key18.setVisibility(View.VISIBLE);
			key19.setVisibility(View.VISIBLE);
			key20.setVisibility(View.VISIBLE);
			key21.setVisibility(View.VISIBLE);
			key22.setVisibility(View.VISIBLE);
			key23.setVisibility(View.VISIBLE);
			keyboardPane.findViewById(R.id.key23spacer).setVisibility(View.VISIBLE);
			key24.setVisibility(View.VISIBLE);
			key25.setVisibility(View.VISIBLE);
			key26.setVisibility(View.VISIBLE);
			key27.setVisibility(View.VISIBLE);
			key28.setVisibility(View.VISIBLE);
			keyboardPane.findViewById(R.id.key28spacer).setVisibility(View.VISIBLE);
			key29.setVisibility(View.VISIBLE);
			key30.setVisibility(View.VISIBLE);
			key31.setVisibility(View.VISIBLE);
			key32.setVisibility(View.VISIBLE);
		}
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
		this.myMidiChannel = midiChannel;
	}

	public void setTuningCents(int cents) {
		Settings.tuningCents = cents;
	}

	public void saveInstrument() {
		if (synth == null || ((Instrument)synth.getInstrument()) == null) {
			return;
		}
		if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass() && ((Instrument)synth.getInstrument()).hasAdvancedModules()) {
			MessageDialog dialog = new MessageDialog(MainActivity.this, "Full Version", "Full version is needed to save this instrument.", new String[] { "OK" });
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
			promptForName = new InputDialog(this, "Save", "Save instrument as ", finalInitialSoundName.trim(), new String[] { "Save", "Share", "Cancel" });
		} else {
			promptForName = new InputDialog(this, "Save", "Save instrument as ", finalInitialSoundName.trim(), new String[] { "Save", "Cancel" });
		}
		promptForName.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				System.out.println("MainActivity.saveInstrument: button pressed = " + promptForName.getButtonPressed());
				final String soundName = promptForName.getValue();
				if (promptForName.getButtonPressed() == 0) {
					clientModel.setContext(MainActivity.this);
					clientModel.saveObject(((Instrument)synth.getInstrument()), soundName + ".modsynth", true);
					if (synth != null && ((Instrument)synth.getInstrument()) != null) {
						((Instrument)synth.getInstrument()).clearDirty();
					}
					System.out.println("Saved sound as " + soundName + ".modsynth");

					// Uncomment to save for update
					// clientModel.exportObject(sound, soundName + ".modsynth");

					soundSelector.setText(soundName);
					ClientModel.getClientModel().setInstrumentName(soundName);
					ClientModel.getClientModel().savePreferences(MainActivity.this);
				}
				if (clientModel.isGoggleDogPass() && promptForName.getButtonPressed() == 1) {
					final MessageDialog verifyUpload = new MessageDialog(MainActivity.this, "Share", "This will upload your instrument to gallantrealm.com for sharing online.", new String[] { "OK" });
					verifyUpload.show();
					verifyUpload.setOnDismissListener(new DialogInterface.OnDismissListener() {
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
											String fileName = soundName + "(" + Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID) + ").modsynth";
											System.out.println("Storing file as " + fileName);
											String[] names = ftpClient.listNames();
											for (int i = 0; i < names.length; i++) {
												if ((names[i].startsWith(soundName + "(") || names[i].startsWith(soundName + ".")) && !names[i].equals(fileName)) {
													MainActivity.this.runOnUiThread(new Runnable() {
														public void run() {
															final MessageDialog verifyUpload = new MessageDialog(MainActivity.this, "Exists", "A shared file already exists by this name.  Choose a different name.", new String[] { "OK" });
															verifyUpload.show();
														}
													});
													return;
												}
											}
											OutputStream outputStream = ftpClient.storeFileStream(fileName);
											ObjectOutputStream instrumentStream = new ObjectOutputStream(outputStream);
											instrumentStream.writeObject(((Instrument)synth.getInstrument()));
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
			File file = clientModel.exportObject(((Instrument)synth.getInstrument()), soundName + ".modsynth");
			System.out.println("Exported sound as " + soundName + ".modsynth");

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("application/vnd.gallantrealm.modsynth");
			Uri uri = Uri.fromFile(file);
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			intent.putExtra(Intent.EXTRA_SUBJECT, soundName);
			if (clientModel.isAmazon()) {
				intent.putExtra(Intent.EXTRA_TEXT, "Sharing an ModSynth instrument with you.  You can play it by installing ModSynth from the Amazon AppStore!");
			} else {
				intent.putExtra(Intent.EXTRA_TEXT, "Sharing an ModSynth instrument with you.  You can play it by installing ModSynth from Google Play at  http://play.google.com/store/apps/details?id=com.gallantrealm.modsynth");
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
							final MessageDialog loadMsg = new MessageDialog(MainActivity.this, "Load", soundName + " could not be loaded.", new String[] { "OK" });
							loadMsg.show();
						} else {
							applySound(newsound);
							if (lastSelectedModView != null) {
								lastSelectedModView.setVisibility(View.GONE);
							}
							if (synth != null && ((Instrument)synth.getInstrument()) != null) {
								if (((Instrument)synth.getInstrument()).selectedModule != null) {
									selectModule(((Instrument)synth.getInstrument()).selectedModule);
								} else {
									Module outputModule = ((Instrument)synth.getInstrument()).getOutputModule();
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
			MessageDialog cannotDelete = new MessageDialog(this, "Delete", "You cannot delete this instrument.", new String[] { "OK" });
			cannotDelete.show();
			return;
		}
		final MessageDialog promptForDelete = new MessageDialog(this, "Delete", "Delete instrument?", new String[] { "OK", "Cancel" });
		promptForDelete.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (promptForDelete.getButtonPressed() == 0) {
					if (soundName.startsWith("Online")) {
						// TODO
					} else {
						clientModel.deleteObject(soundName + ".modsynth", true);
					}
					System.out.println("Deleted sound " + soundName + ".modsynth");
					loadInstrument("BuiltIn/Basic");
					ClientModel.getClientModel().setInstrumentName(soundName);
					ClientModel.getClientModel().savePreferences(MainActivity.this);
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
			instrumentSectoriDialog = new InstrumentSelectorDialog(MainActivity.this, "ftp.gallantrealm.com", "modsynth@gallantrealm.com", "A23R3&8C", ".modsynth");
		}
		instrumentSectoriDialog.show("", new FileSelectorDialog.SelectionListener() {
			public void onFileselected(final String filename) {
				final String soundName = filename.substring(0, filename.indexOf(".modsynth"));
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						try {
							loadInstrument(soundName);
							ClientModel.getClientModel().setInstrumentName(soundName);
							ClientModel.getClientModel().savePreferences(MainActivity.this);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}

	public void loadProgram(final int program) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String soundName = clientModel.findObject("" + program + " ", ".modsynth");
				if (soundName == null) {
					Toast.makeText(MainActivity.this, "No instrument found with name starting with \"" + program + " \" ", Toast.LENGTH_LONG).show();
				} else {
					loadInstrument(soundName);
					ClientModel.getClientModel().setInstrumentName(soundName);
					ClientModel.getClientModel().savePreferences(MainActivity.this);
				}
			}
		});
	}

	public boolean unsavedRecording;

	public void startRecording() {
		if (unsavedRecording) {
			final MessageDialog message = new MessageDialog(MainActivity.this, null, "Erase unsaved recording?", new String[] { "OK", "Cancel" });
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
				MessageDialog message = new MessageDialog(MainActivity.this, null, "Not enough memory to record.  Close other apps and try again.", new String[] { "OK" });
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
				Toast.makeText(MainActivity.this, "Cannot open PCM.  Permission denied.", Toast.LENGTH_SHORT).show();
			}
			break;
		case REQUEST_PERMISSION_READ_PCM_EXTERNAL_STORAGE:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (((Instrument)synth.getInstrument()).selectedModule != null) {
					((PCMViewer) ((Instrument)synth.getInstrument()).selectedModule.getViewer(synth)).onContinuePCMSelect(this);
					return;
				}
			} else {
				Toast.makeText(MainActivity.this, "Cannot open PCM.  Permission denied.", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	public String lastRecordName = "jam";

	@SuppressLint("NewApi")
	public void saveRecording() {
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
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
		final InputDialog inputDialog = new InputDialog(this, "Save Recording", "Recording name:", lastRecordName, new String[] { "Save", "Cancel" });
		inputDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (inputDialog.getButtonPressed() == 0) {
					final String recordname = inputDialog.getValue();
					lastRecordName = recordname;
					final String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ModSynth/" + recordname + ".wav";
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
										Toast toast = Toast.makeText(MainActivity.this, "Saved to " + filename, Toast.LENGTH_LONG);
										toast.show();
									}
								});
							} catch (IOException e) {
								e.printStackTrace();
								runOnUiThread(new Runnable() {
									public void run() {
										Toast toast = Toast.makeText(MainActivity.this, "Failed to save to " + filename, Toast.LENGTH_LONG);
										toast.show();
									}
								});
							}
						}
					});

					// Intent intent = new Intent(Intent.ACTION_SEND);
					// intent.setType("audio/wav");
					// Uri uri = Uri.fromFile(file);
					// intent.putExtra(Intent.EXTRA_STREAM, uri);
					// intent.putExtra(Intent.EXTRA_SUBJECT, recordname +
					// ".wav");
					// intent.putExtra(Intent.EXTRA_TEXT, "Sharing a recording
					// with you. I made it LIVE using ModSynth!");
					// try {
					// clientModel.getContext().startActivity(intent);
					// } catch (Exception e) {
					// MessageDialog message = new
					// MessageDialog(MainActivity.this, null, "Recording saved
					// at " + filename + ". You will need to use a file manager
					// app to access the file.", new String[] { "OK" });
					// message.show();
					// }

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
			MessageDialog message = new MessageDialog(MainActivity.this, null, "The " + sound.requiresUpgrade() + " module has significantly changed since this instrument was last saved.  The settings may need adjustment.",
					new String[] { "OK" });
			message.show();
		}
		try {
			synth.setInstrument(sound);
			updateControls();
			applyingASound = false;
			modGraph.setInstrument(sound, synth);
		} catch (OutOfMemoryError e) {
			MessageDialog message = new MessageDialog(MainActivity.this, null, "Not enough memory to load this instrument.  Close other apps and try again.", new String[] { "OK" });
			message.show();
			applyingASound = false;
		}
	}

	private void updateControls() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				applyingASound = true;
				for (Module module : ((Instrument)synth.getInstrument()).modules) {
					((ModuleViewer) module.getViewer(synth)).dropView();
				}
				if (((Instrument)synth.getInstrument()).getKeyboardModule() == null) {
					keyboardPane.setVisibility(View.GONE);
				} else if (!midiDeviceAttached) {
					keyboardPane.setVisibility(View.VISIBLE);
				}
				selectModule(((Instrument)synth.getInstrument()).selectedModule);
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

	int[] lastNote = new int[20]; // 20 fingers max

	private static final int PRESS = 1;
	private static final int RELEASE = 2;
	private static final int SLIDE = 0;

	private float lastTouchDownX;
	private float lastTouchDownY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int index = event.getActionIndex();
		int pointerCount = event.getPointerCount();

		float x = event.getX(index);
		float y = event.getY(index);
		if (keyboardLocation == null) {
			keyboardLocation = new int[2];
			keyboard.getLocationOnScreen(keyboardLocation);
		}
		x += keyboardLocation[0];
		y += keyboardLocation[1];
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
			doKey(event.getPointerId(index) + 1, x, y, PRESS, pointerCount);
			lastTouchDownX = x;
			lastTouchDownY = y;
		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
			for (int i = 0; i < pointerCount; i++) {
				int historySize = event.getHistorySize();
				for (int h = 0; h < historySize; h++) {
					doKey(event.getPointerId(i) + 1, event.getHistoricalX(i, h) + keyboardLocation[0], Math.max(0, event.getHistoricalY(i, h) + keyboardLocation[1]), SLIDE, pointerCount);
				}
				doKey(event.getPointerId(i) + 1, event.getX(i) + keyboardLocation[0], Math.max(0, event.getY(i) + keyboardLocation[1]), SLIDE, pointerCount);
			}
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
			doKey(event.getPointerId(index) + 1, x, y, RELEASE, pointerCount);
		}
		return true;
	}

	private float initialX; // for pitch bend
	private float initialY; // for expression

	private final boolean[] keyPressed = new boolean[32];

	public void updateKeysPressed() {
		if (synth != null && ((Instrument)synth.getInstrument()) != null) {
			Keyboard keyboard = ((Instrument)synth.getInstrument()).getKeyboardModule();
			if (keyboard != null) {
				for (int i = 0; i < 32; i++) {
					if (!keyPressed[i] && keyboard.isPlaying(i + 48)) {
						getKeyForNote(i).setPressed(true);
						keyPressed[i] = true;
					} else if (keyPressed[i] && !keyboard.isPlaying(i + 48)) {
						getKeyForNote(i).setPressed(false);
						keyPressed[i] = false;
					}
				}
			}
		}
	}

	@SuppressLint("NewApi")
	private void doKey(int finger, float x, float y, int type, int fingers) {

		if (!isPointInsideView(x, y, keyboard)) {
			int[] location = new int[2];
			keyboard.getLocationInWindow(location);
			y = location[1] + 4;
		}

		// test black keys first, then white
		int key = -1;
		while (key == -1) {
			if (isPointInsideView(x, y, key2)) {
				key = 1;
			} else if (isPointInsideView(x, y, key4)) {
				key = 3;
			} else if (isPointInsideView(x, y, key7)) {
				key = 6;
			} else if (isPointInsideView(x, y, key9)) {
				key = 8;
			} else if (isPointInsideView(x, y, key11)) {
				key = 10;
			} else if (isPointInsideView(x, y, key14)) {
				key = 13;
			} else if (isPointInsideView(x, y, key16)) {
				key = 15;
			} else if (isPointInsideView(x, y, key19)) {
				key = 18;
			} else if (isPointInsideView(x, y, key21)) {
				key = 20;
			} else if (isPointInsideView(x, y, key23)) {
				key = 22;
			} else if (x <= 0 || isPointInsideView(x, y, key1)) {
				key = 0;
			} else if (isPointInsideView(x, y, key3)) {
				key = 2;
			} else if (isPointInsideView(x, y, key5)) {
				key = 4;
			} else if (isPointInsideView(x, y, key6)) {
				key = 5;
			} else if (isPointInsideView(x, y, key8)) {
				key = 7;
			} else if (isPointInsideView(x, y, key10)) {
				key = 9;
			} else if (isPointInsideView(x, y, key12)) {
				key = 11;
			} else if (isPointInsideView(x, y, key13)) {
				key = 12;
			} else if (isPointInsideView(x, y, key15)) {
				key = 14;
			} else if (isPointInsideView(x, y, key17)) {
				key = 16;
			} else if (isPointInsideView(x, y, key18)) {
				key = 17;
			} else if (isPointInsideView(x, y, key20)) {
				key = 19;
			} else if (isPointInsideView(x, y, key21)) {
				key = 20;
			} else if (isPointInsideView(x, y, key22)) {
				key = 21;
			} else if (isPointInsideView(x, y, key24)) {
				key = 23;
			} else if (isPointInsideView(x, y, key25)) {
				key = 24;
			} else if (isPointInsideView(x, y, key26)) {
				key = 25;
			} else if (isPointInsideView(x, y, key27)) {
				key = 26;
			} else if (isPointInsideView(x, y, key28)) {
				key = 27;
			} else if (isPointInsideView(x, y, key29)) {
				key = 28;
			} else if (isPointInsideView(x, y, key30)) {
				key = 29;
			} else if (isPointInsideView(x, y, key31)) {
				key = 30;
			} else if (isPointInsideView(x, y, key32)) {
				key = 31;
			} else {
				// fudge x a bit and try again
				x = x - 4;
			}
		}
		int note = key + 60 - 12;

		int[] coords = new int[2];
		keyboard.getLocationOnScreen(coords);
		float velocity = Math.min(1.0f, 4.0f - 4.0f * (y - coords[1]) / keyboard.getHeight());

		int voice = 0;

		if (type == PRESS) {
//				sendMidiNoteOn(0, note, (int)(127.0 * velocity));
			synth.notePress(note, velocity);
		} else if (type == RELEASE) {
//				sendMidiNoteOff(0, note, 0);
			synth.noteRelease(note);
		} else if (type == SLIDE) {
			if (lastNote[finger] != note) {
//					sendMidiNoteOff(0, lastNote[finger], 0);
				synth.noteRelease(lastNote[finger]);
//					sendMidiNoteOn(0, note, (int)(127.0 * velocity));
				synth.notePress(note, velocity);
			} else {
				synth.pressure(voice, velocity);
			}
		}
		lastNote[finger] = note;

		// if the action is up, remove the upped finger from the lastnote array
		if (type == RELEASE) {
			for (int i = finger; i < lastNote.length - 1; i++) {
				lastNote[finger] = lastNote[finger + 1];
			}
		}

		updateKeysPressed();
	}

	private View getKeyForNote(int note) {
		if (note <= 0) {
			return key1;
		} else if (note == 1) {
			return key2;
		} else if (note == 2) {
			return key3;
		} else if (note == 3) {
			return key4;
		} else if (note == 4) {
			return key5;
		} else if (note == 5) {
			return key6;
		} else if (note == 6) {
			return key7;
		} else if (note == 7) {
			return key8;
		} else if (note == 8) {
			return key9;
		} else if (note == 9) {
			return key10;
		} else if (note == 10) {
			return key11;
		} else if (note == 11) {
			return key12;
		} else if (note == 12) {
			return key13;
		} else if (note == 13) {
			return key14;
		} else if (note == 14) {
			return key15;
		} else if (note == 15) {
			return key16;
		} else if (note == 16) {
			return key17;
		} else if (note == 17) {
			return key18;
		} else if (note == 18) {
			return key19;
		} else if (note == 19) {
			return key20;
		} else if (note == 20) {
			return key21;
		} else if (note == 21) {
			return key22;
		} else if (note == 22) {
			return key23;
		} else if (note == 23) {
			return key24;
		} else if (note == 24) {
			return key25;
		} else if (note == 25) {
			return key26;
		} else if (note == 26) {
			return key27;
		} else if (note == 27) {
			return key28;
		} else if (note == 28) {
			return key29;
		} else if (note == 29) {
			return key30;
		} else if (note == 30) {
			return key31;
		} else if (note == 31) {
			return key32;
		} else {
			return key32;
		}
	}

	private boolean isPointInsideView(float x, float y, View view) {
		if (view.getVisibility() == View.GONE) {
			return false;
		}
		int location[] = new int[2];
		view.getLocationOnScreen(location);
		int viewX = location[0];
		int viewY = location[1];

		// point is inside view bounds
		if ((x > viewX && x < (viewX + view.getWidth())) && (y > viewY && y < (viewY + view.getHeight()))) {
			return true;
		} else {
			return false;
		}
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
						lastSelectedModView = ((ModuleViewer) module.getViewer(synth)).getView(MainActivity.this, modViewGroup);
						lastSelectedModView.setVisibility(View.VISIBLE);
					} else {
						noModSelectedText.setVisibility(View.VISIBLE);
					}
					synth.setScopeShowing(module instanceof Output);
				} else {
					synth.setScopeShowing(false);
				}
			}
		});
	}

	// --- MIDI ----

	public boolean midiDeviceAttached;

	@Override
	public void onDeviceAttached(UsbDevice usbDevice) {
		System.out.println("USB Device Attached: Vendor:" + usbDevice.getVendorId() + " Device: " + usbDevice.getDeviceId());
		keyboardPane.setVisibility(View.GONE);
		midiDeviceAttached = true;
	}

	@Override
	public void onDeviceDetached(UsbDevice usbDevice) {
		System.out.println("USB Device Detached: Vendor:" + usbDevice.getVendorId() + " Device: " + usbDevice.getDeviceId());
		keyboardPane.setVisibility(View.VISIBLE);
		midiDeviceAttached = false;
	}

	@Override
	public void onMidiMiscellaneousFunctionCodes(MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
	}

	@Override
	public void onMidiCableEvents(MidiInputDevice sender, int cable, int byte1, int byte2, int byte3) {
	}

	@Override
	public void onMidiSystemCommonMessage(MidiInputDevice sender, int cable, byte[] bytes) {
	}

	@Override
	public void onMidiSystemExclusive(MidiInputDevice sender, int cable, byte[] systemExclusive) {
	}

	@Override
	public void onMidiNoteOn(MidiInputDevice sender, int cable, int channel, int midinote, int midivelocity) {
		if (midivelocity == 0) {
			onMidiNoteOff(sender, cable, channel, midinote, midivelocity);
			return;
		}
		if (myMidiChannel != 0 && channel != myMidiChannel) {
			return;
		}
		//System.out.println("MIDI Note On: " + midinote + " velocity " + midivelocity);
		float velocity = FastMath.min(1.0f, (midivelocity + 1) / 128.0f); // TODO add a sensitivity option
		if (synth != null) {
			synth.notePress(midinote, velocity);
		}
	}

	@Override
	public void onMidiNoteOff(MidiInputDevice sender, int cable, int channel, int midinote, int midivelocity) {
		if (myMidiChannel != 0 && channel != myMidiChannel) {
			return;
		}
		//System.out.println("MIDI Note Off: " + midinote + " velocity " + midivelocity);
		if (synth != null) {
			synth.noteRelease(midinote);
		}
	}

	@Override
	public void onMidiPolyphonicAftertouch(MidiInputDevice sender, int cable, int channel, int midinote, int pressure) {
		if (myMidiChannel != 0 && channel != myMidiChannel) {
			return;
		}
		//System.out.println("MIDI Poly Aftertouch: " + midinote + " pressure " + pressure);
	}

	@Override
	public void onMidiControlChange(MidiInputDevice sender, int cable, int channel, int function, int value) {
		if (myMidiChannel != 0 && channel != myMidiChannel) {
			return;
		}
		//System.out.println("MIDI CC " + function + " " + value);
		if (function != 0 && synth != null) {
			synth.updateCC(function, value / 127.0f);
		}
		MidiControlDialog.controlChanged(this, function);

//		if (function == 0) { // ?
//		} else if (function == 1) { // modulation amount
//			// float currentVibratoAmount = progress * progress / 25000.0f;
//			synth.expression(value / 128.0f);
//		} else if (function == 2) { // breath controller
//			synth.pressure(0, value); // assumed monophonic
//		} else if (function == 3) { // chorus (pulse) width
//			// ((Instrument)synth.getInstrument()).chorusWidth = value / 128.0f;
//			// TODO determine what to do with pulse width
//			updateControls();
//		} else if (function == 7) { // overall volume
//			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//			am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) ((value / 128.0f) * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
//		} else if (function == 10) { // pan
//			// doesn't exist
//		} else if (function == 14) { // amp level
//			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//			am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) ((value / 128.0f) * am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
//		} else if (function == 16) { // vibrato rate
//			// synth.vibratoRate = value;
//			updateControls();
//		} else if (function == 17) { // tremelo rate (actually lfo2)
//			// doesn't exist
//		} else if (function == 18) { // vibrato amount
//			// synth.vibratoAmount = value / 128.0f;
//			updateControls();
//		} else if (function == 22) { // tremelo amount (actually lfo2)
//			// doesn't exist
//		} else if (function == 28) { // filter sustain
//			// synth.filterEnvSustain = value / 128.0f;
//			updateControls();
//		} else if (function == 29) { // filter release
//			// synth.filterEnvRelease = value;
//			updateControls();
//		} else if (function == 31) { // amp sustain
//			// synth.ampSustain = value / 128.0f;
//			updateControls();
//		} else if (function == 64) { // damper pedal
//			if (value > 0) {
//				synth.damper(true);
//			} else {
//				synth.damper(false);
//			}
//		} else if (function == 70) {
//
//		} else if (function == 71) { // filter resonance
//			// synth.filterResonance = value / 128.0f;
//			updateControls();
//		} else if (function == 72) { // amp release
//			// synth.ampRelease = value;
//			updateControls();
//		} else if (function == 73) { // amp attack
//			// synth.ampAttack = value;
//			updateControls();
//		} else if (function == 74) { // filter cutoff
//			// synth.filterLow = value / 128.0f;
//			updateControls();
//		} else if (function == 75) { // amp decay
//			// synth.ampDecay = value;
//			updateControls();
//		} else if (function == 76) { // vibrato rate
//			// synth.vibratoRate = value / 128.0f;
//			updateControls();
//		} else if (function == 77) { // vibrato amount
//			// synth.vibratoAmount = value / 128.0f;
//			updateControls();
//		} else if (function == 78) { // vibrato attack
//			// synth.vibratoAttack = value;
//			updateControls();
//		} else if (function == 81) { // filter depth
//			// synth.filterHigh = value / 128.0f;
//			updateControls();
//		} else if (function == 82) { // filter attack
//			// synth.filterEnvAttack = value;
//			updateControls();
//		} else if (function == 83) { // filter decay
//			// synth.filterEnvDecay = value;
//			updateControls();
//		} else if (function == 91) { // reverb amount
//			// synth.echoFeedback = value / 128.0f;
//			updateControls();
//		} else if (function == 93) { // chorus amount
//			// synth.echoAmount = value / 128.0f;
//			updateControls();
//		}
//
//		// Special commands
//		else if (function == 120) { // all sound off
//			synth.allSoundOff();
//		} else if (function == 121) { // reset all controllers
//			// not implemented
//		} else if (function == 122) { // local control
//			// not implemented
//		} else if (function == 123) { // all notes off
//			// todo
//		} else if (function == 124) { // omni mode off
//			// not supported
//		} else if (function == 125) { // omni mode on
//			// the default
//		} else if (function == 126) { // mono mode on
//			// todo
//		} else if (function == 127) { // poly mode on
//			// todo
//		}
	}

	@Override
	public void onMidiProgramChange(MidiInputDevice sender, int cable, int channel, int program) {
		if (myMidiChannel != 0 && channel != myMidiChannel) {
			return;
		}
		System.out.println("MIDI Program Change: " + program);
		loadProgram(program + 1);
	}

	@Override
	public void onMidiChannelAftertouch(MidiInputDevice sender, int cable, int channel, int midipressure) {
		if (myMidiChannel != 0 && channel != myMidiChannel) {
			return;
		}
		//System.out.println("MIDI Channel Aftertouch: " + channel + " " + midipressure);
		if (synth != null) {
			float pressure = FastMath.min(1.0f, (midipressure + 1) / 128.0f); // TODO add a sensitivity option
			synth.pressure(pressure);
		}
	}

	@Override
	public void onMidiPitchWheel(MidiInputDevice sender, int cable, int channel, int amount) {
		if (myMidiChannel != 0 && channel != myMidiChannel) {
			return;
		}
		if (synth != null) {
			synth.pitchBend((amount - 8192) / 8192.0f);
		}
	}

	@Override
	public void onMidiClock() {
		if (synth != null) {
			synth.midiclock();
		}
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

	/**
	 * Override the back button to prompt for quit.
	 */
	public boolean nKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !event.isAltPressed()) {
			final MessageDialog dialog = new MessageDialog(this, null, "Are you sure you want to quit?", new String[] { //
					Translator.getTranslator().translate("Yes"), //
					Translator.getTranslator().translate("No") }, null);
			dialog.show();
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface d) {
					int rc = dialog.getButtonPressed();
					if (rc == 0) {
						finish();
					}
				}
			});
			return true; // overriding the standard action handling
		}
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
			synth.notePress(note, velocity);
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
			synth.noteRelease(note);
		}
		return false;
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
//		int note = midinote - 60 + 12;
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
