package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Operator;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;

public class OperatorViewer extends ModuleViewer {
	
	Operator module;

	public OperatorViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Operator)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		diagramPaint.setStyle(Style.FILL_AND_STROKE);
		diagramPaint.setTextSize(50);
		canvas.drawText("OP", x - 35, y + 10, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.operatorpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		SeekBar opOctave = (SeekBar) view.findViewById(R.id.opOctave);
		opOctave.setProgress(module.octave + 5);
		opOctave.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.octave = progress - 5;
				instrument.moduleUpdated(module);
			}
		});
		((View) opOctave.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.octaveCC));

		SeekBar opPitch = (SeekBar) view.findViewById(R.id.opPitch);
		opPitch.setProgress(module.pitch);
		opPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.pitch = progress;
				instrument.moduleUpdated(module);
			}
		});
		((View) opPitch.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.pitchCC));

		SeekBar opDetune = (SeekBar) view.findViewById(R.id.opDetune);
		opDetune.setProgress(module.detune + 50);
		opDetune.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.detune = (progress - 50);
				instrument.moduleUpdated(module);
			}
		});
		((View) opDetune.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.detuneCC));

		SeekBar opFeedback = (SeekBar) view.findViewById(R.id.opFeedback);
		opFeedback.setProgress((int) (module.feedback * 100));
		opFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.feedback = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opFeedback.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.feedbackCC));
		
		SeekBar opDelay = (SeekBar) view.findViewById(R.id.opDelay);
		opDelay.setProgress((int) (module.delay * 100));
		opDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.delay = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opDelay.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.delayCC));

		SeekBar opAttack = (SeekBar) view.findViewById(R.id.opAttack);
		opAttack.setProgress((int) (Math.sqrt(module.attack) * 100));
		opAttack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.attack = progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opAttack.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.attackCC));

		SeekBar opHold = (SeekBar) view.findViewById(R.id.opHold);
		opHold.setProgress((int) (module.hold * 100));
		opHold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.hold = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opHold.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.holdCC));

		SeekBar opDecay = (SeekBar) view.findViewById(R.id.opDecay);
		opDecay.setProgress((int) (Math.sqrt(module.decay) * 100));
		opDecay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.decay = progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opDecay.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.decayCC));

		SeekBar opSustain = (SeekBar) view.findViewById(R.id.opSustain);
		opSustain.setProgress((int) (Math.sqrt(module.sustain) * 100));
		opSustain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.sustain = progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opSustain.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.sustainCC));

		SeekBar opRelease = (SeekBar) view.findViewById(R.id.opRelease);
		opRelease.setProgress((int) (Math.sqrt(module.release) * 100));
		opRelease.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.release = progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opRelease.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.releaseCC));

		SeekBar opMin = (SeekBar) view.findViewById(R.id.opMin);
		opMin.setProgress((int) (Math.pow(module.min, 1.0 / 3.0) * 100));
		opMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.min = progress / 100.0 * progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opMin.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.minCC));

		SeekBar opMax = (SeekBar) view.findViewById(R.id.opMax);
		opMax.setProgress((int) (Math.pow(module.max, 1.0 / 3.0) * 100));
		opMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.max = progress / 100.0 * progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View) opMax.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.maxCC));
		final Spinner slopeTypeSpinner = (Spinner) view.findViewById(R.id.opSlopeSpinner);
		ArrayAdapter<CharSequence> octaveAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, Operator.SlopeType.values());
		octaveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		slopeTypeSpinner.setAdapter(octaveAdapter);
		slopeTypeSpinner.setSelection(module.slopeType != null ? module.slopeType.ordinal() : 0);
		slopeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.slopeType != (Operator.SlopeType) slopeTypeSpinner.getSelectedItem()) {
					module.slopeType = (Operator.SlopeType) slopeTypeSpinner.getSelectedItem();
					instrument.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		CheckBox velocitySensitiveCheckBox = (CheckBox) view.findViewById(R.id.opVelocitySensitive);
		velocitySensitiveCheckBox.setChecked(module.velocitySensitive);
		velocitySensitiveCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				module.velocitySensitive = checked;
				instrument.moduleUpdated(module);
			}
		});
		
		CheckBox keyScaleCheckBox = (CheckBox) view.findViewById(R.id.opKeyScale);
		keyScaleCheckBox.setChecked(module.keyScale);
		keyScaleCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				module.keyScale = checked;
				instrument.moduleUpdated(module);
			}
		});
		
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.octaveCC.cc == cc) {
			if (view != null) {
				SeekBar opOctave = (SeekBar) view.findViewById(R.id.opOctave);
				opOctave.setProgress(module.octave + 5);
			}
		}
		if (module.pitchCC.cc == cc) {
			if (view != null) {
				SeekBar opPitch = (SeekBar) view.findViewById(R.id.opPitch);
				opPitch.setProgress(module.pitch);
			}
		}
		if (module.detuneCC.cc == cc) {
			if (view != null) {
				SeekBar opDetune = (SeekBar) view.findViewById(R.id.opDetune);
				opDetune.setProgress(module.detune + 50);
			}
		}
		if (module.attackCC.cc == cc) {
			if (view != null) {
				SeekBar opAttack = (SeekBar) view.findViewById(R.id.opAttack);
				opAttack.setProgress((int) (Math.sqrt(module.attack) * 100.0));
			}
		}
		if (module.decayCC.cc == cc) {
			if (view != null) {
				SeekBar opDecay = (SeekBar) view.findViewById(R.id.opDecay);
				opDecay.setProgress((int) (Math.sqrt(module.decay) * 100.0));
			}
		}
		if (module.sustainCC.cc == cc) {
			if (view != null) {
				SeekBar opSustain = (SeekBar) view.findViewById(R.id.opSustain);
				opSustain.setProgress((int) (module.sustain * 100.0));
			}
		}
		if (module.releaseCC.cc == cc) {
			if (view != null) {
				SeekBar opRelease = (SeekBar) view.findViewById(R.id.opRelease);
				opRelease.setProgress((int) (Math.sqrt(module.release) * 100.0));
			}
		}
		if (module.minCC.cc == cc) {
			if (view != null) {
				SeekBar opMin = (SeekBar) view.findViewById(R.id.opMin);
				opMin.setProgress((int) (Math.pow(module.min, 1.0 / 3.0) * 100));
			}
		}
		if (module.maxCC.cc == cc) {
			if (view != null) {
				SeekBar opMax = (SeekBar) view.findViewById(R.id.opMax);
				opMax.setProgress((int) (Math.pow(module.max, 1.0 / 3.0) * 100));
			}
		}
		if (module.feedbackCC.cc == cc) {
			if (view != null) {
				SeekBar opFeedback = (SeekBar) view.findViewById(R.id.opFeedback);
				opFeedback.setProgress((int) (module.feedback * 100));
			}
		}
		if (module.delayCC.cc == cc) {
			if (view != null) {
				SeekBar opDelay = (SeekBar) view.findViewById(R.id.opDelay);
				opDelay.setProgress((int) (module.delay * 100));
			}
		}
		if (module.holdCC.cc == cc) {
			if (view != null) {
				SeekBar opHold = (SeekBar) view.findViewById(R.id.opHold);
				opHold.setProgress((int) (module.hold * 100));
			}
		}
	}

}
