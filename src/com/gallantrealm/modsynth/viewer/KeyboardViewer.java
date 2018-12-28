package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Keyboard;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.ClientModel;
import com.gallantrealm.mysynth.MySynth;

import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import de.viktorreiser.toolbox.widget.NumberPicker;

public class KeyboardViewer extends ModuleViewer {
	
	Keyboard module;

	public KeyboardViewer(Module module, MySynth synth) {
		super(module, synth);
		this.module = (Keyboard)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		canvas.drawRect(x - 25, y - 35, x + 25, y + 35, diagramPaint);
		canvas.drawLine(x + 5, y - 35, x + 5, y + 35, diagramPaint);
		for (int i = 1; i < 7; i++) {
			canvas.drawLine(x - 25, y - 35 + 10 * i, x + 5, y - 35 + 10 * i, diagramPaint);
		}
	}

	@Override
	public int getViewResource() {
		return R.layout.keyboardpane;
	}

	@Override
	public void onViewCreate(final MainActivity mainActivity) {

		final Spinner voicesSpinner = (Spinner) view.findViewById(R.id.keyboardVoicesSpinner);
		ArrayAdapter<CharSequence> voicesAdapter;
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			String[] voiceValues = new String[Instrument.MAX_VOICES + 1];
			voiceValues[0] = "Mono";
			voiceValues[1] = "Legato";
			for (int i = 2; i <= Instrument.MAX_VOICES; i++) {
				voiceValues[i] = String.valueOf(i);
			}
			voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, voiceValues);
		} else {
			voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "Mono", "Legato", "2", "3" });
			module.voices = Math.min(3, module.voices);
		}
		voicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voicesSpinner.setAdapter(voicesAdapter);
		voicesSpinner.setSelection(Math.max(0, module.voices));
		voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.voices != voicesSpinner.getSelectedItemPosition()) {
					module.voices = voicesSpinner.getSelectedItemPosition();
					synth.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		((View) voicesSpinner.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.voicesCC));

		final NumberPicker octavePicker = (NumberPicker) view.findViewById(R.id.keyboardOctavePicker);
		octavePicker.setCurrent(module.octave);
		octavePicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				module.octave = octavePicker.getCurrent();
				synth.moduleUpdated(module);
			}
		});
		((View) octavePicker.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.octaveCC));

		View tuningRow = view.findViewById(R.id.keyboardTuningRow);
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			tuningRow.setVisibility(View.VISIBLE);
			final Spinner tuningSpinner = (Spinner) view.findViewById(R.id.keyboardTuningSpinner);
			ArrayAdapter<CharSequence> tuningAdapter = Translator.getArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "Equal Temperament", "Just Intonation", "Out of Tune" });
			tuningAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			tuningSpinner.setAdapter(tuningAdapter);
			tuningSpinner.setSelection(module.tuning);
			tuningSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
					if (module.tuning != tuningSpinner.getSelectedItemPosition()) {
						module.tuning = tuningSpinner.getSelectedItemPosition();
						synth.moduleUpdated(module);
					}
				}
				public void onNothingSelected(AdapterView av) {
				}
			});
			((View) tuningSpinner.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.tuningCC));
		} else {
			tuningRow.setVisibility(View.GONE);
		}

		SeekBar keyboardPortamento = (SeekBar) view.findViewById(R.id.keyboardPortamento);
		keyboardPortamento.setProgress((int) (100.0 * Math.pow(module.portamento, 10.0)));
		keyboardPortamento.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.portamento = Math.pow(progress / 100.0, 0.1);
				synth.moduleUpdated(module);
			}
		});
		((View)keyboardPortamento.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.portamentoCC));

		CheckBox sustainBox = (CheckBox) view.findViewById(R.id.keyboardSustain);
		sustainBox.setChecked(module.getSustaining());
		sustainBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.setSustaining(isChecked);
				mainActivity.updateKeysPressed();
			}
		});

		module.dirty = false;
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.portamentoCC.cc == cc) {
			if (view != null) {
				SeekBar keyboardPortamento = (SeekBar) view.findViewById(R.id.keyboardPortamento);
				keyboardPortamento.setProgress((int) (100.0 * Math.pow(module.portamento, 10.0)));
			}
		}
		if (module.voicesCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					Spinner voicesSpinner = (Spinner) view.findViewById(R.id.keyboardVoicesSpinner);
					voicesSpinner.setSelection(module.voices);
				}
			});
		}
		if (module.octaveCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					final NumberPicker octavePicker = (NumberPicker) view.findViewById(R.id.keyboardOctavePicker);
					octavePicker.setCurrent(module.octave);
				}
			});
		}
		if (module.tuningCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					final Spinner tuningSpinner = (Spinner) view.findViewById(R.id.keyboardTuningSpinner);
					tuningSpinner.setSelection(module.tuning);
				}
			});
		}
	}

}
