package com.gallantrealm.modsynth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class SoundContentProvider extends ContentProvider {

	private static final String AUTHORITY = "com.gallantrealm.modsynth";

	public SoundContentProvider() {
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0; // not supported
	}

	@Override
	public String getType(Uri uri) {
		return "application/vnd.gallantrealm.modsynth";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null; // not supported
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0; // not supported
	}

	@Override
	public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
		return new String[] { "application/vnd.gallantrealm.modsynth" };
	}

//	@Override
//	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
//		AssetManager am = getContext().getAssets();
//		String file_name = uri.getLastPathSegment();
//		if (file_name == null)
//			throw new FileNotFoundException();
//		AssetFileDescriptor afd = null;
//		try {
//			afd = am.openFd(file_name);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return afd;//super.openAssetFile(uri, mode);
//	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		String fileName = uri.getEncodedPath().substring(1);
		System.out.println("SoundContentProvider.openFile: " + fileName);
		if (fileName.equals("sounds")) {
			// make a file that has a string array of all sound names
			File soundsFile = new File(getContext().getFilesDir(), "sounds");
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(soundsFile));
				ArrayList<String> sounds = getSoundsList();

				outStream.writeObject(sounds);
				ParcelFileDescriptor pfd = ParcelFileDescriptor.open(soundsFile, ParcelFileDescriptor.MODE_READ_ONLY);
				return pfd;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			// get the particular sound file.  Create the (internal) file 
			// from the asset if necessary (since assets are compressed and can't be returned)
			try {
				File file = new File(getContext().getFilesDir(), fileName);
				if (!file.exists()) {
					InputStream inputStream = getContext().getAssets().open(fileName);
					OutputStream outputStream = new FileOutputStream(file);
					int b = inputStream.read();
					while (b != -1) {
						outputStream.write(b);
						b = inputStream.read();
					}
					inputStream.close();
					outputStream.close();
				}
				ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(file));
				Object object = inStream.readObject();
				inStream.close();
				ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
				System.out.println("Returning " + fileName + " internal file.");
				return pfd;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("File " + fileName + " was not found.");
		return null;
	}

	private ArrayList<String> getSoundsList() {
		ArrayList<String> soundsList = new ArrayList<String>();
		try {
			String[] assets = getContext().getAssets().list("");
			for (int i = 0; i < assets.length; i++) {
				String filename = assets[i];
				int synthpos = filename.indexOf(".modsynth");
				if (synthpos >= 0) {
					String name = filename.substring(0, synthpos);
					soundsList.add(name);
				}
			}
			File filesDir = getContext().getFilesDir();
			File[] files = filesDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				String filename = files[i].getName();
				boolean found = false;
				for (int j = 0; j < assets.length; j++) {
					if (filename.equals(assets[j])) {
						found = true;
					}
				}
				if (!found) {
					int synthpos = filename.indexOf(".modsynth");
					if (synthpos >= 0) {
						String name = filename.substring(0, synthpos);
						if (!soundsList.contains(name)) {
							soundsList.add(name);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return soundsList;
	}
}
