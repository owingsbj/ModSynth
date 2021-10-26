package com.gallantrealm.modsynth;

import com.gallantrealm.android.GallantDialog;
import com.gallantrealm.android.RangeSlider;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.module.CC;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import de.viktorreiser.toolbox.widget.NumberPicker;

public class MidiControlDialog extends GallantDialog {

	public static MidiControlDialog lastMidiControlDialog;

	public static View.OnLongClickListener newLongClickListener(final CC cc) {
		return new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				final MidiControlDialog ccSelect = new MidiControlDialog(v.getContext());
				ccSelect.setCC(cc);
				ccSelect.show();
				lastMidiControlDialog = ccSelect;
				return false;
			}
		};
	}

	/**
	 * This is called by MainActivity whenever a MIDI control changes. This automatically selects that control in the dialog.
	 */
	public static void controlChanged(Activity activity, final int cc) {
		if (lastMidiControlDialog != null && lastMidiControlDialog.isShowing()) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					if (lastMidiControlDialog != null && lastMidiControlDialog.controlPick != null) {
						lastMidiControlDialog.controlPick.setCurrent(cc);
						lastMidiControlDialog.updateCC();
					}
				}
			});
		}
	}

	ClientModel clientModel = ClientModel.getClientModel();

	NumberPicker controlPick;
	RangeSlider valueRangeSlider;
	CheckBox invertCheckBox;
	Button okButton;

	CC cc;

	public MidiControlDialog(Context context) {
		super(context, R.style.Theme_Dialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.midicontrol_dialog);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		controlPick = (NumberPicker) findViewById(R.id.controlPick);
		valueRangeSlider = (RangeSlider) findViewById(R.id.valueRangeSlider);
		invertCheckBox = (CheckBox)findViewById(R.id.invertCheckBox);
		okButton = (Button) findViewById(R.id.okButton);

		Typeface typeface = clientModel.getTypeface(getContext());
		if (typeface != null) {
			okButton.setTypeface(typeface);
		}

		controlPick.setCurrent(cc.cc);
		if (cc.rangeLimited) {
			valueRangeSlider.setMinValue(cc.minLimit);
			valueRangeSlider.setMaxValue(cc.maxLimit);
		}
		valueRangeSlider.setThumb1Value(cc.minRange);
		valueRangeSlider.setThumb2Value(cc.maxRange);
		
		controlPick.setOnChangeListener(new NumberPicker.OnChangedListener() {
			public void onChanged(NumberPicker arg0, int arg1, int arg2) {
				updateCC();
			}
		});
		valueRangeSlider.setOnRangeChangeListener(new RangeSlider.RangeChangeListener() {
			public void rangeChanged(int arg0, int arg1) {
				updateCC();
			}
		});

		invertCheckBox.setChecked(cc.invert);

		okButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				updateCC();
				MidiControlDialog.this.dismiss();
				MidiControlDialog.this.cancel();
				return true;
			}

		});

		Translator.getTranslator().translate(this.getWindow().getDecorView());
	}

	public void setCC(CC cc) {
		this.cc = cc;
		if (controlPick != null) {
			controlPick.setCurrent(cc.cc);
			if (cc.rangeLimited) {
				valueRangeSlider.setMinValue(cc.minLimit);
				valueRangeSlider.setMaxValue(cc.maxLimit);
			}
			valueRangeSlider.setThumb1Value(cc.minRange);
			valueRangeSlider.setThumb2Value(cc.maxRange);
		}
	}

	public CC getCC() {
		updateCC();
		return cc;
	}

	public void updateCC() {
		try {
			if (cc != null) {
				cc.cc = controlPick.getCurrent();
				cc.minRange = valueRangeSlider.getThumb1Value();
				cc.maxRange = valueRangeSlider.getThumb2Value();
				cc.invert = invertCheckBox.isChecked();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void dismiss() {
		super.dismiss();
		lastMidiControlDialog = null;
	}

}
