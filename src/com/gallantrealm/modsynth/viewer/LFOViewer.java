package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.WaveEditor;
import com.gallantrealm.modsynth.module.LFO;
import com.gallantrealm.modsynth.module.LFO.WaveForm;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.ClientModel;
import com.gallantrealm.mysynth.MessageDialog;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;

public class LFOViewer extends ModuleViewer {

	LFO module;

	public LFOViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (LFO) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 30, y);
		path.lineTo(x - 20, y - 15);
		path.lineTo(x + 20, y + 15);
		path.lineTo(x + 30, y);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.lfopane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {
		final Spinner waveFormSpinner = (Spinner) view.findViewById(R.id.lfoWaveForm);
		final Button waveFormEdit = (Button) view.findViewById(R.id.lfoWaveFormEdit);
		final Button waveFormDone = (Button) view.findViewById(R.id.lfoWaveFormDone);
		final SeekBar lfoPitch = (SeekBar) view.findViewById(R.id.lfoFrequency);
		final SeekBar lfoRandom = (SeekBar) view.findViewById(R.id.lfoRandom);
		final SeekBar lfoFadeIn = (SeekBar) view.findViewById(R.id.lfoFadeIn);
		final CheckBox lfoPositive = (CheckBox) view.findViewById(R.id.lfoPositive);
		final CheckBox lfoPulse = (CheckBox) view.findViewById(R.id.lfoPulse);
		final CheckBox lfoSync = (CheckBox) view.findViewById(R.id.lfoSync);
		final View lfoMainPane = view.findViewById(R.id.lfoMainPane);
		final View lfoWavePane = view.findViewById(R.id.lfoWavePane);
		final WaveEditor lfoWaveEditor = (WaveEditor) view.findViewById(R.id.lfoWaveEditor);

		lfoWaveEditor.wave = module.waveTable;

		WaveForm[] values;
//		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
		values = WaveForm.values();
//		} else {
//			values = new WaveForm[WaveForm.values().length - 1]; // skip harmonics
//			for (int i = 0; i < values.length; i++) {
//				values[i] = WaveForm.values()[i];
//			}
//		}
		ArrayAdapter<CharSequence> waveFormAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, values);
		waveFormAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		waveFormSpinner.setAdapter(waveFormAdapter);
		if (module.waveForm != null) {
			waveFormSpinner.setSelection(module.waveForm.ordinal());
			if (module.waveForm == WaveForm.CUSTOM) {
				waveFormEdit.setVisibility(View.VISIBLE);
			}
		}
		waveFormSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.waveForm != (WaveForm) waveFormSpinner.getSelectedItem()) {
					module.waveForm = (WaveForm) waveFormSpinner.getSelectedItem();
					if (module.waveForm == WaveForm.CUSTOM) {
						waveFormEdit.setVisibility(View.VISIBLE);
					} else {
						waveFormEdit.setVisibility(View.GONE);
					}
					instrument.moduleUpdated(module);
					module.updateWaveTable();
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		((View) waveFormSpinner.getParent().getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.waveformCC));

		waveFormEdit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
					lfoMainPane.setVisibility(View.GONE);
					lfoWavePane.setVisibility(View.VISIBLE);
				} else {
					new MessageDialog(view.getRootView().getContext(), "Full Version", "Wave can only be edited in the full version of ModSynth.", null).show();
				}
			}
		});
		waveFormDone.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				lfoMainPane.setVisibility(View.VISIBLE);
				lfoWavePane.setVisibility(View.GONE);
			}
		});

		lfoPitch.setProgress((int) (Math.sqrt(module.frequency / 20.0) * 100.0));
		lfoPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.frequency = Math.max(0.0001, Math.pow(progress / 100.0, 2.0) * 20.0);
				instrument.moduleUpdated(module);
			}
		});
		((View) lfoPitch.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.frequencyCC));

		ClientModel clientModel = ClientModel.getClientModel();
		if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass()) {
			view.findViewById(R.id.lfoRandomRow).setVisibility(View.GONE);
		} else {
			lfoRandom.setProgress((int) (Math.sqrt(module.random) * 100));
			lfoRandom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.random = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View) lfoRandom.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.randomCC));
		}

		if (module.input1 != null && (clientModel.isFullVersion() || clientModel.isGoggleDogPass())) {
			lfoFadeIn.setProgress((int) (Math.pow(module.fadeIn, 4.0) * 100));
			lfoFadeIn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.fadeIn = Math.pow(progress / 100.0, 0.25);
					instrument.moduleUpdated(module);
				}
			});
			((View) lfoFadeIn.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.fadeInCC));
		} else {
			view.findViewById(R.id.lfoFadeInRow).setVisibility(View.GONE);
		}

		lfoPositive.setChecked(module.positive);
		lfoPositive.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.positive = isChecked;
			}
		});

		if (module.input1 != null) {
			lfoPulse.setVisibility(View.VISIBLE);
		} else {
			lfoPulse.setVisibility(View.GONE);
		}
		lfoPulse.setChecked(module.pulse);
		lfoPulse.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.pulse = isChecked;
			}
		});

		lfoSync.setChecked(module.midiSync);
		lfoSync.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.midiSync = isChecked;
			}
		});
	
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.frequencyCC.cc == cc) {
			SeekBar oscPitch = (SeekBar) view.findViewById(R.id.lfoFrequency);
			oscPitch.setProgress((int) (Math.sqrt(module.frequency / 20.0) * 100.0));
		}
		if (module.randomCC.cc == cc) {
			SeekBar oscNoise = (SeekBar) view.findViewById(R.id.lfoRandom);
			oscNoise.setProgress((int) (Math.sqrt(module.random) * 100));
		}
		if (module.fadeInCC.cc == cc) {
			SeekBar lfoFadeIn = (SeekBar) view.findViewById(R.id.lfoFadeIn);
			lfoFadeIn.setProgress((int) (Math.pow(module.fadeIn, 4.0) * 100));
		}
		if (module.waveformCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					Spinner waveFormSpinner = (Spinner) view.findViewById(R.id.lfoWaveForm);
					waveFormSpinner.setSelection(module.waveForm.ordinal());
				}
			});
		}
	}

}
