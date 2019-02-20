package com.gallantrealm.modsynth;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabHelper.OnIabPurchaseFinishedListener;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Purchase;
import com.gallantrealm.android.MessageDialog;
import com.gallantrealm.android.themes.DefaultTheme;
import com.gallantrealm.android.themes.Theme;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import uk.co.labbookpages.WavFile;

public class ClientModel {

	public static final int FULLVERSION = 0;
	public static final int GOOGLE = 1;
	public static final int AMAZON = 2;
	public static final int FREEVERSION = 3;

	public static final int market = GOOGLE;

	private static ClientModel clientModel;

	public static ClientModel getClientModel() {
		if (clientModel == null) {
			clientModel = new ClientModel();
		}
		return clientModel;
	}

	private String instrumentName;
	private String backgroundName;
	private String customBackgroundPath;
	private Activity context;
	private boolean fullVersion;
	private int playCount;
	private int preferencesVersion; // 0=2013-2014, 1=2015, 2=10/17/2018
	private int keyboardSize;
	private int midiChannel;
	private int sampleRateReducer;
	private int tuningCents;
	private int controlSide;
	private int colorIcons;
	private int nbuffers;
	private int language;
	private boolean preferAndroidMidi;

	public boolean goggleDogPass;

	IabHelper purchaseHelper;

	private List<ClientModelChangedListener> listeners;

	private ClientModel() {
		listeners = new ArrayList<ClientModelChangedListener>();
	}

	public void loadPreferences(Activity context) {
		this.context = context;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		preferencesVersion = preferences.getInt("preferencesVersion", 2);
		if (preferencesVersion <= 1) { // old prefs that used myworld names
			backgroundName = preferences.getString("avatarName", null);
			instrumentName = preferences.getString("worldName", null);
			fullVersion = preferences.getBoolean("fullVersion", false);
			playCount = preferences.getInt("playCount", 0);
			keyboardSize = preferences.getInt("score0", 0);
			midiChannel = preferences.getInt("score1", 0);
			sampleRateReducer = preferences.getInt("score2", 0);
			tuningCents = preferences.getInt("score3", 0);
			controlSide = preferences.getInt("score4", 0);
			colorIcons = preferences.getInt("score5", 0);
			nbuffers = preferences.getInt("score6", 0);
			language = preferences.getInt("score7", 0);
			customBackgroundPath = preferences.getString("avatarDisplayName0", "");
		} else { // new modsynth names
			backgroundName = preferences.getString("backgroundName", preferences.getString("avatarName", null));
			instrumentName = preferences.getString("instrumentName", preferences.getString("worldName", null));
			fullVersion = preferences.getBoolean("fullVersion", false);
			playCount = preferences.getInt("playCount", 0);
			keyboardSize = preferences.getInt("keyboardSize", 0);
			midiChannel = preferences.getInt("midiChannel", 0);
			sampleRateReducer = preferences.getInt("sampleRateReducer", 0);
			tuningCents = preferences.getInt("tuningCents", 0);
			controlSide = preferences.getInt("controlSide", 0);
			colorIcons = preferences.getInt("colorIcons", 0);
			nbuffers = preferences.getInt("nbuffers", 0);
			language = preferences.getInt("language", 0);
			preferAndroidMidi = preferences.getBoolean("preferAndroidMidi", true);
			customBackgroundPath = preferences.getString("customBackgroundPath", "");
		}
	}

