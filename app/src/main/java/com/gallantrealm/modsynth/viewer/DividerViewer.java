package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Divider;
import com.gallantrealm.modsynth.module.Module;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import de.viktorreiser.toolbox.widget.NumberPicker;

public class DividerViewer extends ModuleViewer {

	Divider module;

	public DividerViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Divider) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		float originalStrokeWidth = diagramPaint.getStrokeWidth();
		diagramPaint.setStrokeWidth(10);
		diagramPaint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawLine(x, y - 30, x, y - 25, diagramPaint);
		canvas.drawLine(x - 30, y, x + 30, y, diagramPaint);
		canvas.drawLine(x, y + 25, x, y + 30, diagramPaint);
		diagramPaint.setStrokeWidth(originalStrokeWidth);
	}

	@Override
	public int getViewResource() {
		return R.layout.dividerpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {
		NumberPicker delayLevelBar = (NumberPicker) view.findViewById(R.id.dividerDivisor);
		delayLevelBar.setCurrent((int) (module.divisor));
		delayLevelBar.setOnChangeListener(new NumberPicker.OnChangedListener() {
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				module.divisor = newVal;
				instrument.moduleUpdated(module);
			}
		});
//		((View)delayLevelBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.divisorlCC));

		NumberPicker delayTimeBar = (NumberPicker) view.findViewById(R.id.dividerPhase);
		delayTimeBar.setCurrent((int) (module.phase));
		delayTimeBar.setOnChangeListener(new NumberPicker.OnChangedListener() {
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				module.phase = newVal;
				instrument.moduleUpdated(module);
			}
		});
//		((View)delayTimeBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.phaseCC));

		NumberPicker dutyTimeBar = (NumberPicker) view.findViewById(R.id.dividerDuty);
		dutyTimeBar.setCurrent((int) (module.duty));
		dutyTimeBar.setOnChangeListener(new NumberPicker.OnChangedListener() {
			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
				module.duty = newVal;
				instrument.moduleUpdated(module);
			}
		});
//		((View)delayTimeBar.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.phaseCC));
	
		CheckBox positiveCheckBox = (CheckBox)view.findViewById(R.id.dividerPositive);
		positiveCheckBox.setChecked(module.positive);
		positiveCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.positive = isChecked;
				instrument.moduleUpdated(module);
			}
		});
		
	}

}
