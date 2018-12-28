package com.gallantrealm.modsynth;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class WaveEditor extends View implements View.OnTouchListener {

	Timer timer = new Timer();

	TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			postInvalidate();
		}
	};

	public WaveEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		startTimer();
		setOnTouchListener(this);
	}

	public WaveEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		startTimer();
		setOnTouchListener(this);
	}

	public WaveEditor(Context context) {
		super(context);
		startTimer();
		setOnTouchListener(this);
	}

	public double[] wave;

	@Override
	public void draw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();
		Paint paint = new Paint();
		
		// clear background
		paint.setColor(0xC0000800);
		canvas.drawRect(new Rect(0,0,w,h), paint);
		
		// draw grid
		paint.setColor(0x80208020);
		canvas.drawLine(0, h / 2, w, h / 2, paint);
		
		// draw wave
		paint.setColor(0x8040FF40);
		if (wave != null) {
			paint.setStrokeWidth(w / 100);
			float scaleX = w / (float) wave.length;
			float scaleY = h / 2.0f;
			for (int x = 1; x < wave.length; x++) {
				canvas.drawLine(scaleX * (x - 1), scaleY * (-(float)wave[x - 1] + 1.0f), scaleX * x, scaleY * (-(float)wave[x] + 1.0f), paint);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		stopTimer();
		super.finalize();
	}

	public void startTimer() {
		timer.schedule(timerTask, 100, 100);
	}

	public void stopTimer() {
		timer.cancel();
	}

	int lastI;
	float lastJ;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (wave != null) {
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				int i = Math.min(wave.length - 1, Math.max(0, (int) (event.getX() / getWidth() * wave.length)));
				float j =  ((1.0f - 2.0f *Math.max(0, Math.min(getHeight(), event.getY())) / getHeight()));
				wave[i] = j;
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (lastI < i) {
						for (int x = lastI; x < i; x++) {
							float t = (x - lastI) / (float) (i - lastI);
							wave[x] =  (lastJ * (1.0f - t) + j * t);
						}
					} else if (lastI > i) {
						for (int x = i; x < lastI; x++) {
							float t = (x - i) / (float) (lastI - i);
							wave[x] =  (j * (1.0f - t) + lastJ * t);
						}
					}
				}
				lastI = i;
				lastJ = j;
			}
		}
		return true;
	}
}
