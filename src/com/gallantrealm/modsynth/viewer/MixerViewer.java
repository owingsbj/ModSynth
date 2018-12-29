package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Mixer;
import com.gallantrealm.modsynth.module.Mixer.MixFunction;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.ClientModel;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

public class MixerViewer extends ModuleViewer {

	Mixer module;

	public MixerViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Mixer) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
//		Paint paint = new Paint();
//		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
//		paint.setColor(0xFFFFFFFF);
		float originalStrokeWidth = diagramPaint.getStrokeWidth();
		diagramPaint.setStrokeWidth(10);
		diagramPaint.setStyle(Style.FILL_AND_STROKE);
		if (module.mixFunction == Mixer.MixFunction.ADD) {
			canvas.drawLine(x, y - 30, x, y + 30, diagramPaint);
			canvas.drawLine(x - 30, y, x + 30, y, diagramPaint);
		} else if (module.mixFunction == Mixer.MixFunction.SUBTRACT) {
			canvas.drawLine(x - 30, y, x + 30, y, diagramPaint);
		} else if (module.mixFunction == MixFunction.MULTIPLY) {
			canvas.drawLine(x - 25, y - 25, x + 25, y + 25, diagramPaint);
			canvas.drawLine(x - 25, y + 25, x + 25, y - 25, diagramPaint);
		} else if (module.mixFunction == MixFunction.MAX) {
			canvas.drawLine(x - 20, y - 30, x, y + 30, diagramPaint);
			canvas.drawLine(x, y + 30, x + 20, y - 30, diagramPaint);
		} else if (module.mixFunction == MixFunction.MIN) {
			canvas.drawLine(x - 20, y + 30, x, y - 30, diagramPaint);
			canvas.drawLine(x, y - 30, x + 20, y + 30, diagramPaint);
		}
		diagramPaint.setStrokeWidth(originalStrokeWidth);
	}

	@Override
	public int getViewResource() {
		return R.layout.mixerpane;
	}

	@Override
	public void onViewCreate(final MainActivity mainActivity) {

		final Spinner typeSpinner = (Spinner) view.findViewById(R.id.mixerTypeSpinner);
		ArrayAdapter<CharSequence> octaveAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, MixFunction.values());
		octaveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(octaveAdapter);
		typeSpinner.setSelection(module.mixFunction.ordinal());
		typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.mixFunction != (MixFunction) typeSpinner.getSelectedItem()) {
					module.mixFunction = (MixFunction) typeSpinner.getSelectedItem();
					instrument.moduleUpdated(module);
					mainActivity.modGraph.invalidate();
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		((View) typeSpinner.getParent().getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.mixFunctionCC));

		if (module.input1 == null) {
			view.findViewById(R.id.mixerLevel1Row).setVisibility(View.GONE);
		} else {
			SeekBar mixerLevel1 = (SeekBar) view.findViewById(R.id.mixerLevel1);
			mixerLevel1.setProgress((int) (Math.sqrt(module.level1) * 100.0));
			mixerLevel1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.level1 = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View) mixerLevel1.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.level1CC));
		}

		if (module.input2 == null) {
			view.findViewById(R.id.mixerLevel2Row).setVisibility(View.GONE);
		} else {
			SeekBar mixerLevel2 = (SeekBar) view.findViewById(R.id.mixerLevel2);
			mixerLevel2.setProgress((int) (Math.sqrt(module.level2) * 100.0));
			mixerLevel2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.level2 = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View) mixerLevel2.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.level2CC));
		}

		if (module.input3 == null) {
			view.findViewById(R.id.mixerLevel3Row).setVisibility(View.GONE);
		} else {
			SeekBar mixerLevel3 = (SeekBar) view.findViewById(R.id.mixerLevel3);
			mixerLevel3.setProgress((int) (Math.sqrt(module.level3) * 100.0));
			mixerLevel3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.level3 = (progress / 100.0) * (progress / 100.0);
					instrument.moduleUpdated(module);
				}
			});
			((View) mixerLevel3.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.level3CC));
		}

		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			SeekBar mixerBias = (SeekBar) view.findViewById(R.id.mixerBias);
			mixerBias.setProgress((int) (module.bias * 100.0));
			mixerBias.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.bias = progress / 100.0;
					instrument.moduleUpdated(module);
				}
			});
			((View) mixerBias.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.biasCC));
		} else {
			view.findViewById(R.id.mixerBiasRow).setVisibility(View.GONE);
		}

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
					instrument.moduleUpdated(module);
				}
			});
			((View) mixerModulation.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.modulationCC));
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.level1CC != null && module.level1CC.cc == cc) {
			if (view != null) {
				SeekBar mixerLevel1 = (SeekBar) view.findViewById(R.id.mixerLevel1);
				mixerLevel1.setProgress((int) (Math.sqrt(module.level1) * 100.0));
			}
		}
		if (module.level2CC != null && module.level2CC.cc == cc) {
			if (view != null) {
				SeekBar mixerLevel2 = (SeekBar) view.findViewById(R.id.mixerLevel2);
				mixerLevel2.setProgress((int) (Math.sqrt(module.level2) * 100.0));
			}
		}
		if (module.level3CC != null && module.level3CC.cc == cc) {
			if (view != null) {
				SeekBar mixerLevel3 = (SeekBar) view.findViewById(R.id.mixerLevel3);
				mixerLevel3.setProgress((int) (Math.sqrt(module.level3) * 100.0));
			}
		}
		if (module.biasCC != null && module.biasCC.cc == cc) {
			if (view != null) {
				SeekBar mixerBias = (SeekBar) view.findViewById(R.id.mixerBias);
				mixerBias.setProgress((int) (module.bias * 100.0));
			}
		}
		if (module.modulationCC != null && module.modulationCC.cc == cc) {
			if (view != null) {
				SeekBar mixerModulation = (SeekBar) view.findViewById(R.id.mixerModulation);
				mixerModulation.setProgress((int) (Math.sqrt(module.modulation) * 100.0));
			}
		}
		if (module.mixFunctionCC != null && module.mixFunctionCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					Spinner mixerTypeSpinner = (Spinner) view.findViewById(R.id.mixerTypeSpinner);
					mixerTypeSpinner.setSelection(module.mixFunction.ordinal());
				}
			});
		}
	}

}
