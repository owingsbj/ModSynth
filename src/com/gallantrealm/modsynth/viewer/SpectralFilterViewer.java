package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.SpectralControl;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.SpectralFilter;
import com.gallantrealm.mysynth.MessageDialog;
import com.gallantrealm.mysynth.MySynth;

import android.graphics.Canvas;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class SpectralFilterViewer extends ModuleViewer {

	SpectralFilter module;
	int currentStep;

	public SpectralFilterViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (SpectralFilter) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 30, y + 25);
		path.cubicTo(x - 27, y + 25, x - 24, y - 25, x - 21, y - 25);
		path.cubicTo(x - 21, y - 25, x - 18, y + 25, x - 15, y + 25);
		path.moveTo(x - 15f, y + 25);
		path.cubicTo(x - 12, y + 25, x - 9, y - 25, x - 6, y - 25);
		path.cubicTo(x - 6, y - 25, x - 3, y + 25, x, y + 25);
		path.moveTo(x, y + 25);
		path.cubicTo(x +3, y + 25, x + 6, y - 25, x+9, y - 25);
		path.cubicTo(x + 9, y - 25, x + 12, y + 25, x + 15, y + 25);
		path.moveTo(x + 15f, y + 25);
		path.cubicTo(x +18, y + 25, x +21, y - 25, x + 24, y - 25);
		path.cubicTo(x + 24, y - 25, x + 27, y + 25, x + 30, y + 25);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.spectralfilterpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {
		final SpectralControl sfControl = (SpectralControl) view.findViewById(R.id.sfControl);
		final View sfControls = view.findViewById(R.id.sfControls);
		final Button sfEdit = (Button) view.findViewById(R.id.sfEdit);
		final Button sfDone = (Button) view.findViewById(R.id.sfDone);
		final Button sfUp = (Button) view.findViewById(R.id.sfUp);
		final Button sfDown = (Button) view.findViewById(R.id.sfDown);
		final SeekBar sfResonance = (SeekBar) view.findViewById(R.id.sfResonance);
		final SeekBar sfSpread = (SeekBar) view.findViewById(R.id.sfRange);
		final SeekBar sfModulation = (SeekBar) view.findViewById(R.id.sfModulation);

		sfEdit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sfControl.setVisibility(View.GONE);
				sfControls.setVisibility(View.VISIBLE);
				sfEdit.setVisibility(View.GONE);
				sfUp.setVisibility(View.GONE);
				sfDown.setVisibility(View.GONE);
				sfDone.setVisibility(View.VISIBLE);
			}
		});
		sfDone.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sfControl.setVisibility(View.VISIBLE);
				sfControls.setVisibility(View.GONE);
				sfEdit.setVisibility(View.VISIBLE);
				sfUp.setVisibility(View.VISIBLE);
				sfDown.setVisibility(View.VISIBLE);
				sfDone.setVisibility(View.GONE);
			}
		});
		sfUp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sfControl.painting = true;
			}
		});
		sfDown.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sfControl.painting = false;
			}
		});
		sfControl.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new MessageDialog(view.getRootView().getContext(), "Full Version", "Spectral Filter can only be edited in the full version of ModSynth.", null).show();
			}
		});

		sfControl.setSpectralMap(module.spectralMap2);
		sfControls.setVisibility(View.GONE);
		sfDone.setVisibility(View.GONE);

		sfResonance.setProgress((int) (Math.sqrt(module.resonance) * 100.0));
		sfResonance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.resonance = (progress / 100.0) * (progress / 100.0);
				instrument.moduleUpdated(module);
				module.setupFilters(Instrument.MAX_VOICES);
			}
		});
		((View) sfResonance.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.resonanceCC));

		sfSpread.setProgress((int) (module.spread * 100.0));
		sfSpread.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.spread = (progress / 100.0);
				instrument.moduleUpdated(module);
			}
		});
		((View) sfSpread.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.spreadCC));

		sfModulation.setProgress((int) (Math.sqrt(module.modulation) * 100.0));
		sfModulation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.modulation = (progress / 100.0) * (progress / 100.0);
				instrument.moduleUpdated(module);
			}
		});
		((View) sfModulation.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.modulationCC));
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.resonanceCC != null && module.resonanceCC.cc == cc) {
			if (view != null) {
				SeekBar sfResonance = (SeekBar) view.findViewById(R.id.sfResonance);
				sfResonance.setProgress((int) (Math.sqrt(module.resonance) * 100.0));
			}
		}
		if (module.spreadCC != null && module.spreadCC.cc == cc) {
			if (view != null) {
				SeekBar sfSpread = (SeekBar) view.findViewById(R.id.sfRange);
				sfSpread.setProgress((int) (module.spread * 100.0));
			}
		}
		if (module.modulationCC != null && module.modulationCC.cc == cc) {
			if (view != null) {
				SeekBar sfModulation = (SeekBar) view.findViewById(R.id.sfModulation);
				sfModulation.setProgress((int) (Math.sqrt(module.modulation) * 100.0));
			}
		}
	}

	public void setCurrentStep(Integer currentStep) {
		if (view != null) {
			SpectralControl sfControl = (SpectralControl)view.findViewById(R.id.sfControl);
			sfControl.setCurrentStep(currentStep);
		}
	}

}
