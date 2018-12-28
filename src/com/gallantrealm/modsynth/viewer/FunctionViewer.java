package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Function;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.MySynth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class FunctionViewer extends ModuleViewer {

	Function module;

	public FunctionViewer(Module module, MySynth synth) {
		super(module, synth);
		this.module = (Function) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		Paint paint = new Paint();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xFFFFFFFF);
		paint.setStrokeWidth(2);
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setTextSize(50);
		paint.setTextSkewX(-0.25f);
		canvas.drawText("f( )", x - 35, y + 15, paint);
	}

	@Override
	public int getViewResource() {
		return R.layout.functionpane;
	}

	@Override
	public void onViewCreate(final MainActivity mainActivity) {

		final EditText algSource = (EditText) view.findViewById(R.id.algSource);
		algSource.setText(module.expressionString);
		if (module.expression == null) {
			algSource.setTextColor(0xFFFF0000);
		}

		algSource.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				System.out.println("ACTION: "+actionId);
				if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					if (event == null || !event.isShiftPressed()) {
						module.expressionString = algSource.getText().toString();
						try {
							module.expression = null;
							module.expression = module.parse(module.expressionString);
							algSource.setTextColor(0xFF000000);
						} catch (Exception e) {
							e.printStackTrace();
							algSource.setTextColor(0xFFFF0000);
						}
						InputMethodManager imm = (InputMethodManager) algSource.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(algSource.getApplicationWindowToken(), 0);
						return true; // consume.
					}
				}
				return false; // pass on to other listeners.
			}
		});
		
		SeekBar algControlA = (SeekBar) view.findViewById(R.id.algControlA);
		algControlA.setProgress((int) (module.a * 100.0));
		algControlA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.a = progress / 100.0;
				synth.moduleUpdated(module);
			}
		});
		((View) algControlA.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.aCC));

		SeekBar algControlB = (SeekBar) view.findViewById(R.id.algControlB);
		algControlB.setProgress((int) (module.b * 100.0));
		algControlB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.b = progress / 100.0;
				synth.moduleUpdated(module);
			}
		});
		((View) algControlB.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.bCC));

		SeekBar algControlC = (SeekBar) view.findViewById(R.id.algControlC);
		algControlC.setProgress((int) (module.c * 100.0));
		algControlC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.c = progress / 100.0;
				synth.moduleUpdated(module);
			}
		});
		((View) algControlC.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.cCC));
	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.aCC.cc == cc) {
			if (view != null) {
				SeekBar algControlA = (SeekBar) view.findViewById(R.id.algControlA);
				algControlA.setProgress((int) (module.a * 100.0));
			}
		}
		if (module.bCC.cc == cc) {
			if (view != null) {
				SeekBar algControlB = (SeekBar) view.findViewById(R.id.algControlB);
				algControlB.setProgress((int) (module.b * 100.0));
			}
		}
		if (module.cCC.cc == cc) {
			if (view != null) {
				SeekBar algControlC = (SeekBar) view.findViewById(R.id.algControlC);
				algControlC.setProgress((int) (module.c * 100.0));
			}
		}
	}

}
