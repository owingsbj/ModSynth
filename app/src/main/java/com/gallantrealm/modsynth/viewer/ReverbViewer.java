package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Reverb;
import android.view.View;
import android.widget.SeekBar;

public class ReverbViewer extends ModuleViewer {
	
	Reverb module;

	public ReverbViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Reverb)module;
	}

	@Override
	public int getViewResource() {
		return R.layout.reverbpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {
		SeekBar reverbAmountBar = (SeekBar) view.findViewById(R.id.reverbAmount);
		reverbAmountBar.setProgress((int) (module.amount * 100.0));
		reverbAmountBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.amount = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)reverbAmountBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.amountCC));

		SeekBar reverbDepthBar = (SeekBar) view.findViewById(R.id.reverbDepth);
		reverbDepthBar.setProgress((int) (Math.sqrt(module.depth) * 100.0));
		reverbDepthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.depth = progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)reverbDepthBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.depthCC));

		SeekBar reverbToneBar = (SeekBar) view.findViewById(R.id.reverbTone);
		reverbToneBar.setProgress((int) (100 * (1.0 - module.tone)));
		reverbToneBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.tone = 1.0 - progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)reverbToneBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.toneCC));
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.amountCC.cc == cc) {
			if (view != null) {
				SeekBar reverbAmountBar = (SeekBar) view.findViewById(R.id.reverbAmount);
				reverbAmountBar.setProgress((int) (module.amount * 100.0));
			}
		}
		if (module.depthCC.cc == cc) {
			if (view != null) {
				SeekBar reverbDepthBar = (SeekBar) view.findViewById(R.id.reverbDepth);
				reverbDepthBar.setProgress((int) (Math.sqrt(module.depth) * 100.0));
			}
		}
		if (module.toneCC.cc == cc) {
			if (view != null) {
				SeekBar reverbToneBar = (SeekBar) view.findViewById(R.id.reverbTone);
				reverbToneBar.setProgress((int) (100 * (1.0 - module.tone)));
			}
		}
	}
	

}
