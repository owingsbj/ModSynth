package com.gallantrealm.modsynth;

import com.gallantrealm.android.RangeSlider;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.module.CC;
import com.gallantrealm.mysynth.ClientModel;
import com.gallantrealm.mysynth.GallantDialog;
import com.zeemote.zc.event.ButtonEvent;
import com.zeemote.zc.event.IButtonListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import de.viktorreiser.toolbox.widget.NumberPicker;

public class MidiControlDialog extends GallantDialog implements IButtonListener {

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
	 * This is called by MainActivity whenever a MIDI control changes.  This automatically
	 * selects that control in the dialog.
	 */
	public static void controlChanged(Activity activity, final int cc) {
		if (lastMidiControlDialog != null && lastMidiControlDialog.isShowing()) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					if (lastMidiControlDialog != null && lastMidiControlDialog.controlPick != null) {
						lastMidiControlDialog.controlPick.setCurrent(cc);
					}
				}
			});
		}
	}

	ClientModel clientModel = ClientModel.getClientModel();

	NumberPicker controlPick;
	RangeSlider valueRangeSlider;
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

		okButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				cc.cc = controlPick.getCurrent();
				cc.minRange = valueRangeSlider.getThumb1Value();
				cc.maxRange = valueRangeSlider.getThumb2Value();
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
		try {
			cc.cc = controlPick.getCurrent();
			cc.minRange = valueRangeSlider.getThumb1Value();
			cc.maxRange = valueRangeSlider.getThumb2Value();
		} catch (Exception e) {
		}
		return cc;
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

	boolean controllerWasPressed;

	@Override
	public void buttonPressed(ButtonEvent buttonEvent) {
		controllerWasPressed = true;
	}

	@Override
	public void buttonReleased(ButtonEvent buttonEvent) {
		if (controllerWasPressed) {
			controllerWasPressed = false;
			if (buttonEvent.getButtonGameAction() == ButtonEvent.BUTTON_A) {
//				buttonPressed = 0;
				MidiControlDialog.this.dismiss();
				MidiControlDialog.this.cancel();
			} else if (buttonEvent.getButtonGameAction() == ButtonEvent.BUTTON_B) {
//				buttonPressed = options.length - 1;
				MidiControlDialog.this.dismiss();
				MidiControlDialog.this.cancel();
			}
		}
	}

}
