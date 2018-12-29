package com.gallantrealm.modsynth.viewer;

import java.io.IOException;
import java.net.URLDecoder;
import com.gallantrealm.android.FileUtils;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.PCM;
import com.gallantrealm.mysynth.MessageDialog;
import com.gallantrealm.mysynth.SelectItemDialog;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;

public class PCMViewer extends ModuleViewer {

	PCM module;

	public PCMViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (PCM) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		canvas.drawCircle(x, y, 30, diagramPaint);
		path.reset();
		path.moveTo(x - 20, y - 20);
		path.lineTo(x + 20, y + 20);
		path.moveTo(x + 20, y - 20);
		path.lineTo(x - 20, y + 20);
		path.moveTo(x, y - 30);
		path.lineTo(x, y + 30);
		path.moveTo(x - 30, y);
		path.lineTo(x + 30, y);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.pcmpane;
	}

	private transient Button fileButton;
	private transient Spinner patchSpinner;

	private static class BuiltInSample {
		public static String getStaticName() {
			return "BuiltIn";
		};
		public static int getStaticImageResource() {
			return R.drawable.pcm;
		}
	}
	private static class CustomSample {
		public static String getStaticName() {
			return "Custom";
		};
		public static int getStaticImageResource() {
			return R.drawable.pcm;
		}
	};

	@Override
	public void onViewCreate(final MainActivity mainActivity) {
		fileButton = (Button) view.findViewById(R.id.pcmFile);
		patchSpinner = (Spinner) view.findViewById(R.id.pcmPatch);
		final SeekBar pcmOctave = (SeekBar) view.findViewById(R.id.pcmOctave);
		final SeekBar pcmPitch = (SeekBar) view.findViewById(R.id.pcmPitch);
		final SeekBar pcmDetune = (SeekBar) view.findViewById(R.id.pcmDetune);
		final View pcmMainPane = view.findViewById(R.id.pcmMain);

		fileButton.setOnClickListener(new View.OnClickListener() {
			@TargetApi(23)
			@SuppressLint("NewApi")
			public void onClick(View v) {

				final SelectItemDialog selector = new SelectItemDialog(mainActivity, "", new Class[] { //
						BuiltInSample.class, CustomSample.class //
				}, null);
				selector.show();
				selector.setOnDismissListener(new Dialog.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						Class clazz = selector.getItemSelected();
						if (clazz == BuiltInSample.class) {
							openSample("BuiltIn.sf2");
						} else if (clazz == CustomSample.class) {
							// for freesound
//						if (sampleType == SampleType.Freesound) {
//							if (freesoundDialog == null) {
//								freesoundDialog = new FreesoundDialog(view.getRootView().getContext());
//								freesoundDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//									public void onDismiss(DialogInterface dialog) {
//										// 
//									}
//								});
//							}
//							freesoundDialog.show();

							if (Build.VERSION.SDK_INT >= 23 && mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
								mainActivity.requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, MainActivity.REQUEST_PERMISSION_READ_PCM_EXTERNAL_STORAGE);
								return;
							}

							onContinuePCMSelect(mainActivity);
						}
					}
				});
			}
		});
		if (module.sampleName != null) {
			String trim = trimName(module.sampleName);
			if (trim.lastIndexOf(".") > 0) {
				fileButton.setText(trim.substring(0, trim.lastIndexOf(".")));
			} else {
				fileButton.setText(trim);
			}
		}
		ArrayAdapter<CharSequence> patchAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, module.patchNames);
		patchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		patchSpinner.setAdapter(patchAdapter);
		patchSpinner.setSelection(module.patchNum);
		patchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.patchNum != patchSpinner.getSelectedItemPosition()) {
					module.patchNum = patchSpinner.getSelectedItemPosition();
					instrument.moduleUpdated(module);
					try {
						module.loadSample();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		pcmOctave.setProgress(module.octave + 5);
		pcmOctave.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.octave = progress - 5;
				instrument.moduleUpdated(module);
			}
		});
		((View) pcmOctave.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.octaveCC));

		pcmPitch.setProgress(module.pitch);
		pcmPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.pitch = progress;
				instrument.moduleUpdated(module);
			}
		});
		((View) pcmPitch.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.pitchCC));

		pcmDetune.setProgress((int) (module.detune + 50));
		pcmDetune.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.detune = progress - 50;
				instrument.moduleUpdated(module);
			}
		});
		((View) pcmDetune.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.detuneCC));

		CheckBox loopCheckBox = (CheckBox) view.findViewById(R.id.pcmLoop);
		loopCheckBox.setChecked(module.loop);
		loopCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.loop = isChecked;
				// need to update the voice samples as well
				for (int i = 0; i < module.notes.length; i++) {
					if (module.notes[i] != null) {
						if (module.notes[i].sampleL != null) {
							module.notes[i].sampleL.mode = module.loop ? 1 : 0;
						}
						if (module.notes[i].sampleR != null) {
							module.notes[i].sampleR.mode = module.loop ? 1 : 0;
						}
					}
				}
			}
		});

		showSampleDetails();
	}

	public void onContinuePCMSelect(MainActivity mainActivity) {
		Intent intent = new Intent();
		intent.setType("*/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		mainActivity.startActivityForResult(Intent.createChooser(intent, Translator.getTranslator().translate("Select a WAV or SF2 file using")), 0);
	}

	private void showSampleDetails() {
		View loopView = view.findViewById(R.id.pcmLoopView);
		View patchView = view.findViewById(R.id.pcmPatchView);
		if (module.sampleName != null && module.sampleName.toLowerCase().endsWith(".sf2")) {
			loopView.setVisibility(View.GONE);
			patchView.setVisibility(View.VISIBLE);
		} else {
			loopView.setVisibility(View.VISIBLE);
			patchView.setVisibility(View.GONE);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		String path = "";
		try {
			Uri mImageCaptureUri = data.getData();
			System.out.println("URI: " + mImageCaptureUri);
			path = FileUtils.getPath(view.getContext(), mImageCaptureUri);
//			path = getRealPathFromURI(mImageCaptureUri, (MainActivity) ClientModel.getClientModel().getContext()); // from Gallery
//			if (path == null) {
//				path = mImageCaptureUri.getPath(); // from File Manager
//			}
//			System.out.println("FILE: " + path);
			openSample(path);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog messageDialog = new MessageDialog(view.getRootView().getContext(), null, "The file cannot be opened at " + path, new String[] { "OK" });
			messageDialog.show();
		}
	}

	private void openSample(String path) {
		try {
			module.sampleName = path;
			String trim = trimName(module.sampleName);
			if (trim.contains(".")) {
				trim = trim.substring(0, trim.lastIndexOf("."));
			}
			fileButton.setText(trim);
			module.patchNum = 0;
			showSampleDetails();
			module.loadSample();
			ArrayAdapter<CharSequence> patchAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, module.patchNames);
			patchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			patchSpinner.setAdapter(patchAdapter);
			patchSpinner.setSelection(module.patchNum);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			MessageDialog messageDialog = new MessageDialog(view.getRootView().getContext(), null, "The WAV or SoundFont file is too large.", new String[] { "OK" });
			messageDialog.show();
		} catch (IOException e) {
			e.printStackTrace();
			MessageDialog messageDialog = new MessageDialog(view.getRootView().getContext(), null, "The file cannot be opened at " + path, new String[] { "OK" });
			messageDialog.show();
		} catch (Throwable e) {
			e.printStackTrace();
			MessageDialog messageDialog = new MessageDialog(view.getRootView().getContext(), null, "The file cannot be read.   Is it a valid WAV or SoundFont file?", new String[] { "OK" });
			messageDialog.show();
		}
	}

//	public String getRealPathFromURI(Uri contentUri) {
//		String[] proj = { MediaStore.Images.Media.DATA };
//		Cursor cursor = ((Activity) view.getRootView().getContext()).managedQuery(contentUri, proj, null, null, null);
//		if (cursor == null) {
//			System.out.println("couldn't find " + contentUri + " in media files");
//			return null;
//		}
//		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//		cursor.moveToFirst();
//		return cursor.getString(column_index);
//	}

	public String getRealPathFromURI(Uri contentUri, MainActivity mainActivity) {
		System.out.println("contentUri is " + contentUri);
		Cursor cursor = null;
		try {
			if (contentUri.toString().contains("com.android.externalstorage")) {
				String s = URLDecoder.decode(contentUri.toString());
				int i = s.indexOf("/primary");
				s = s.substring(i + 9);
				String path = Environment.getExternalStorageDirectory() + "/" + s;
				return path;
			} else {
				// Assume it's a a mediastore managed file
				String[] proj = { MediaStore.Images.Media.DATA };
				cursor = mainActivity.getContentResolver().query(contentUri, proj, null, null, null);
				if (cursor == null)
					return null;
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String path = cursor.getString(column_index);
				return path;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private String trimName(String sampleName) {
		if (sampleName.lastIndexOf("/") >= 0) {
			return sampleName.substring(sampleName.lastIndexOf("/") + 1);
		} else {
			return sampleName;
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.octaveCC.cc == cc) {
			if (view != null) {
				SeekBar pcmOctave = (SeekBar) view.findViewById(R.id.pcmOctave);
				pcmOctave.setProgress(module.octave + 5);
			}
		}
		if (module.pitchCC.cc == cc) {
			if (view != null) {
				SeekBar pcmPitch = (SeekBar) view.findViewById(R.id.pcmPitch);
				pcmPitch.setProgress(module.pitch);
			}
		}
		if (module.detuneCC.cc == cc) {
			if (view != null) {
				SeekBar pcmDetune = (SeekBar) view.findViewById(R.id.pcmDetune);
				pcmDetune.setProgress((int) (module.detune + 50));
			}
		}
	}

}
