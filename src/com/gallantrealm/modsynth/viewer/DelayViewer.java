package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Delay;
import com.gallantrealm.modsynth.module.Module;
import android.view.View;
import android.widget.SeekBar;

public class DelayViewer extends ModuleViewer {
	
	Delay module;

	public DelayViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Delay)module;
	}

	@Override
	public int getViewResource() {
		return R.layout.delaypane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		SeekBar delayLevelBar = (SeekBar) view.findViewById(R.id.delayLevel);
		delayLevelBar.setProgress((int) (module.delayLevel * 100.0));
		delayLevelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.delayLevel = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)delayLevelBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.delayLevelCC));

		SeekBar delayTimeBar = (SeekBar) view.findViewById(R.id.delayTime);
		delayTimeBar.setProgress((int) (Math.sqrt(module.delayTime) * 100.0));
		delayTimeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.delayTime = progress / 100.0 * progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)delayTimeBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.delayTimeCC));

		SeekBar feedbackBar = (SeekBar) view.findViewById(R.id.delayFeedback);
		feedbackBar.setProgress((int) (module.feedback * 100.0));
		feedbackBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.feedback = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)feedbackBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.feedbackCC));

		View delayModulationRow = view.findViewById(R.id.delayModulationRow);
		if (module.mod1 == null) {
			delayModulationRow.setVisibility(View.GONE);
		} else {
			delayModulationRow.setVisibility(View.VISIBLE);
			SeekBar flangeAmountBar = (SeekBar) view.findViewById(R.id.delayModulation);
			flangeAmountBar.setProgress((int) (module.flangeAmount * 100.0));
			flangeAmountBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.flangeAmount = progress / 100.0;
					instrument.moduleUpdated(module);
				}
			});
			((View)flangeAmountBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.flangeAmountCC));
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.delayLevelCC.cc == cc) {
			if (view != null) {
				SeekBar delayLevelBar = (SeekBar) view.findViewById(R.id.delayLevel);
				delayLevelBar.setProgress((int) (module.delayLevel * 100.0));
			}
		}
		if (module.delayTimeCC.cc == cc) {
			if (view != null) {
				SeekBar delayTimeBar = (SeekBar) view.findViewById(R.id.delayTime);
				delayTimeBar.setProgress((int) (Math.sqrt(module.delayTime) * 100.0));
			}
		}
		if (module.feedbackCC.cc == cc) {
			if (view != null) {
				SeekBar feedbackBar = (SeekBar) view.findViewById(R.id.delayFeedback);
				feedbackBar.setProgress((int) (module.feedback * 100.0));
			}
		}
		if (module.flangeAmountCC.cc == cc) {
			if (view != null) {
				SeekBar flangeAmountBar = (SeekBar) view.findViewById(R.id.delayModulation);
				flangeAmountBar.setProgress((int) (module.flangeAmount * 100.0));
			}
		}
	}
	
}
