package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Unison;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;

public class UnisonViewer extends ModuleViewer {
	
	Unison module;

	public UnisonViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Unison)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 25, y - 25);
		path.lineTo(x + 25, y - 25);
		path.moveTo(x - 25, y - 12);
		path.lineTo(x + 25, y - 12);
		path.moveTo(x - 25, y);
		path.lineTo(x + 25, y);
		path.moveTo(x - 25, y + 12);
		path.lineTo(x + 25, y + 12);
		path.moveTo(x - 25, y + 25);
		path.lineTo(x + 25, y + 25);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.unisonpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		final Spinner voicesSpinner = (Spinner) view.findViewById(R.id.unisonVoicesSpinner);
		ArrayAdapter<CharSequence> voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "2", "3", "4", "5" });
		voicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voicesSpinner.setAdapter(voicesAdapter);
		voicesSpinner.setSelection(Math.min(Math.max(0, module.voices - 2), 3));
		voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.voices != voicesSpinner.getSelectedItemPosition() + 2) {
					module.voices = voicesSpinner.getSelectedItemPosition() + 2;
					instrument.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		final Spinner polySpinner = (Spinner) view.findViewById(R.id.unisonPolySpinner);
		ArrayAdapter<CharSequence> polyAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "1", "2", "3", "4", "5" });
		polyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		polySpinner.setAdapter(polyAdapter);
		polySpinner.setSelection(Math.max(0, module.polyphony - 1));
		polySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.polyphony != polySpinner.getSelectedItemPosition() + 1) {
					module.polyphony = polySpinner.getSelectedItemPosition() + 1;
					instrument.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		final SeekBar unisonWidth = (SeekBar) view.findViewById(R.id.unisonWidth);
		unisonWidth.setProgress((int) (module.chorusWidth * 100));
		unisonWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.chorusWidth = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)unisonWidth.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.chorusWidthCC));

		final SeekBar unisonSpread = (SeekBar) view.findViewById(R.id.unisonSpread);
		unisonSpread.setProgress((int) (module.chorusSpread * 100));
		unisonSpread.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.chorusSpread = progress / 100.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)unisonSpread.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.chorusSpreadCC));

	}

	public void updateCC(int cc, double value) {
		if (module.chorusWidthCC.cc == cc) {
			if (view != null) {
				SeekBar unisonWidth = (SeekBar) view.findViewById(R.id.unisonWidth);
				unisonWidth.setProgress((int) (module.chorusWidth * 100));
			}
		}
		if (module.chorusSpreadCC.cc == cc) {
			if (view != null) {
				final SeekBar unisonSpread = (SeekBar) view.findViewById(R.id.unisonSpread);
				unisonSpread.setProgress((int) (module.chorusSpread * 100));
			}
		}
	}

}
