package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Pan;
import com.gallantrealm.mysynth.MySynth;

import android.graphics.Canvas;
import android.view.View;
import android.widget.SeekBar;

public class PanViewer extends ModuleViewer {
	
	Pan module;

	public PanViewer(Module module, MySynth synth) {
		super(module, synth);
		this.module = (Pan)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		canvas.drawLine(x - 25, y, x, y, diagramPaint);
		canvas.drawLine(x - 5, y - 5, x, y, diagramPaint);
		canvas.drawLine(x - 5, y + 5, x, y, diagramPaint);
		canvas.drawLine(x, y - 25, x, y + 25, diagramPaint);
		canvas.drawLine(x - 5, y - 25, x + 25, y - 25, diagramPaint);
		canvas.drawLine(x - 5, y + 25, x + 25, y + 25, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.panpane;
	}

	@Override
	public void onViewCreate(final MainActivity mainActivity) {

		SeekBar mixerBalance = (SeekBar) view.findViewById(R.id.mixerLevel2);
		mixerBalance.setProgress((int) (module.balance * 100.0));
		mixerBalance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.balance = progress / 100.0;
				synth.moduleUpdated(module);
			}
		});
		((View) mixerBalance.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.balanceCC));

		View mixerModulationRow = view.findViewById(R.id.mixerModulationRow);
		if (module.mod1 == null) {
			mixerModulationRow.setVisibility(View.GONE);
		} else {
			mixerModulationRow.setVisibility(View.VISIBLE);
			SeekBar mixerModulation = (SeekBar) view.findViewById(R.id.mixerModulation);
			mixerModulation.setProgress((int) (module.modulation * 100.0));
			mixerModulation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.modulation = progress / 100.0;
					synth.moduleUpdated(module);
				}
			});
			((View) mixerModulation.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.modulationCC));
		}
	}

	public void updateCC(int cc, double value) {
		if (module.balanceCC.cc == cc) {
			if (view != null) {
				SeekBar panBalance = (SeekBar) view.findViewById(R.id.mixerLevel2);
				panBalance.setProgress((int) (module.balance * 100.0));
			}
		}
		if (module.modulationCC.cc == cc) {
			if (view != null) {
				SeekBar panModulation = (SeekBar) view.findViewById(R.id.mixerModulation);
				panModulation.setProgress((int) (module.modulation * 100.0));
			}
		}
	}

}
