package com.gallantrealm.modsynth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import com.gallantrealm.android.FileSelectorDialog;
import com.gallantrealm.mysynth.ClientModel;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class InstrumentSelectorDialog extends FileSelectorDialog {

	FTPClient ftpClient = new FTPClient();
	ClientModel clientModel = ClientModel.getClientModel();
	String hostname;
	String userid;
	String password;

	public InstrumentSelectorDialog(Activity activity, final String hostname, final String userid, final String password, String extension) {
		super(activity, extension);
		this.hostname = hostname;
		this.userid = userid;
		this.password = password;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView title = (TextView) findViewById(R.id.titleText);
		title.setText(ModSynthTranslator.getTranslator().translate("Instrument"));
	}

	@Override
	protected List<String> getFileNames(String folder) throws Exception {
		System.out.println("InstrumentSelectorDialog.getFileNames: " + folder);
		List<String> fileNames = new ArrayList<String>();
		if (folder.equals("")) {
			fileNames.add("BuiltIn/");
			if (clientModel.isGoggleDogPass()) {
				fileNames.add("Online/");
			}
			File filesDir = activity.getExternalFilesDir(null);
			if (filesDir != null) {
				File[] files = filesDir.listFiles();
				if (files != null) {
					Arrays.sort(files);
					for (int i = 0; i < files.length; i++) {
						String filename = files[i].getName();
						if (filename.indexOf(".modsynth") > 0 && filename.indexOf("gallant") < 0) {
							int synthpos = filename.indexOf(".modsynth");
							if (synthpos >= 0) {
								String name = filename.substring(0, synthpos).trim();
								fileNames.add(name);
							}
						}
					}
				}
			}
		} else if (folder.startsWith("BuiltIn/")) {
			String path = folder.substring("BuiltIn".length());
			path = "Instruments" + path;
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			System.out.println(path);
			String[] assets = activity.getAssets().list(path);
			for (int i = 0; i < assets.length; i++) {
				String filename = assets[i];
				if (filename.indexOf(".") <= 0) { // folder
					fileNames.add(filename + "/");
				} else {
					int synthpos = filename.indexOf(".modsynth");
					if (synthpos >= 0) {
						String name = filename.substring(0, synthpos);
						if (!filename.contains("+") || clientModel.isFullVersion() || clientModel.isGoggleDogPass()) { // add plus only on full version
							fileNames.add(name);
						}
					}
				}
			}
		} else if (folder.startsWith("Online/")) {
			folder = folder.substring("Online/".length());
			folder = "/ModSynth/" + folder;
			System.out.println("FTP LISTFILES FOR " + folder);
			connectIfNeeded();
			ftpClient.changeWorkingDirectory(folder);
			System.out.println("LISTING FILES FOR " + folder);
			FTPFile[] files = ftpClient.listFiles();
			for (FTPFile file : files) {
				if (!file.getName().startsWith(".")) {
					if (file.isDirectory()) {
						fileNames.add(file.getName() + "/");
					} else if (extension == null) {
						fileNames.add(file.getName());
					} else if (file.getName().endsWith(extension)) {
						String name = file.getName().substring(0, file.getName().indexOf(extension));
						if (name.contains("(")) {
							name = name.substring(0, name.indexOf("("));
						}
						fileNames.add(name);
					}
				}
			}
			System.out.println("" + files.length + " FILES");
			Collections.sort(fileNames, new Comparator<String>() {
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
		}
		return fileNames;
	}

	private void connectIfNeeded() throws Exception {
		if (!ftpClient.isConnected()) {
			System.out.println("CONNECTING TO " + hostname + " AS " + userid);
			int retries = 0;
			while (retries < 4) {
				try {
					ftpClient.connect(hostname);
					ftpClient.enterLocalPassiveMode();
					ftpClient.login(userid, password);
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					ftpClient.setKeepAlive(true);
					return;
				} catch (Exception e) {
					if (retries >= 4) {
						e.printStackTrace();
						return;
					}
					Thread.sleep(500);
					retries++;
				}
			}
		}
	}

	public InputStream getFileInputStream(String filename) throws IOException {
		if (filename.startsWith("BuiltIn/")) {
			filename = filename.substring("BuiltIn/".length()).trim();
			filename = "Instruments/" + filename;
			InputStream is = activity.getAssets().open(filename);
			return is;
		} else if (filename.startsWith("Online/")) {
			filename = filename.substring("Online/".length());
			filename = "/ModSynth/" + filename;
			try {
				connectIfNeeded();
				return ftpClient.retrieveFileStream(filename);
			} catch (Exception e) {
				return null;
			}
		} else { // if (filename.startsWith("/Custom")) {
			File file = new File(getContext().getExternalFilesDir(null), filename);
			return new FileInputStream(file);
		}
	}

	protected void completeFileRead() {
		try {
			ftpClient.completePendingCommand();
		} catch (Exception e) {
			// can happen if the file is local
		}
	}

}
