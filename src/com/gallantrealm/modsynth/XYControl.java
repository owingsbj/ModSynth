package com.gallantrealm.modsynth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class XYControl extends View {

	int xGradiated;
	int yGradiated;
	double minX;
	double maxX;
	double minY;
	double maxY;

	public XYControl(Context context) {
		super(context);
	}

	public XYControl(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public XYControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public int getXGradiated() {
		return xGradiated;
	}

	public void setXGradiated(int xGradiated) {
		this.xGradiated = xGradiated;
	}

	public int getYGradiated() {
		return yGradiated;
	}

	public void setYGradiated(int yGradiated) {
		this.yGradiated = yGradiated;
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	@Override
	public void draw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();
		Paint paint = new Paint();

		paint.setColor(0xC0808080);
		canvas.drawRect(new Rect(10, 10, w - 10, h - 10), paint);
		
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(1);

		// draw x gradient
		if (xGradiated > 0) {
			for (int note = (int)(minX * 100); note <= (int)(maxX*100); note += 1) {
				if (note % xGradiated == 0) {
					paint.setColor(0xFF000000);
				} else {
					paint.setColor(0xFFC0C0C0);
				}
				double roundx = note / 100.0;
				float linex = (float) ((roundx - minX) / (maxX - minX) * getWidth());
				if (linex >= 10 && linex <= getWidth() - 10) {
					canvas.drawLine(linex, 10, linex, getHeight() - 10, paint);
				}
			}
		}

		// draw y gradient
		if (yGradiated > 0) {
			for (int note = (int)(minY*100); note <= (int)(maxY*100); note += 1) {
				if (note % yGradiated == 0) {
					paint.setColor(0xFF000000);
				} else {
					paint.setColor(0xFFC0C0C0);
				}
				double roundy = note / 100.0;
				float liney = (float) ((roundy - minY) / (maxY - minY) * getHeight());
				if (liney >= 10 && liney <= getHeight() - 10) {
					canvas.drawLine(10, liney, getWidth() - 10, liney, paint);
				}
			}
		}

		// draw a etched border
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

}