	public void setContext(Activity context) {
		// if (this.context != null && purchaseObserver != null) {
		// ResponseHandler.unregister(purchaseObserver);
		// purchaseObserver = null;
		// }
		this.context = context;
		if (purchaseHelper == null) {
			try {
				purchaseHelper = new IabHelper(context, context.getString(R.string.googleappkey));
				purchaseHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					public void onIabSetupFinished(IabResult result) {
					}
				});
			} catch (Exception e) {
				System.out.println("No in-app purchase, the app isn't setup correctly for it: " + e.getMessage());
			}
		}

	}

	public Activity getContext() {
		return context;
	}

	public void savePreferences(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("preferencesVersion", 2);
		editor.putString("backgroundName", backgroundName);
		editor.putString("instrumentName", instrumentName);
		if (fullVersion) {
			editor.putBoolean("fullVersion", fullVersion);
		}
		editor.putInt("playCount", playCount);
		editor.putInt("keyboardSize", keyboardSize);
		editor.putInt("midiChannel", midiChannel);
		editor.putInt("sampleRateReducer", sampleRateReducer);
		editor.putInt("tuningCents", tuningCents);
		editor.putInt("controlSide", controlSide);
		editor.putInt("colorIcons", colorIcons);
		editor.putInt("nbuffers", nbuffers);
		editor.putInt("language", language);
		editor.putBoolean("preferAndroidMidi", preferAndroidMidi);
		editor.putString("customBackgroundPath", customBackgroundPath);
		editor.commit();
	}

	public int getPlayCount() {
		return playCount;
	}

	public void updatePlayCount(Context context) {
		playCount = playCount + 1;
		savePreferences(context);
	}

	public String getInstrumentName() {
		return this.instrumentName;
	}

	public void setInstrumentName(String worldName) {
		this.instrumentName = worldName;
	}

	public void setBackgroundName(String name) {
		backgroundName = name;
	}

	public String getBackgroundName() {
		return backgroundName;
	}

	public String getCustomBackgroundPath() {
		return customBackgroundPath;
	}

	public void setCustomBackgroundPath(String path) {
		customBackgroundPath = path;
	}

	public boolean isScreenOn() {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}

	public void setKeyboardSize(int score) {
		keyboardSize = score;
	}

	public int getKeyboardSize() {
		return keyboardSize;
	}

	public void setMidiChannel(int score) {
		midiChannel = score;
	}

	public int getMidiChannel() {
		return midiChannel;
	}

	public void setSampleRateReducer(int score) {
		sampleRateReducer = score;
	}

	public int getSampleRateReducer() {
		return sampleRateReducer;
	}

	public void setTuningCents(int score) {
		tuningCents = score;
	}

	public int getTuningCents() {
		return tuningCents;
	}

	public void setControlSide(int score) {
		controlSide = score;
	}

	public int getControlSide() {
		return controlSide;
	}

	public void setColorIcons(int score) {
		colorIcons = score;
	}

	public int getColorIcons() {
		return colorIcons;
	}

	public void setNBuffers(int score) {
		nbuffers = score;
	}

	public int getNBuffers() {
		return nbuffers;
	}

	public void setLanguage(int score) {
		language = score;
	}

	public int getLanguage() {
		return language;
	}
	
	public void setPreferAndroidMidi(boolean preferAndroidMidi) {
		this.preferAndroidMidi = preferAndroidMidi;
	}
	public boolean isPreferAndroidMidi() {
		return preferAndroidMidi;
	}

	public boolean isFullVersion() {
		try {
			return fullVersion // actually marked a full version
					|| (!isGoogle() && !isAmazon()) // is not a market that supports in-app purchase
			;
		} catch (Exception e) { // a problem figuring out above
			return true; // free!
		}
	}

	static final int RC_REQUEST = 10001;
	static final String SKU_FULL_VERSION = "fullversion";
	// static final String SKU_FULL_VERSION = "android.test.purchase";
	static final String PAYLOAD = "DontSteal.IWorkedHardToMakeThisApp";

	public void buyFullVersion() {
		try {
			purchaseHelper.flagEndAsync();
			purchaseHelper.launchPurchaseFlow(context, SKU_FULL_VERSION, IabHelper.ITEM_TYPE_SUBS, RC_REQUEST, new OnIabPurchaseFinishedListener() {
				public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
					if (purchaseHelper == null) { // shutdown
						return;
					}
					if (result.isFailure()) {
						System.err.println("Purchase Failed: " + result.getMessage());
						// new MessageDialog(context, "Purchase Failed", "There was an error purchasing. Check your internet connection and try again later.", null).show();
						// Note: No need to show an error as Google Play's error dialog is better.
						return;
					}
					setFullVersion(true);
					savePreferences(context);
					new MessageDialog(context, "Purchase Success", "Thanks for purchasing!  Restart the app to enable all features and remove ads.", null).show();
				}
			}, PAYLOAD);
		} catch (Exception e) {
			e.printStackTrace();
			new MessageDialog(context, "Purchase Failed", "There was an error launching Google Play for purchasing.  Please make sure Google Play is installed and working.", null).show();
		}
	}

	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
		if (purchaseHelper != null) {
			try {
				purchaseHelper.handleActivityResult(requestCode, resultCode, data);
			} catch (Exception e) {
			}
		}
		return false;
	}

	public boolean isGoggleDogPass() {
		return goggleDogPass;
	}

	public void setGoggleDogPass(boolean pass) {
		goggleDogPass = pass;
	}

	public boolean isGoogle() {
		return market == GOOGLE;
	}

	public boolean isAmazon() {
		return market == AMAZON;
	}

	public boolean isFree() {
		return market == FREEVERSION;
	}

	public boolean isChromebook() {
		return context.getPackageManager().hasSystemFeature("org.chromium.arc.device_management");
	}

	public void setFullVersion(boolean fullVersion) {
		this.fullVersion = fullVersion;
	}

	private String themeName = "com.gallantrealm.mysynth.themes.DefaultTheme";
	private Theme theme = new DefaultTheme();

	public void setThemeName(String themeName) {
		this.themeName = themeName;
		try {
			this.theme = (Theme) this.getClass().getClassLoader().loadClass(themeName).newInstance();
			typeface = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getThemeName() {
		return themeName;
	}

	public Theme getTheme() {
		return theme;
	}

	private Typeface typeface;

	public Typeface getTypeface(Context context) {
		if (typeface == null) {
			try {
				String font = getTheme().font;
				typeface = Typeface.createFromAsset(context.getAssets(), font);
			} catch (Throwable e) {
				System.err.println("Could not create typeface for app.");
			}
		}
		return typeface;
	}

	/**
	 * Loads a bitmap for editing, such as in the eggworld decorator
	 * 
	 * @param fileName
	 * @return
	 */
	public Bitmap loadBitmap(String fileName) {
		Bitmap bitmap = null;
		try {
			File file = new File(getContext().getFilesDir(), fileName);
			if (!file.exists()) {
				// create a new bitmap, set to white
				bitmap = Bitmap.createBitmap(512, 512, Config.RGB_565);
				for (int i = 0; i < 512; i++) {
					for (int j = 0; j < 512; j++) {
						bitmap.setPixel(i, j, 0xFFFFFFFF);
					}
				}
				saveBitmap(bitmap, fileName);
			}
			InputStream inStream = new BufferedInputStream(new FileInputStream(file), 65536);
			Bitmap tbitmap = BitmapFactory.decodeStream(inStream);
			inStream.close();
			bitmap = tbitmap.copy(tbitmap.getConfig(), true);
			tbitmap.recycle();
			// bitmap = tbitmap;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * Saves a bitmap to app-local files.
	 * 
	 * @param bitmap
	 * @param fileName
	 */
	public void saveBitmap(Bitmap bitmap, String fileName) {
		try {
			File file = new File(getContext().getFilesDir(), fileName);
			if (file.exists()) {
				file.delete();
			}
			OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file), 65536);
			bitmap.compress(CompressFormat.PNG, 100, outStream);
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a RAW wave file into a wave table
	 * 
	 * @param fileName
	 * @return
	 */
	public double[] loadWave(String fileName, boolean external) throws Exception {
		System.out.println("Loading wav file: " + fileName);
		File file;
		if (fileName.startsWith("file:")) { // via an external url
			file = new File(fileName.substring(7));
		} else if (fileName.startsWith("/")) { // full path file
			file = new File(fileName);
		} else { // within the application
			if (external) {
				try {
					file = new File(getContext().getExternalFilesDir(null), fileName);
				} catch (Exception e) {
					file = new File(getContext().getFilesDir(), fileName);
				}
			} else {
				file = new File(getContext().getFilesDir(), fileName);
			}
		}
		InputStream is;
		long len;
		if (file.exists()) {
			is = new FileInputStream(file);
			len = file.length();
		} else {
			// if file not found, perhaps it has a built-in replacement, so try assets
			System.out.println("Looking for wav in assets");
			file = null;
			fileName = trimName(fileName);
			is = context.getAssets().open(fileName);
			len = context.getAssets().openFd(fileName).getLength();
		}
		WavFile wavFile = WavFile.openWavFile(is, len);
		int waveLength = Math.min((int) wavFile.getNumFrames(), 1000000);
		double[] wave = new double[waveLength];
		wavFile.readFramesMono(wave, waveLength);
		return wave;
	}

	private String trimName(String sampleName) {
		if (sampleName.lastIndexOf("/") >= 0) {
			return sampleName.substring(sampleName.lastIndexOf("/") + 1);
		} else {
			return sampleName;
		}
	}

	public String findObject(String prefix, String suffix) {
		File filesDir = context.getExternalFilesDir(null);
		if (filesDir != null) {
			File[] files = filesDir.listFiles();
			if (files != null) {
				Arrays.sort(files);
				for (int i = 0; i < files.length; i++) {
					String filename = files[i].getName();
					if (filename.startsWith(prefix) && filename.endsWith(suffix) && filename.indexOf("gallant") < 0) {
						int synthpos = filename.indexOf(".modsynth");
						if (synthpos >= 0) {
							String name = filename.substring(0, synthpos).trim();
							return name;
						}
					}
				}
			}
		}
		return null;
	}

	public Object loadObject(String fileName) {
		return loadObject(fileName, false);
	}

	/**
	 * Loads a serializable object from a file.
	 * 
	 * @param fileName
	 * @return
	 */
	public Object loadObject(String fileName, boolean external) {
		Object object = null;
		if (fileName.startsWith("file:")) { // via an external url
			try {
				File file = new File(fileName.substring(7));
				object = deserializeObject(new FileInputStream(file));
			} catch (Exception e) {
			}
		} else { // within the application
			if (external && getContext().getExternalFilesDir(null) != null) { // external file
				try {
					File file = new File(getContext().getExternalFilesDir(null), fileName);
					object = deserializeObject(new FileInputStream(file));
				} catch (Exception e) {
				}
			}
			if (object == null) { // internal file
				try {
					File file = new File(getContext().getFilesDir(), fileName);
					object = deserializeObject(new FileInputStream(file));
				} catch (Exception e) {
				}
			}
			// if file not found, it is a built-in. so try asset
			if (object == null) {
				try {
					InputStream is = context.getAssets().open(fileName.trim());
					object = deserializeObject(is);
				} catch (Exception e) {
				}
			}
		}
		return object;
	}

	public void deleteObject(String fileName) {
		deleteObject(fileName, false);
	}

	public void deleteObject(String fileName, boolean external) {
		try {
			File file;
			if (fileName.startsWith("file:")) { // an external file
				file = new File(fileName.substring(7));
			} else { // within the application
				if (external && getContext().getExternalFilesDir(null) != null) {
					file = new File(getContext().getExternalFilesDir(null), fileName);
				} else {
					file = new File(getContext().getFilesDir(), fileName);
				}
			}
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves an object to app-local files. The object needs to be serializable
	 * 
	 * @param bitmap
	 * @param fileName
	 */
	public void saveObject(Object object, String fileName, boolean external) {
		boolean saved = false;
		if (external && getContext().getExternalFilesDir(null) != null) {
			try {
				File file = new File(getContext().getExternalFilesDir(null), fileName);
				if (file.exists()) {
					file.delete();
				}
				serializeObject(object, new FileOutputStream(file));
				saved = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!saved) {
			try {
				File file = new File(getContext().getFilesDir(), fileName);
				if (file.exists()) {
					file.delete();
				}
				serializeObject(object, new FileOutputStream(file));
				saved = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Exports an object to sdcard storage.
	 */
	public File exportObject(Object object, String fileName) {
		File file;
		try {
			File appDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getApplicationInfo().packageName);
			if (!appDir.exists()) {
				appDir.mkdir();
			}
			file = new File(appDir, fileName);
			if (file.exists()) {
				file.delete();
			}
			serializeObject(object, new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return file;
	}

	private void serializeObject(Object object, OutputStream os) throws Exception {
		ObjectOutputStream outStream = new ObjectOutputStream(os);
		try {
			outStream.writeObject(object);
		} finally {
			outStream.close();
		}
	}

	private Object deserializeObject(InputStream is) throws Exception {
		Object object = null;
		ObjectInputStream inStream = new ObjectInputStream(is);
		try {
			object = inStream.readObject();
		} finally {
			inStream.close();
		}
		return object;
	}

	public void addClientModelChangedListener(ClientModelChangedListener listener) {
		listeners.add(listener);
	}

	public void removeClientModelChangedListener(ClientModelChangedListener listener) {
		listeners.remove(listener);
	}

	public void fireClientModelChanged(final int changeType) {
		ClientModelChangedEvent event = new ClientModelChangedEvent(changeType);
		Iterator<ClientModelChangedListener> iterator = listeners.iterator();
		while (iterator.hasNext()) {
			ClientModelChangedListener listener = iterator.next();
			listener.clientModelChanged(event);
		}
	}

}
