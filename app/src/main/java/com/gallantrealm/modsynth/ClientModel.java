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
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
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
import android.util.Log;

import androidx.annotation.NonNull;

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
	private Context context;
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

	public boolean goggleDogPass;

	private List<ClientModelChangedListener> listeners;

	private ClientModel() {
		listeners = new ArrayList<ClientModelChangedListener>();
	}

	public void loadPreferences() {
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
			customBackgroundPath = preferences.getString("customBackgroundPath", "");
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public void savePreferences() {
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
		editor.putString("customBackgroundPath", customBackgroundPath);
		editor.commit();
	}

	public int getPlayCount() {
		return playCount;
	}

	public void updatePlayCount() {
		playCount = playCount + 1;
		savePreferences();
	}

	public String getInstrumentName() {
		return this.instrumentName;
	}

	public void setInstrumentName(String name) {
		this.instrumentName = name;
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

	BillingClient billingClient;
	boolean billingClientConnected;

	public void buyFullVersion(Activity activity) {
		try {
	//		if (billingClient == null) {
				Log.d("ClientModel", "initializing billing client");
				billingClient = BillingClient.newBuilder(this.context).setListener(new PurchasesUpdatedListener() {
					public void onPurchasesUpdated(final BillingResult billingResult, final List<Purchase> purchases) {
						Log.d("ClientModel", ">> onPurchasesUpdated");
						if (billingResult.getResponseCode() == BillingResponseCode.OK  && purchases != null && purchases.size() >= 1) {
							activity.runOnUiThread(new Runnable() {
								public void run() {
									setFullVersion(true);
									savePreferences();
									Purchase purchase = purchases.get(0);
									AcknowledgePurchaseParams acknowledgePurchaseParams =
							                AcknowledgePurchaseParams.newBuilder()
							                    .setPurchaseToken(purchase.getPurchaseToken())
							                    .build();
									billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
										public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
											Log.d("ClientModel", "Purchase acknowledged: "+billingResult.getResponseCode());
										}
									});
									if (context != null) {
										new MessageDialog(context, "Purchase Success", "Thanks for purchasing!  Restart the app to enable all features.", null).show();
									}
								}
							});
						} else if (billingResult.getResponseCode() == BillingResponseCode.ITEM_ALREADY_OWNED) {
							activity.runOnUiThread(new Runnable() {
								public void run() {
									setFullVersion(true);
									savePreferences();
									if (context != null) {
										new MessageDialog(context, "Purchase Success", "You already own the full version!  Restart the app to enable all features.", null).show();
									}
								}
							});
						} else if (billingResult.getResponseCode() == BillingResponseCode.USER_CANCELED) {
							// nothing to do.. user cancelled
						} else {
							Log.d("ClientModel", "Purchase Failed: " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
//							// Note: No need to show an error as Google Play's error dialog is better.
						}
						Log.d("ClientModel", "<< onPurchasesUpdated");
					}
				}).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
						.enableAutoServiceReconnection()
						.build();
	//		}
//			if (!billingClientConnected) {
				Log.d("ClientModel", "connecting to google play billing");
				billingClient.startConnection(new BillingClientStateListener() {
					public void onBillingSetupFinished(BillingResult arg0) {
						billingClientConnected = true;
						queryProductDetails(activity);
					}
					public void onBillingServiceDisconnected() {
						billingClientConnected = false;
					}
				});
//			} else {
//				querySkuDetails();
//			}
		} catch (Exception e) {
			e.printStackTrace();
			if (context != null) {
				new MessageDialog(context, "Purchase Failed", "There was an error launching Google Play for purchasing.  Please make sure Google Play is installed and working.", null).show();
			}
		}
	}

	public void restartApp() {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(intent);

			// Finish the current activity so the user can't "back" into the old state
			if (context instanceof Activity) {
				((Activity) context).finish();
			}

			// Optional: Kill the current process to ensure a total cold start
			Runtime.getRuntime().exit(0);
		}
	}

	private void queryProductDetails(Activity activity) {
		Log.d("ClientModel", "querying sku details");
		List<QueryProductDetailsParams.Product> productList = new ArrayList<QueryProductDetailsParams.Product>();
		productList.add(
				QueryProductDetailsParams.Product.newBuilder()
						.setProductId(SKU_FULL_VERSION)
						.setProductType(BillingClient.ProductType.INAPP)
						.build()
		);
		QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
						.setProductList(productList)
						.build();
		billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
			@Override
			public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull QueryProductDetailsResult queryProductDetailsResult) {
				if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
					ProductDetails productDetails = queryProductDetailsResult.getProductDetailsList().get(0);
					Log.d("ClientModel", "product details:");
					Log.d("ClientModel", "  Title: " + productDetails.getTitle());
					Log.d("ClientModel", "  Description: " + productDetails.getDescription());
					activity.runOnUiThread(new Runnable() {
						public void run() {
							launchPurchaseFlow(activity, productDetails);
						}
					});
				} else {
					Log.d("ClientModel", "Purchase Failed: " + billingResult.getDebugMessage());
					activity.runOnUiThread(new Runnable() {
						public void run() {
							if (context != null) {
								new MessageDialog(context, "Purchase Failed", billingResult.getDebugMessage(), null).show();
							}
						}
					});
				}
			}
		});
	}

	private void launchPurchaseFlow(Activity activity, ProductDetails productDetails) {
		try {
			Log.d("ClientModel", "launching purchase flow");
			ArrayList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
			productDetailsParamsList.add(
					BillingFlowParams.ProductDetailsParams.newBuilder()
							.setProductDetails(productDetails)
							.build()
			);
			BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
					.setProductDetailsParamsList(productDetailsParamsList).build();
			billingClient.launchBillingFlow(activity, billingFlowParams);
		} catch (Exception e) {
			e.printStackTrace();
			activity.runOnUiThread(new Runnable() {
				public void run() {
					if (context != null) {
						new MessageDialog(context, "Purchase Failed", "There was an error launching Google Play for purchasing.  Please make sure Google Play is installed and working.", null).show();
					}
				}
			});
		}
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
