package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.Scope;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Output;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

public class OutputViewer extends ModuleViewer {
	private static final long serialVersionUID = 1L;

	public double left;
	public double right;

	public Scope.Type scopeType;
	
	public Paint clippingPaint;

	public OutputViewer(Module module, Instrument instrument) {
		super(module, instrument);
		clippingPaint = new Paint();
		clippingPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		clippingPaint.setColor(0xFFE00000);
		clippingPaint.setStrokeWidth(4);
		clippingPaint.setStyle(Style.FILL);
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		if (((Output)module).limiter < 0.75) {
			if (((Output)module).limiter < 0.1) {
				clippingPaint.setColor(0xFFFF0000);
			} else if (((Output)module).limiter < 0.25) {
				clippingPaint.setColor(0xFFC06000);
			} else {
				clippingPaint.setColor(0xFFA0A000);
			}
			canvas.drawRect(x - 25, y - 15, x, y + 15, clippingPaint);
			path.reset();
			path.moveTo(x, y - 15);
			path.lineTo(x + 25, y - 30);
			path.lineTo(x + 25, y + 30);
			path.lineTo(x, y + 15);
			canvas.drawPath(path, clippingPaint);
		}
		canvas.drawRect(x - 25, y - 15, x, y + 15, diagramPaint);
		path.reset();
		path.moveTo(x, y - 15);
		path.lineTo(x + 25, y - 30);
		path.lineTo(x + 25, y + 30);
		path.lineTo(x, y + 15);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.speakerpane;
	}

	public transient Scope scope;

	@Override
	public void onViewCreate(MainActivity mainActivity) {
		scope = (Scope) view.findViewById(R.id.outputScope);
		scope.setType(scopeType);
		scope.setClickable(true);
		scope.setFocusable(true);
		scope.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.out.println("Click!");
				if (scopeType == null) {
					scopeType = Scope.Type.Oscilloscope;
				}
				scopeType = Scope.Type.values()[(scopeType.ordinal() + 1) % Scope.Type.values().length];
				scope.setType(scopeType);
//				dirty = true;  annoying
			}
		});
	}

}
