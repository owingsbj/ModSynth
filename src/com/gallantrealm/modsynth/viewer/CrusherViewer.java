package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Crusher;
import com.gallantrealm.modsynth.module.Module;
import android.graphics.Canvas;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class CrusherViewer extends ModuleViewer {
	
	private Crusher module;

	public CrusherViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Crusher)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 25, y);
		path.lineTo(x - 25, y - 10);
		path.lineTo(x - 18, y - 10);
		path.lineTo(x - 18, y - 25);
		path.lineTo(x - 6, y - 25);
		path.lineTo(x - 6, y - 10);
		path.lineTo(x + 0, y - 10);
		path.lineTo(x + 0, y + 10);
		path.lineTo(x + 6, y + 10);
		path.lineTo(x + 6, y + 25);
		path.lineTo(x + 18, y + 25);
		path.lineTo(x + 18, y + 10);
		path.lineTo(x + 25, y + 10);
		path.lineTo(x + 25, y);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.crusherpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		SeekBar crusherLevels = (SeekBar) view.findViewById(R.id.crusherLevels);
		crusherLevels.setProgress((int) (100 * module.level));
		crusherLevels.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.level = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)crusherLevels.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.levelCC));

		SeekBar crusherRate = (SeekBar) view.findViewById(R.id.crusherRate);
		crusherRate.setProgress((int) (100 * Math.pow(module.rate, 10)));
		crusherRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.rate = Math.pow(progress / 100.0, 0.1);
				instrument.moduleUpdated(module);
			}
		});
		((View)crusherRate.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.rateCC));

		View crusherModulationRow = view.findViewById(R.id.crusherModulationRow);
		if (module.mod1 == null) {
			crusherModulationRow.setVisibility(View.GONE);
		} else {
			crusherModulationRow.setVisibility(View.VISIBLE);
			SeekBar crusherModulation = (SeekBar) view.findViewById(R.id.crusherModulation);
			crusherModulation.setProgress((int) (100 * Math.sqrt(module.modulation)));
			crusherModulation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.modulation = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View)crusherModulation.getParent().getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.modulationCC));
			CheckBox crusherModRate = (CheckBox) view.findViewById(R.id.crusherModRate);
			crusherModRate.setChecked(module.modulateRate);
			crusherModRate.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton arg0, boolean checked) {
					module.modulateRate = checked;
					instrument.moduleUpdated(module);
				}
			});
		}

	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.levelCC.cc == cc) {
			if (view != null) {
				SeekBar crusherLevel = (SeekBar) view.findViewById(R.id.crusherLevels);
				crusherLevel.setProgress((int) (module.level * 100.0));
			}
		}
		if (module.rateCC.cc == cc) {
			if (view != null) {
				SeekBar crusherRate = (SeekBar) view.findViewById(R.id.crusherRate);
				crusherRate.setProgress((int) (module.rate * 100.0));
			}
		}
		if (module.modulationCC.cc == cc) {
			if (view != null) {
				SeekBar crusherModulation = (SeekBar) view.findViewById(R.id.crusherModulation);
				crusherModulation.setProgress((int) (module.modulation * 100.0));
			}
		}
	}
	
}
