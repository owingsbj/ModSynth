package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.MultiOsc;
import android.graphics.Canvas;
import android.view.View;
import android.widget.SeekBar;

public class MultiOscViewer extends OscillatorViewer {
	
	MultiOsc module;

	public MultiOscViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (MultiOsc)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		for (int i = -1; i < 2; i++) {
			float ix = x + 5 * i;
			path.reset();
			path.moveTo(ix - 30, y - 15);
			path.lineTo(ix - 30, y - 25);
			path.lineTo(ix, y - 25);
			path.lineTo(ix, y - 5);
			path.lineTo(ix + 30, y - 5);
			path.lineTo(ix + 30, y - 15);
			path.moveTo(ix - 30, y + 15);
			path.lineTo(ix - 20, y + 5);
			path.lineTo(ix + 20, y + 25);
			path.lineTo(ix + 30, y + 15);
			canvas.drawPath(path, diagramPaint);
		}
	}

	@Override
	public int getViewResource() {
		return R.layout.oscillatorpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		super.onViewCreate(mainActivity);

		View oscNoiseRow = view.findViewById(R.id.oscillatorNoiseRow);
		oscNoiseRow.setVisibility(View.GONE);

		final View oscWidthRow = view.findViewById(R.id.oscillatorWidthRow);
		oscWidthRow.setVisibility(View.VISIBLE);
		final SeekBar oscWidth = (SeekBar) view.findViewById(R.id.oscillatorWidth);
		oscWidth.setProgress(Math.max(1, (int) (Math.pow(module.chorusWidth / 12.0, 0.5) * 100)));
		oscWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.chorusWidth = 12.0 * Math.pow(Math.max(1, progress) / 100.0, 2.0);
				instrument.moduleUpdated(module);
			}
		});
		((View)oscWidth.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.chorusWidthCC));

	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.chorusWidthCC.cc == cc) {
			if (view != null) {
				SeekBar oscWidth = (SeekBar) view.findViewById(R.id.oscillatorWidth);
				oscWidth.setProgress(Math.max(1, (int) (Math.pow(module.chorusWidth / 12.0, 0.5) * 100)));
			}
		}
	}

}
