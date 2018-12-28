package com.gallantrealm.modsynth;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class Scope extends View {

	public enum Type {
		Oscilloscope, Flower // , Waterfall
	};

	Timer timer = new Timer();

	TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			if (!zeroed) {
				postInvalidate();
			}
		}
	};

	private boolean zeroed = true;
	private static int WAVE_SIZE = 500;
	private Type type = Type.Oscilloscope;

	float[] wave = new float[WAVE_SIZE];
	float[] newwave = new float[WAVE_SIZE];
	int ptr;
	float lastValue;

	public Scope(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Scope(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Scope(Context context) {
		super(context);
	}

	public final void setType(Type type) {
		this.type = type;
		postInvalidate();
	}

	private float center;

	public final void scope(float value) {
		center = (999.0f * center + value) * 0.001f;
		if (ptr == 0) {
			if (lastValue <= center + 0.001f && value >= center - 0.001f) {
				newwave[ptr] = value;
				ptr++;
			}
			// wait to sync
		} else {
			newwave[ptr] = value;
			ptr++;
			if (ptr >= WAVE_SIZE - 1) {
				float[] t = wave;
				wave = newwave;
				newwave = t;
				ptr = 0;
			}
		}
		lastValue = value;
		zeroed = false;
	}

	public void zero() {
		for (int i = 0; i < wave.length; i++) {
			wave[i] = 0;
		}
		zeroed = true;
		postInvalidate();
	}

	@Override
	public void draw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();
		Paint paint = new Paint();

		// paint wave
		if (wave == null) {
			paint.setColor(0x80000000);
			canvas.drawRect(new Rect(0, 0, w, h), paint);
		} else {

			if (type == Type.Flower) {
				paint.setColor(0x80000000);
				canvas.drawRect(new Rect(0, 0, w, h), paint);
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(0xFF40FF40);
				paint.setStrokeWidth(w / 100);
				double scale = Math.max(w, h) * 2.0;
				double scaleR = 360.0 / wave.length;
				for (int x = 1; x < wave.length; x++) {
					double startX = w / 2.0 + Math.sin(Math.toRadians((x - 1) * scaleR)) * (wave[x - 1] * scale);
					double startY = h / 2.0 + Math.cos(Math.toRadians((x - 1) * scaleR)) * (wave[x - 1] * scale);
					double endX = w / 2.0 + Math.sin(Math.toRadians(x * scaleR)) * (wave[x] * scale);
					double endY = h / 2.0 + Math.cos(Math.toRadians(x * scaleR)) * (wave[x] * scale);
					if (wave[x] < 0) {
						paint.setColor(0xFFFF4040);
					} else {
						paint.setColor(0xFF4040FF);
					}
					canvas.drawLine((float) startX, (float) startY, (float) endX, (float) endY, paint);
				}
//			} else if (type == Type.Waterfall) {
//				// shift everything to the left:
//				var imageData = context.getImageData(1, 0, context.canvas.width-1, context.canvas.height);
//				context.putImageData(imageData, 0, 0);
//				// now clear the right-most pixels:
//				context.clearRect(context.canvas.width-1, 0, 1, context.canvas.height);
//				
			} else { // oscilloscope
				paint.setColor(0x80000000);
				canvas.drawRect(new Rect(0, 0, w, h), paint);
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(0xFF40FF40);
				paint.setStrokeWidth(w / 100);
				float scaleX = w / (float) wave.length;
				float scaleY = h;
				Path path = new Path();
				for (int x = 0; x < wave.length; x++) {
					if (x == 0) {
						path.moveTo(scaleX * x, scaleY * (-wave[x] + 0.5f));
					} else {
						path.lineTo(scaleX * x, scaleY * (-wave[x] + 0.5f));
					}
				}
				canvas.drawPath(path, paint);
			}
		}

		// draw a border
		canvas.scale(1, 1);
		float t = 0.0f + this.getScrollY();
		float l = 0.0f + this.getScrollX();
		float r = this.getWidth() + this.getScrollX();
		float b = this.getHeight() + this.getScrollY();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		paint.setColor(0xC0404040);
		canvas.drawLine(l, t, l, b, paint);
		canvas.drawLine(l, t, r, t, paint);
		paint.setStrokeWidth(5);
		paint.setColor(0xC0C0C0C0);
		canvas.drawLine(r, b, l, b, paint);
		canvas.drawLine(r, b, r, t, paint);
	}

	@Override
	protected void finalize() throws Throwable {
		stopTimer();
		super.finalize();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		startTimer();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopTimer();
	}

	public void startTimer() {
		try {
			timer.schedule(timerTask, 100, 100);
			System.out.println("SCOPE STARTED!!!");
		} catch (Exception e) {
			// can happen if timer cancelled
		}
	}

	public void stopTimer() {
		timer.cancel();
		System.out.println("SCOPE STOPPED!!!");
	}

	int lastI;
	int lastJ;

}
