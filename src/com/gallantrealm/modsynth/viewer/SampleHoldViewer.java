package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.MySynth;

import android.graphics.Canvas;

public class SampleHoldViewer extends ModuleViewer {

	public SampleHoldViewer(Module module, MySynth synth) {
		super(module, synth);
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 30, y + 25);
		path.lineTo(x - 20, y + 25);
		path.lineTo(x - 20, y - 25);
		path.lineTo(x - 10, y - 25);
		path.lineTo(x - 10, y + 0);
		path.lineTo(x - 0, y + 0);
		path.lineTo(x + 0, y + 10);
		path.lineTo(x + 10, y + 10);
		path.lineTo(x + 10, y + 20);
		path.lineTo(x + 20, y + 20);
		path.lineTo(x + 20, y - 10);
		path.lineTo(x + 30, y - 10);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.sampleholdpane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {
	}

}
