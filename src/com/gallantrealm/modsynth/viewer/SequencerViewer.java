package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.android.VerticalBalanceSlider;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Sequencer;
import com.gallantrealm.mysynth.ClientModel;
import com.gallantrealm.mysynth.MySynth;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class SequencerViewer extends ModuleViewer {

	Sequencer module;
	VerticalBalanceSlider[] seekBars;
	ToggleButton[] buttons;
	private transient boolean maximized;

	public SequencerViewer(Module module, MySynth synth) {
		super(module, synth);
		this.module = (Sequencer) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		canvas.drawRect(x - 30, y - 25, x + 30, y + 25, diagramPaint);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 3; j++) {
				canvas.drawPoint(x - 20 + 10 * i, y - 10 + 10 * j, diagramPaint);
			}
		}
	}

	@Override
	public int getViewResource() {
		return R.layout.sequencerpane;
	}

	@Override
	public void onViewCreate(final MainActivity mainActivity) {

		final Spinner voicesSpinner = (Spinner) view.findViewById(R.id.sequenceVoices);
		ArrayAdapter<CharSequence> voicesAdapter;
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });
		} else {
			voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "1", "2", "3" });
			module.voices = Math.min(3, module.voices);
		}
		voicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voicesSpinner.setAdapter(voicesAdapter);
		voicesSpinner.setSelection(Math.max(0, module.voices - 1));
		voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.voices != voicesSpinner.getSelectedItemPosition() + 1) {
					module.voices = voicesSpinner.getSelectedItemPosition() + 1;
					synth.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		final Spinner lengthSpinner = (Spinner) view.findViewById(R.id.sequenceLength);
		ArrayAdapter<CharSequence> lengthAdapter;
		lengthAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" });
		lengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		lengthSpinner.setAdapter(lengthAdapter);
		lengthSpinner.setSelection(Math.max(0, module.activeSteps - 1));
		lengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.activeSteps != lengthSpinner.getSelectedItemPosition() + 1) {
					module.activeSteps = lengthSpinner.getSelectedItemPosition() + 1;
					synth.moduleUpdated(module);
					updateSequenceControls();
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		final SeekBar seqOctave = (SeekBar) view.findViewById(R.id.sequencerOctave);
		seqOctave.setProgress(module.octave + 5);
		seqOctave.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.octave = progress - 5;
				synth.moduleUpdated(module);
			}
		});
		
		CheckBox loopCheckBox = (CheckBox) view.findViewById(R.id.sequenceLoopCheckBox);
		loopCheckBox.setChecked(module.looping);
		loopCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.looping = isChecked;
				synth.moduleUpdated(module);
			}
		});

		CheckBox randomCheckBox = (CheckBox) view.findViewById(R.id.sequenceRandom);
		randomCheckBox.setChecked(module.random);
		randomCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.random = isChecked;
				synth.moduleUpdated(module);
			}
		});
		ClientModel clientModel = ClientModel.getClientModel();
		if (!clientModel.isFullVersion() && !clientModel.isGoggleDogPass()) {
			randomCheckBox.setVisibility(View.GONE);
		}

		if (!module.supportRetrigger) {
			module.supportRetrigger = true;
			module.retrigger = true;
		}
		CheckBox retriggerCheckBox = (CheckBox) view.findViewById(R.id.sequenceRetrigger);
		retriggerCheckBox.setChecked(module.retrigger);
		retriggerCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.retrigger = isChecked;
				synth.moduleUpdated(module);
			}
		});

		seekBars = new VerticalBalanceSlider[Sequencer.MAXSTEPS];
		seekBars[0] = (VerticalBalanceSlider) view.findViewById(R.id.sequence1);
		seekBars[1] = (VerticalBalanceSlider) view.findViewById(R.id.sequence2);
		seekBars[2] = (VerticalBalanceSlider) view.findViewById(R.id.sequence3);
		seekBars[3] = (VerticalBalanceSlider) view.findViewById(R.id.sequence4);
		seekBars[4] = (VerticalBalanceSlider) view.findViewById(R.id.sequence5);
		seekBars[5] = (VerticalBalanceSlider) view.findViewById(R.id.sequence6);
		seekBars[6] = (VerticalBalanceSlider) view.findViewById(R.id.sequence7);
		seekBars[7] = (VerticalBalanceSlider) view.findViewById(R.id.sequence8);
		seekBars[8] = (VerticalBalanceSlider) view.findViewById(R.id.sequence9);
		seekBars[9] = (VerticalBalanceSlider) view.findViewById(R.id.sequence10);
		seekBars[10] = (VerticalBalanceSlider) view.findViewById(R.id.sequence11);
		seekBars[11] = (VerticalBalanceSlider) view.findViewById(R.id.sequence12);
		seekBars[12] = (VerticalBalanceSlider) view.findViewById(R.id.sequence13);
		seekBars[13] = (VerticalBalanceSlider) view.findViewById(R.id.sequence14);
		seekBars[14] = (VerticalBalanceSlider) view.findViewById(R.id.sequence15);
		seekBars[15] = (VerticalBalanceSlider) view.findViewById(R.id.sequence16);
		buttons = new ToggleButton[Sequencer.MAXSTEPS];
		buttons[0] = (ToggleButton) view.findViewById(R.id.sequence1button);
		buttons[1] = (ToggleButton) view.findViewById(R.id.sequence2button);
		buttons[2] = (ToggleButton) view.findViewById(R.id.sequence3button);
		buttons[3] = (ToggleButton) view.findViewById(R.id.sequence4button);
		buttons[4] = (ToggleButton) view.findViewById(R.id.sequence5button);
		buttons[5] = (ToggleButton) view.findViewById(R.id.sequence6button);
		buttons[6] = (ToggleButton) view.findViewById(R.id.sequence7button);
		buttons[7] = (ToggleButton) view.findViewById(R.id.sequence8button);
		buttons[8] = (ToggleButton) view.findViewById(R.id.sequence9button);
		buttons[9] = (ToggleButton) view.findViewById(R.id.sequence10button);
		buttons[10] = (ToggleButton) view.findViewById(R.id.sequence11button);
		buttons[11] = (ToggleButton) view.findViewById(R.id.sequence12button);
		buttons[12] = (ToggleButton) view.findViewById(R.id.sequence13button);
		buttons[13] = (ToggleButton) view.findViewById(R.id.sequence14button);
		buttons[14] = (ToggleButton) view.findViewById(R.id.sequence15button);
		buttons[15] = (ToggleButton) view.findViewById(R.id.sequence16button);

		SeekBar.OnSeekBarChangeListener sequenceChangeListener = new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				for (int i = 0; i < Sequencer.MAXSTEPS; i++) {
					module.sequence[i] = seekBars[i].getProgress();
					synth.moduleUpdated(module);
					if (module.sequenceOn[i]) {
						module.activeSteps = Math.max(module.activeSteps, i + 1);
						buttons[i].setChecked(true);
					} else {
						buttons[i].setChecked(false);
					}
				}
				lengthSpinner.setSelection(Math.max(0, module.activeSteps - 1));
			}
		};
		for (int i = 0; i < Sequencer.MAXSTEPS; i++) {
			SeekBar seekBar = seekBars[i];
			seekBar.setProgress(module.sequence[i]);
			seekBar.setOnSeekBarChangeListener(sequenceChangeListener);
		}

		for (int i = 0; i < Sequencer.MAXSTEPS; i++) {
			ToggleButton button = buttons[i];
			button.setChecked(module.sequenceOn[i]);
			final int sequenceIndex = i;
			OnClickListener buttonsListener = new OnClickListener() {
				public void onClick(View v) {
					ToggleButton button = (ToggleButton)v;
					module.sequenceOn[sequenceIndex] = button.isChecked();
				}
			};
			button.setOnClickListener(buttonsListener);
		}

		final Button max = (Button) view.findViewById(R.id.sequencerMax);
		max.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (maximized) {
					mainActivity.modGraphPane.setVisibility(View.VISIBLE);
					mainActivity.operatorPane.setVisibility(View.VISIBLE);
				//	edit.setVisibility(View.VISIBLE);
					max.setText("[+]");
					maximized = false;
				} else {
					mainActivity.modGraphPane.setVisibility(View.GONE);
					mainActivity.operatorPane.setVisibility(View.GONE);
				//	edit.setVisibility(View.GONE);
					max.setText("[ - ]");
					maximized = true;
				}
			}
		});

		Button sequenceEdit = (Button) view.findViewById(R.id.sequenceEdit);
		sequenceEdit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				view.findViewById(R.id.sequencerControls).setVisibility(View.VISIBLE);
				view.findViewById(R.id.sequencerNotes).setVisibility(View.GONE);
			}
		});

		Button sequenceDone = (Button) view.findViewById(R.id.sequenceDone);
		sequenceDone.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				view.findViewById(R.id.sequencerControls).setVisibility(View.GONE);
				view.findViewById(R.id.sequencerNotes).setVisibility(View.VISIBLE);
			}
		});
		
		view.findViewById(R.id.sequencerControls).setVisibility(View.GONE);
		view.findViewById(R.id.sequencerNotes).setVisibility(View.VISIBLE);
		updateSequenceControls();
	}

	@SuppressLint("NewApi")
	public void setCurrentStep(Integer currentStep) {
		if (view != null && seekBars != null) {
			for (int i = 0; i < seekBars.length; i++) {
				VerticalBalanceSlider seekBar = seekBars[i];
				if (seekBar.isActivated() != (i == currentStep)) {
					seekBar.setThumbLight(i == currentStep);
				}
			}
		}
	}
	
	private void updateSequenceControls() {
		for (int i = 0; i < seekBars.length; i++) {
			if (i < module.activeSteps) {
				seekBars[i].setVisibility(View.VISIBLE);
				buttons[i].setVisibility(View.VISIBLE);
			} else {
				seekBars[i].setVisibility(View.GONE);
				buttons[i].setVisibility(View.GONE);
			}
		}
	}

}
