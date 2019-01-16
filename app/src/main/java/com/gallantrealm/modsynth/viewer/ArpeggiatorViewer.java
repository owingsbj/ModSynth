package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Arpeggiator;
import com.gallantrealm.modsynth.module.Module;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class ArpeggiatorViewer extends ModuleViewer {
	
	private Arpeggiator module;

	public ArpeggiatorViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Arpeggiator)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x + 5, y -30);
		path.lineTo(x - 5, y - 20);
		path.lineTo(x + 5, y - 10);
		path.lineTo(x - 5, y + 0);
		path.lineTo(x + 5, y + 10);
		path.lineTo(x - 5, y + 20);
		path.lineTo(x + 5, y + 30);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.arpeggiatorpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		final Spinner typeSpinner = (Spinner) view.findViewById(R.id.arpeggiatorType);
		ArrayAdapter<CharSequence> octaveAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, Arpeggiator.Type.values());
		octaveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(octaveAdapter);
		typeSpinner.setSelection(module.type.ordinal());
		typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.type != (Arpeggiator.Type) typeSpinner.getSelectedItem()) {
					module.type = (Arpeggiator.Type) typeSpinner.getSelectedItem();
					instrument.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});
		((View) typeSpinner.getParent().getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.typeCC));

		final CheckBox repeatBox = (CheckBox) view.findViewById(R.id.arpegiatorRepeat);
		repeatBox.setChecked(module.looping);
		repeatBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.looping = isChecked;
				instrument.moduleUpdated(module);
			}
		});
		repeatBox.setOnLongClickListener(MidiControlDialog.newLongClickListener(module.loopingCC));

		final CheckBox bypassBox = (CheckBox) view.findViewById(R.id.arpegiatorBypass);
		bypassBox.setChecked(module.bypass);
		bypassBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.bypass = isChecked;
				instrument.moduleUpdated(module);
			}
		});
		bypassBox.setOnLongClickListener(MidiControlDialog.newLongClickListener(module.bypassCC));

	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.typeCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					final Spinner typeSpinner = (Spinner) view.findViewById(R.id.arpeggiatorType);
					typeSpinner.setSelection(module.type.ordinal());
				}
			});
		}
		if (module.loopingCC.cc == cc) {
			final CheckBox repeatBox = (CheckBox) view.findViewById(R.id.arpegiatorRepeat);
			repeatBox.setChecked(module.looping);
		}
		if (module.bypassCC.cc == cc) {
			final CheckBox bypassBox = (CheckBox) view.findViewById(R.id.arpegiatorBypass);
			bypassBox.setChecked(module.bypass);
		}
	}

}
