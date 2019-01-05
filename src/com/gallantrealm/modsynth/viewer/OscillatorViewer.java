package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Oscillator;
import com.gallantrealm.modsynth.module.Oscillator.WaveForm;
import com.gallantrealm.modsynth.ClientModel;
import com.gallantrealm.android.MessageDialog;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

public class OscillatorViewer extends ModuleViewer {

	Oscillator module;

	public OscillatorViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Oscillator) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 30, y - 15);
		path.lineTo(x - 30, y - 25);
		path.lineTo(x, y - 25);
		path.lineTo(x, y - 5);
		path.lineTo(x + 30, y - 5);
		path.lineTo(x + 30, y - 15);
		path.moveTo(x - 30, y + 15);
		path.lineTo(x - 20, y + 5);
		path.lineTo(x + 20, y + 25);
		path.lineTo(x + 30, y + 15);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.oscillatorpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {
		final Spinner waveFormSpinner = (Spinner) view.findViewById(R.id.oscillatorWaveForm);
		final Button waveFormEdit = (Button) view.findViewById(R.id.oscillatorWaveFormEdit);
		final Button waveFormDone = (Button) view.findViewById(R.id.oscillatorWaveFormDone);
		final SeekBar oscOctave = (SeekBar) view.findViewById(R.id.oscillatorOctave);
		final SeekBar oscPitch = (SeekBar) view.findViewById(R.id.oscillatorPitch);
		final SeekBar oscDetune = (SeekBar) view.findViewById(R.id.oscillatorDetune);
		final SeekBar oscNoise = (SeekBar) view.findViewById(R.id.oscillatorNoise);
		final View oscModulationRow = view.findViewById(R.id.oscillatorModulationRow);
		final SeekBar oscModulation = (SeekBar) view.findViewById(R.id.oscillatorModulation);
		final View oscMainPane = view.findViewById(R.id.oscillatorMain);
		final View oscHarmonicsPane = view.findViewById(R.id.oscillatorHarmonics);

		final SeekBar harmonic1 = (SeekBar) view.findViewById(R.id.harmonic1);
		final SeekBar harmonic2 = (SeekBar) view.findViewById(R.id.harmonic2);
		final SeekBar harmonic3 = (SeekBar) view.findViewById(R.id.harmonic3);
		final SeekBar harmonic4 = (SeekBar) view.findViewById(R.id.harmonic4);
		final SeekBar harmonic5 = (SeekBar) view.findViewById(R.id.harmonic5);
		final SeekBar harmonic6 = (SeekBar) view.findViewById(R.id.harmonic6);
		final SeekBar harmonic7 = (SeekBar) view.findViewById(R.id.harmonic7);
		final SeekBar harmonic8 = (SeekBar) view.findViewById(R.id.harmonic8);
		final SeekBar harmonic9 = (SeekBar) view.findViewById(R.id.harmonic9);
		final SeekBar harmonic10 = (SeekBar) view.findViewById(R.id.harmonic10);
		final SeekBar harmonic11 = (SeekBar) view.findViewById(R.id.harmonic11);
		final SeekBar harmonic12 = (SeekBar) view.findViewById(R.id.harmonic12);
		final SeekBar harmonic13 = (SeekBar) view.findViewById(R.id.harmonic13);
		final SeekBar harmonic14 = (SeekBar) view.findViewById(R.id.harmonic14);
		final SeekBar harmonic15 = (SeekBar) view.findViewById(R.id.harmonic15);
		final SeekBar harmonic16 = (SeekBar) view.findViewById(R.id.harmonic16);
		final SeekBar harmonic17 = (SeekBar) view.findViewById(R.id.harmonic17);
		final SeekBar harmonic18 = (SeekBar) view.findViewById(R.id.harmonic18);
		final SeekBar harmonic19 = (SeekBar) view.findViewById(R.id.harmonic19);
		final SeekBar harmonic20 = (SeekBar) view.findViewById(R.id.harmonic20);
		final SeekBar harmonic21 = (SeekBar) view.findViewById(R.id.harmonic21);
		final SeekBar harmonic22 = (SeekBar) view.findViewById(R.id.harmonic22);
		final SeekBar harmonic23 = (SeekBar) view.findViewById(R.id.harmonic23);
		final SeekBar harmonic24 = (SeekBar) view.findViewById(R.id.harmonic24);
		final SeekBar harmonic25 = (SeekBar) view.findViewById(R.id.harmonic25);
		final SeekBar harmonic26 = (SeekBar) view.findViewById(R.id.harmonic26);
		final SeekBar harmonic27 = (SeekBar) view.findViewById(R.id.harmonic27);
		final SeekBar harmonic28 = (SeekBar) view.findViewById(R.id.harmonic28);
		final SeekBar harmonic29 = (SeekBar) view.findViewById(R.id.harmonic29);
		final SeekBar harmonic30 = (SeekBar) view.findViewById(R.id.harmonic30);
		final SeekBar harmonic31 = (SeekBar) view.findViewById(R.id.harmonic31);
		final SeekBar harmonic32 = (SeekBar) view.findViewById(R.id.harmonic32);

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
			if (module.waveForm == WaveForm.HARMONICS) {
				waveFormEdit.setVisibility(View.VISIBLE);
			}
		}
		waveFormSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.waveForm != (WaveForm) waveFormSpinner.getSelectedItem()) {
					module.waveForm = (WaveForm) waveFormSpinner.getSelectedItem();
					if (module.waveForm == WaveForm.HARMONICS) {
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
					oscMainPane.setVisibility(View.GONE);
					oscHarmonicsPane.setVisibility(View.VISIBLE);
				} else {
					new MessageDialog(view.getRootView().getContext(), "Full Version", "Harmonics can only be edited in the full version of ModSynth.", null).show();
				}
			}
		});
		waveFormDone.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				oscMainPane.setVisibility(View.VISIBLE);
				oscHarmonicsPane.setVisibility(View.GONE);
			}
		});

		oscOctave.setProgress(module.octave + 5);
		oscOctave.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.octave = progress - 5;
				instrument.moduleUpdated(module);
			}
		});
		((View) oscOctave.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.octaveCC));
		oscPitch.setProgress(module.pitch);
		oscPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.pitch = progress;
				instrument.moduleUpdated(module);
			}
		});
		((View) oscPitch.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.pitchCC));

		oscDetune.setProgress((int) (module.detune + 50));
		oscDetune.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.detune = progress - 50;
				instrument.moduleUpdated(module);
			}
		});
		((View) oscDetune.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.detuneCC));

		ClientModel clientModel = ClientModel.getClientModel();
		if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass()) {
			view.findViewById(R.id.oscillatorNoiseRow).setVisibility(View.GONE);
		} else {
			oscNoise.setProgress((int) (Math.sqrt(module.noise) * 100));
			oscNoise.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.noise = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View) oscNoise.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.noiseCC));
		}

		if (module.mod1 == null) {
			oscModulationRow.setVisibility(View.GONE);
		} else {
			oscModulationRow.setVisibility(View.VISIBLE);
			oscModulation.setProgress((int) (100 * Math.sqrt(module.modulation)));
			oscModulation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.modulation = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View) oscModulation.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.modulationCC));
		}

		if (module.harmonics == null) {
			module.harmonics = new double[] { 0.5, 0.25, 0, 0.25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		}
		harmonic1.setProgress((int) (Math.sqrt(module.harmonics[0]) * 100.0));
		harmonic2.setProgress((int) (Math.sqrt(module.harmonics[1]) * 100.0));
		harmonic3.setProgress((int) (Math.sqrt(module.harmonics[2]) * 100.0));
		harmonic4.setProgress((int) (Math.sqrt(module.harmonics[3]) * 100.0));
		harmonic5.setProgress((int) (Math.sqrt(module.harmonics[4]) * 100.0));
		harmonic6.setProgress((int) (Math.sqrt(module.harmonics[5]) * 100.0));
		harmonic7.setProgress((int) (Math.sqrt(module.harmonics[6]) * 100.0));
		harmonic8.setProgress((int) (Math.sqrt(module.harmonics[7]) * 100.0));
		harmonic9.setProgress((int) (Math.sqrt(module.harmonics[8]) * 100.0));
		harmonic10.setProgress((int) (Math.sqrt(module.harmonics[9]) * 100.0));
		harmonic11.setProgress((int) (Math.sqrt(module.harmonics[10]) * 100.0));
		harmonic12.setProgress((int) (Math.sqrt(module.harmonics[11]) * 100.0));
		harmonic13.setProgress((int) (Math.sqrt(module.harmonics[12]) * 100.0));
		harmonic14.setProgress((int) (Math.sqrt(module.harmonics[13]) * 100.0));
		harmonic15.setProgress((int) (Math.sqrt(module.harmonics[14]) * 100.0));
		harmonic16.setProgress((int) (Math.sqrt(module.harmonics[15]) * 100.0));
		if (module.harmonics.length > 16) {
			harmonic17.setProgress((int) (Math.sqrt(module.harmonics[16]) * 100.0));
			harmonic18.setProgress((int) (Math.sqrt(module.harmonics[17]) * 100.0));
			harmonic19.setProgress((int) (Math.sqrt(module.harmonics[18]) * 100.0));
			harmonic20.setProgress((int) (Math.sqrt(module.harmonics[19]) * 100.0));
			harmonic21.setProgress((int) (Math.sqrt(module.harmonics[20]) * 100.0));
			harmonic22.setProgress((int) (Math.sqrt(module.harmonics[21]) * 100.0));
			harmonic23.setProgress((int) (Math.sqrt(module.harmonics[22]) * 100.0));
			harmonic24.setProgress((int) (Math.sqrt(module.harmonics[23]) * 100.0));
			harmonic25.setProgress((int) (Math.sqrt(module.harmonics[24]) * 100.0));
			harmonic26.setProgress((int) (Math.sqrt(module.harmonics[25]) * 100.0));
			harmonic27.setProgress((int) (Math.sqrt(module.harmonics[26]) * 100.0));
			harmonic28.setProgress((int) (Math.sqrt(module.harmonics[27]) * 100.0));
			harmonic29.setProgress((int) (Math.sqrt(module.harmonics[28]) * 100.0));
			harmonic30.setProgress((int) (Math.sqrt(module.harmonics[29]) * 100.0));
			harmonic31.setProgress((int) (Math.sqrt(module.harmonics[30]) * 100.0));
			harmonic32.setProgress((int) (Math.sqrt(module.harmonics[31]) * 100.0));
		}
		SeekBar.OnSeekBarChangeListener harmonicListener = new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.harmonics = new double[] { //
						Math.pow(harmonic1.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic2.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic3.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic4.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic5.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic6.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic7.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic8.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic9.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic10.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic11.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic12.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic13.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic14.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic15.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic16.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic17.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic18.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic19.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic20.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic21.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic22.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic23.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic24.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic25.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic26.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic27.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic28.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic29.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic30.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic31.getProgress(), 2.0) / 10000.0, //
						Math.pow(harmonic32.getProgress(), 2.0) / 10000.0 };
				instrument.moduleUpdated(module);
				module.updateWaveTable();
			}
		};
		harmonic1.setOnSeekBarChangeListener(harmonicListener);
		harmonic2.setOnSeekBarChangeListener(harmonicListener);
		harmonic3.setOnSeekBarChangeListener(harmonicListener);
		harmonic4.setOnSeekBarChangeListener(harmonicListener);
		harmonic5.setOnSeekBarChangeListener(harmonicListener);
		harmonic6.setOnSeekBarChangeListener(harmonicListener);
		harmonic7.setOnSeekBarChangeListener(harmonicListener);
		harmonic8.setOnSeekBarChangeListener(harmonicListener);
		harmonic9.setOnSeekBarChangeListener(harmonicListener);
		harmonic10.setOnSeekBarChangeListener(harmonicListener);
		harmonic11.setOnSeekBarChangeListener(harmonicListener);
		harmonic12.setOnSeekBarChangeListener(harmonicListener);
		harmonic13.setOnSeekBarChangeListener(harmonicListener);
		harmonic14.setOnSeekBarChangeListener(harmonicListener);
		harmonic15.setOnSeekBarChangeListener(harmonicListener);
		harmonic16.setOnSeekBarChangeListener(harmonicListener);
		harmonic17.setOnSeekBarChangeListener(harmonicListener);
		harmonic18.setOnSeekBarChangeListener(harmonicListener);
		harmonic19.setOnSeekBarChangeListener(harmonicListener);
		harmonic20.setOnSeekBarChangeListener(harmonicListener);
		harmonic21.setOnSeekBarChangeListener(harmonicListener);
		harmonic22.setOnSeekBarChangeListener(harmonicListener);
		harmonic23.setOnSeekBarChangeListener(harmonicListener);
		harmonic24.setOnSeekBarChangeListener(harmonicListener);
		harmonic25.setOnSeekBarChangeListener(harmonicListener);
		harmonic26.setOnSeekBarChangeListener(harmonicListener);
		harmonic27.setOnSeekBarChangeListener(harmonicListener);
		harmonic28.setOnSeekBarChangeListener(harmonicListener);
		harmonic29.setOnSeekBarChangeListener(harmonicListener);
		harmonic30.setOnSeekBarChangeListener(harmonicListener);
		harmonic31.setOnSeekBarChangeListener(harmonicListener);
		harmonic32.setOnSeekBarChangeListener(harmonicListener);

	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.octaveCC.cc == cc) {
			SeekBar oscOctave = (SeekBar) view.findViewById(R.id.oscillatorOctave);
			oscOctave.setProgress(module.octave + 5);
		}
		if (module.pitchCC.cc == cc) {
			SeekBar oscPitch = (SeekBar) view.findViewById(R.id.oscillatorPitch);
			oscPitch.setProgress(module.pitch);
		}
		if (module.detuneCC.cc == cc) {
			SeekBar oscDetune = (SeekBar) view.findViewById(R.id.oscillatorDetune);
			oscDetune.setProgress((int) (module.detune + 50));
		}
		if (module.noiseCC.cc == cc) {
			SeekBar oscNoise = (SeekBar) view.findViewById(R.id.oscillatorNoise);
			oscNoise.setProgress((int) (Math.sqrt(module.noise) * 100));
		}
		if (module.modulationCC.cc == cc) {
			SeekBar oscModulation = (SeekBar) view.findViewById(R.id.oscillatorModulation);
			oscModulation.setProgress((int) (100 * Math.sqrt(module.modulation)));
		}
		if (module.waveformCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					Spinner waveFormSpinner = (Spinner) view.findViewById(R.id.oscillatorWaveForm);
					waveFormSpinner.setSelection(module.waveForm.ordinal());
				}
			});
		}
	}

}
