package com.gallantrealm.modsynth.viewer;

import java.util.ArrayList;
import com.gallantrealm.android.RangeSlider;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.XYControl;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Pad;
import com.gallantrealm.modsynth.module.Pad.PadType;
import com.gallantrealm.mysynth.ClientModel;
import com.gallantrealm.mysynth.MySynth;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class PadViewer extends ModuleViewer {
	
	Pad module;
	XYControl xyControl;
	boolean editing;
	private transient boolean padMaximized;
	private transient ArrayList<Integer> indexToKey;
	private transient int nextVoice;
	private transient int lastAction;

	public PadViewer(Module module, MySynth synth) {
		super(module, synth);
		this.module = (Pad)module;
		indexToKey = new ArrayList<Integer>();
		nextVoice = 0;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		canvas.drawRect(x - 35, y - 35, x + 35, y + 35, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.padpane;
	}

	@Override
	public void onViewCreate(final MainActivity mainActivity) {

		xyControl = (XYControl) view.findViewById(R.id.padXYControl);
		xyControl.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int index = event.getActionIndex();
				int pointerCount = event.getPointerCount();
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
					int voice = nextVoice;
					indexToKey.add(voice);
					System.out.println("down " + voice);
					nextVoice = (nextVoice + 1) % module.voices;
					module.pressed[voice] = true;
					module.setX(voice, event.getX(index) / xyControl.getWidth());
					module.setY(voice,  (xyControl.getHeight() - event.getY(index)) / xyControl.getHeight());
					module.dragging = false;
				} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
					for (int i = 0; i < pointerCount; i++) {
						if (i < indexToKey.size()) {
							int voice = indexToKey.get(i);
							module.pressed[voice] = true;
							module.setX(i, event.getX(i) / xyControl.getWidth());
							module.setY(i, (xyControl.getHeight() - event.getY(i)) / xyControl.getHeight());
						}
					}
					if (index < indexToKey.size()) {
						int voice = indexToKey.get(index);
						module.pressed[voice] = true;
						module.setX(voice, event.getX(index) / xyControl.getWidth());
						module.setY(voice, (xyControl.getHeight() - event.getY(index)) / xyControl.getHeight());
					}
					if (lastAction == MotionEvent.ACTION_MOVE) {
						module.dragging = true;
					}
				} else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
					if (index < indexToKey.size()) {
						int voice = indexToKey.get(index);
						System.out.println("up " + voice);
						indexToKey.remove(index);
						module.pressed[voice] = false;
					}
					module.dragging = false;
				} else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
					if (index < indexToKey.size()) {
						int voice = indexToKey.get(index);
						System.out.println("cancel " + voice);
						indexToKey.remove(index);
						module.pressed[voice] = false;
					}
					module.dragging = false;
				}
				lastAction = event.getActionMasked();
				return true;
			}
		});

		final Button edit = (Button) view.findViewById(R.id.padEdit);
		final Button done = (Button) view.findViewById(R.id.padDone);
		final Button max = (Button) view.findViewById(R.id.padMax);

		max.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (padMaximized) {
					mainActivity.modGraphPane.setVisibility(View.VISIBLE);
					mainActivity.operatorPane.setVisibility(View.VISIBLE);
					edit.setVisibility(View.VISIBLE);
					max.setText("[+]");
					padMaximized = false;
				} else {
					mainActivity.modGraphPane.setVisibility(View.GONE);
					mainActivity.operatorPane.setVisibility(View.GONE);
					edit.setVisibility(View.GONE);
					max.setText("[-]");
					padMaximized = true;
				}
			}
		});

		edit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setEditing(true);
			}
		});
		done.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setEditing(false);
			}
		});
		setEditing(editing);

		final Spinner voicesSpinner = (Spinner) view.findViewById(R.id.padVoices);
		ArrayAdapter<CharSequence> voicesAdapter;
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });
		} else {
			voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "1", "2", "3" });
			module.voices = Math.min(3, module.voices);
		}
		voicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voicesSpinner.setAdapter(voicesAdapter);
		voicesSpinner.setSelection(Math.max(0, module.voices - 1));
		voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.voices != voicesSpinner.getSelectedItemPosition() + 1) {
					module.voices = voicesSpinner.getSelectedItemPosition() + 1;
					synth.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		final RangeSlider xRangeSlider = (RangeSlider) view.findViewById(R.id.padXRange);
		xRangeSlider.setThumb1Value((int) (100.0 * module.xMin));
		xRangeSlider.setThumb2Value((int) (100.0 * module.xMax));
		xRangeSlider.setOnRangeChangeListener(new RangeSlider.RangeChangeListener() {
			public void rangeChanged(int thumb1Value, int thumb2Value) {
				module.xMin = thumb1Value / 100.0;
				module.xMax = thumb2Value / 100.0;
				synth.moduleUpdated(module);
				updateXYControl();
			}
		});

		final RangeSlider yRangeSlider = (RangeSlider) view.findViewById(R.id.padYRange);
		yRangeSlider.setThumb1Value((int) (100.0 * module.yMin));
		yRangeSlider.setThumb2Value((int) (100.0 * module.yMax));
		yRangeSlider.setOnRangeChangeListener(new RangeSlider.RangeChangeListener() {
			public void rangeChanged(int thumb1Value, int thumb2Value) {
				module.yMin = thumb1Value / 100.0;
				module.yMax = thumb2Value / 100.0;
				synth.moduleUpdated(module);
				updateXYControl();
			}
		});

		final Spinner xTypeSpinner = (Spinner) view.findViewById(R.id.padXType);
		ArrayAdapter<CharSequence> xTypeAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, PadType.values());
		xTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		xTypeSpinner.setAdapter(xTypeAdapter);
		if (module.xType == null) {
			module.xType = PadType.Continuous;
		}
		xTypeSpinner.setSelection(module.xType.ordinal());
		xTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.xType != (PadType) xTypeSpinner.getSelectedItem()) {
					module.xType = (PadType) xTypeSpinner.getSelectedItem();
					synth.moduleUpdated(module);
					updateXYControl();
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		final Spinner yTypeSpinner = (Spinner) view.findViewById(R.id.padYType);
		ArrayAdapter<CharSequence> yTypeAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, PadType.values());
		yTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		yTypeSpinner.setAdapter(yTypeAdapter);
		if (module.yType == null) {
			module.yType = PadType.Continuous;
		}
		yTypeSpinner.setSelection(module.yType.ordinal());
		yTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.yType != (PadType) yTypeSpinner.getSelectedItem()) {
					module.yType = (PadType) yTypeSpinner.getSelectedItem();
					synth.moduleUpdated(module);
					updateXYControl();
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		updateXYControl();

	}

	private void updateXYControl() {
		if (module.xType == PadType.Continuous) {
			xyControl.setXGradiated(0);
		} else if (module.xType == PadType.Chromatic) {
			xyControl.setXGradiated(12);
		} else if (module.xType == PadType.Major) {
			xyControl.setXGradiated(7);
		}
		xyControl.setMinX(module.xMin);
		xyControl.setMaxX(module.xMax);
		if (module.yType == PadType.Continuous) {
			xyControl.setYGradiated(0);
		} else if (module.yType == PadType.Chromatic) {
			xyControl.setYGradiated(12);
		} else if (module.yType == PadType.Major) {
			xyControl.setYGradiated(7);
		}
		xyControl.setMinY(module.yMin);
		xyControl.setMaxY(module.yMax);
	}

	private void setEditing(boolean editing) {
		final Button edit = (Button) view.findViewById(R.id.padEdit);
		final Button done = (Button) view.findViewById(R.id.padDone);
		final Button max = (Button) view.findViewById(R.id.padMax);
		final XYControl xyControl = (XYControl) view.findViewById(R.id.padXYControl);
		final View controls = view.findViewById(R.id.padControls);
		this.editing = editing;
		if (editing) {
			xyControl.setVisibility(View.GONE);
			edit.setVisibility(View.GONE);
			done.setVisibility(View.VISIBLE);
			max.setVisibility(View.GONE);
			controls.setVisibility(View.VISIBLE);
		} else {
			xyControl.setVisibility(View.VISIBLE);
			edit.setVisibility(View.VISIBLE);
			done.setVisibility(View.GONE);
			max.setVisibility(View.VISIBLE);
			controls.setVisibility(View.GONE);
		}
	}

	private int majorScale(int note) {
		int octave = note / 7;
		int noteOnScale = note % 7;
		int noteEqual = 0;
		switch (noteOnScale) {
		case 0:
			noteEqual = 0;
			break;
		case 1:
			noteEqual = 2;
			break;
		case 2:
			noteEqual = 4;
			break;
		case 3:
			noteEqual = 5;
			break;
		case 4:
			noteEqual = 7;
			break;
		case 5:
			noteEqual = 9;
			break;
		case 6:
			noteEqual = 11;
			break;
		}
		return octave * 12 + noteEqual;
	}

}
