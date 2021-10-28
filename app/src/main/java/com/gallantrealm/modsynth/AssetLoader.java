package com.gallantrealm.modsynth;

import java.io.IOException;
import java.io.InputStream;
import android.app.Activity;
import android.content.Context;

public final class AssetLoader {
	
	public static InputStream loadAsset(String assetName) throws IOException {
		Context context = ClientModel.getClientModel().getContext();
		return context.getAssets().open(assetName);
	}
	
	public static long getAssetLength(String assetName) throws IOException {
		Context context = ClientModel.getClientModel().getContext();
		return context.getAssets().openFd(assetName).getLength();
	}
}
