package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Compressor;
import com.gallantrealm.modsynth.module.Module;
import android.graphics.Canvas;
import android.view.View;
import android.widget.SeekBar;

public class CompressorViewer extends ModuleViewer {
	
	private Compressor module;

	public CompressorViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Compressor)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 25, y - 25);
		path.lineTo(x + 25, y);
		path.moveTo(x - 25, y - 12);
		path.lineTo(x + 25, y);
		path.moveTo(x - 25, y);
		path.lineTo(x + 25, y);
		path.moveTo(x - 25, y + 12);
		path.lineTo(x + 25, y);
		path.moveTo(x - 25, y + 25);
		path.lineTo(x + 25, y);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.compressorpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		SeekBar compressorAmount = (SeekBar) view.findViewById(R.id.compressorAmount);
		compressorAmount.setProgress((int) (10 * module.amount));
		compressorAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.amount = progress / 10.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View)compressorAmount.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.amountCC));

		SeekBar compressorDelay = (SeekBar) view.findViewById(R.id.compressorDelay);
		compressorDelay.setProgress((int) (100 * module.delay));
		compressorDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.delay = progress / 100.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View)compressorDelay.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.delayCC));

		SeekBar compressorGain = (SeekBar) view.findViewById(R.id.compressorGain);
		compressorGain.setProgress((int) (10 * module.gain));
		compressorGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.gain = progress / 10.0;
				instrument.moduleUpdated(module);
			}
		});
		((View)compressorGain.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.gainCC));

	}

	transient double ddelay;

	transient double max;

	@Override
	public void updateCC(int cc, double value) {
		if (module.amountCC.cc == cc) {
			if (view != null) {
				SeekBar compressorAmount = (SeekBar) view.findViewById(R.id.compressorAmount);
				compressorAmount.setProgress((int) (10 * module.amount));
			}
		}
		if (module.delayCC.cc== cc) {
			if (view != null) {
				SeekBar compressorDelay = (SeekBar) view.findViewById(R.id.compressorDelay);
				compressorDelay.setProgress((int) (100 * module.delay));
			}
		}
		if (module.gainCC.cc== cc) {
			if (view != null) {
				SeekBar compressorGain = (SeekBar) view.findViewById(R.id.compressorGain);
				compressorGain.setProgress((int) (10 * module.gain));
			}
		}
	}

}
