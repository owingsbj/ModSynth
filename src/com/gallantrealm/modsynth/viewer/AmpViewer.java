package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Amp;
import com.gallantrealm.modsynth.module.Module;
import android.graphics.Canvas;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class AmpViewer extends ModuleViewer {

	Amp module;

	public AmpViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Amp) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 25, y - 25);
		path.lineTo(x + 25, y);
		path.lineTo(x - 25, y + 25);
		path.lineTo(x - 25, y - 25);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.amppane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		SeekBar ampVolume = (SeekBar) view.findViewById(R.id.ampVolume);
		ampVolume.setProgress((int) (100 * module.volume));
		ampVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.volume = progress / 100.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View) ampVolume.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.volumeCC));

		SeekBar ampTone = (SeekBar) view.findViewById(R.id.ampTone);
		ampTone.setProgress((int) (100 * (1.0 - module.tone * module.tone)));
		ampTone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.tone = Math.sqrt(1.0 - progress / 100.0);
				instrument.moduleUpdated(module);
			}
		});
		((View) ampTone.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.toneCC));

		SeekBar ampOverdrive = (SeekBar) view.findViewById(R.id.ampOverdrive);
		ampOverdrive.setProgress((int) (10 * module.overdrive));
		ampOverdrive.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.overdrive = progress / 10.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View) ampOverdrive.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.overdriveCC));

		CheckBox distortionCheckBox = (CheckBox) view.findViewById(R.id.ampDistortion);
		distortionCheckBox.setChecked(module.distortion);
		distortionCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				module.distortion = checked;
				instrument.moduleUpdated(module);
			}
		});
		distortionCheckBox.setOnLongClickListener(MidiControlDialog.newLongClickListener(module.distortionCC));

	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.volumeCC.cc == cc) {
			if (view != null) {
				SeekBar ampVolume = (SeekBar) view.findViewById(R.id.ampVolume);
				ampVolume.setProgress((int) (module.volume * 100.0));
			}
		}
		if (module.toneCC.cc == cc) {
			if (view != null) {
				SeekBar ampTone = (SeekBar) view.findViewById(R.id.ampTone);
				ampTone.setProgress((int) (100 * (1.0 - module.tone)));
			}
		}
		if (module.overdriveCC.cc == cc) {
			if (view != null) {
				SeekBar ampOverdrive = (SeekBar) view.findViewById(R.id.ampOverdrive);
				ampOverdrive.setProgress((int) (10 * module.overdrive));
			}
		}
		if (module.distortionCC.cc == cc) {
			final CheckBox distortionBox = (CheckBox) view.findViewById(R.id.ampDistortion);
			distortionBox.setChecked(module.distortion);
		}
	}

}
