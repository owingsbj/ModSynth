package com.gallantrealm.modsynth;

import com.gallantrealm.modsynth.module.SpectralFilter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SpectralControl extends View implements OnTouchListener {

	boolean[][] spectralMap;
	int bands = SpectralFilter.BANDS;
	int steps = SpectralFilter.STEPS;
	public boolean painting = true;
	int currentStep = 0;

	public SpectralControl(Context context) {
		super(context);
		init();
	}

	public SpectralControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SpectralControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setOnTouchListener(this);
	}

	public void setSpectralMap(boolean[][] spectralMap) {
		this.spectralMap = spectralMap;
		postInvalidate();
	}

	public void setCurrentStep(int currentStep) {
		this.currentStep = currentStep;
		postInvalidate();
	}

	public int getBands() {
		return bands;
	}

	public void setBands(int bands) {
		this.bands = bands;
	}

	public int getSteps() {
		return steps;
	}

	public void setYGradiated(int steps) {
		this.steps = steps;
	}

	@Override
	public void draw(Canvas canvas) {
		float l = 10;
		float r = getWidth() - 10;
		float t = 10;
		float b = getHeight() - 10;
		float w = getWidth() - 20;
		float h = getHeight() - 20;
		Paint paint = new Paint();

		paint.setColor(0xC0808080);
		canvas.drawRect(new RectF(l, t, r, b), paint);

		// draw spectral map
		if (spectralMap != null) {
			paint.setColor(0x8080FF80);
			RectF rect = new RectF();
			for (int x = 0; x < steps; x += 1) {
				for (int y = 0; y < bands; y += 1) {
					if (spectralMap[x][bands - y - 1]) {
						rect.left = l + w / steps * x;
						rect.right = l + w / steps * (x + 1);
						rect.top = t + h / bands * y;
						rect.bottom = t + h / bands * (y + 1);
						canvas.drawRect(rect, paint);
					}
				}
			}
		}

		// draw x gradient
		paint.setColor(0xFFC0C0C0);
		for (int x = 0; x <= steps; x += 1) {
			float linex = l + w / steps * x;
			canvas.drawLine(linex, t, linex, b, paint);
		}

		// draw y gradient
		paint.setColor(0xFFC0C0C0);
		for (int y = 0; y <= bands; y += 1) {
			float liney = t + h / bands * y;
			canvas.drawLine(l, liney, r, liney, paint);
		}

		// draw a rectangle around the current step
		paint.setColor(0x40FF0000);
		float x1 = l + w / steps * currentStep;
		float x2 = l + w / steps * (currentStep + 1);
		canvas.drawRect(new RectF(x1, t, x2, b), paint);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {

			float l = 10;
			float r = getWidth() - 10;
			float t = 10;
			float b = getHeight() - 10;
			float w = getWidth() - 20;
			float h = getHeight() - 20;
			if (spectralMap != null && event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				int step = (int) ((event.getX() - l) / w * steps);
				step = Math.max(0, Math.min(steps - 1, step));
				int band = bands - (int) ((event.getY() - t) / h * bands) - 1;
				band = Math.max(0, Math.min(bands - 1, band));
				if (painting) {
					spectralMap[step][band] = true;
				} else {
					spectralMap[step][band] = false;
				}
				invalidate();
			}
			return true;
		}
		return false;
	}

}
