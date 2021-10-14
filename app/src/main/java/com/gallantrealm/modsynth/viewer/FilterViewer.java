package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.BiQuadraticFilter;
import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Filter;
import com.gallantrealm.modsynth.module.Module;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

public class FilterViewer extends ModuleViewer {

	Filter module;

	public FilterViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Filter) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 25, y + 25);
		path.cubicTo(x - 20, y + 25, x - 10, y - 25, x, y - 25);
		path.cubicTo(x + 10, y - 25, x + 20, y + 25, x + 25, y + 25);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.filterpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		final Spinner typeSpinner = (Spinner) view.findViewById(R.id.filterTypeSpinner);
		ArrayAdapter<CharSequence> octaveAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, BiQuadraticFilter.Type.values());
		octaveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(octaveAdapter);
		typeSpinner.setSelection(module.filterType.ordinal());
		typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.filterType != (BiQuadraticFilter.Type) typeSpinner.getSelectedItem()) {
					module.filterType = (BiQuadraticFilter.Type) typeSpinner.getSelectedItem();
					instrument.moduleUpdated(module);
					module.setupFilters();
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		((View) typeSpinner.getParent().getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.filterTypeCC));

		SeekBar filterResonance = (SeekBar) view.findViewById(R.id.filterResonance);
		filterResonance.setProgress((int) (10.0 * Math.sqrt(module.resonance)));
		filterResonance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				double v = progress / 10.0;
				module.resonance = v * v;
				instrument.moduleUpdated(module);
			}
		});
		((View) filterResonance.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.resonanceCC));

		SeekBar filterCutoff = (SeekBar) view.findViewById(R.id.filterCutoff);
		filterCutoff.setProgress((int) (module.cutoff * 100.0));
		filterCutoff.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// cutoff = progress / 100.0;
				module.cutoff = (module.cutoff + progress / 100.0) * 0.5;
				instrument.moduleUpdated(module);
			}
		});
		((View) filterCutoff.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.cutoffCC));

		View filterModulationRow = view.findViewById(R.id.filterModulationRow);
		if (module.mod1 == null) {
			filterModulationRow.setVisibility(View.GONE);
		} else {
			filterModulationRow.setVisibility(View.VISIBLE);
			SeekBar filterSweep = (SeekBar) view.findViewById(R.id.filterSweep);
			filterSweep.setProgress((int) (Math.sqrt(module.sweep) * 100.0));
			filterSweep.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.sweep = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View) filterSweep.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.sweepCC));
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.resonanceCC != null && module.resonanceCC.cc == cc) {
			if (view != null) {
				SeekBar filterResonance = (SeekBar) view.findViewById(R.id.filterResonance);
				filterResonance.setProgress((int) (10.0 * Math.sqrt(module.resonance)));
			}
		}
		if (module.cutoffCC != null && module.cutoffCC.cc == cc) {
			if (view != null) {
				SeekBar filterCutoff = (SeekBar) view.findViewById(R.id.filterCutoff);
				filterCutoff.setProgress((int) (module.cutoff * 100.0));
			}
		}
		if (module.sweepCC != null && module.sweepCC.cc == cc) {
			if (view != null) {
				SeekBar filterSweep = (SeekBar) view.findViewById(R.id.filterSweep);
				filterSweep.setProgress((int) (Math.sqrt(module.sweep) * 100.0));
			}
		}
		if (module.filterTypeCC != null && module.filterTypeCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					Spinner typeSpinner = (Spinner) view.findViewById(R.id.filterTypeSpinner);
					typeSpinner.setSelection(module.filterType.ordinal());
				}
			});
		}
	}

}
