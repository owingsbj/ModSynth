package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.Module.Link;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Canvas.EdgeType;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ModuleViewer {

	public Module module;
	public View view;
	public String translatedTitle;
	public Instrument instrument;

	protected static Paint paint, fillpaint, diagramPaint, linkpaint, selectedpaint, unselectedpaint, shadowpaint, titlepaint, subpaint;
	protected static Path path;

	public ModuleViewer(Module module, Instrument instrument) {
		this.module = module;
		this.instrument = instrument;

		paint = new Paint();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xFFFFFFFF);
		paint.setStrokeWidth(2);
		paint.setStyle(Style.STROKE);

		linkpaint = new Paint();
		linkpaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		linkpaint.setColor(0xFFE0E0E0);
		linkpaint.setStrokeWidth(3);
		linkpaint.setStyle(Style.STROKE);

		fillpaint = new Paint();
		fillpaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		fillpaint.setColor(0xFFFFFFFF);
		fillpaint.setStrokeWidth(2);
		fillpaint.setStyle(Style.FILL_AND_STROKE);

		diagramPaint = new Paint();
		diagramPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		diagramPaint.setColor(0xFFD0D0D0);
		diagramPaint.setStrokeWidth(4);
		diagramPaint.setStyle(Style.STROKE);

		selectedpaint = new Paint();
		selectedpaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		selectedpaint.setStrokeWidth(2);
		selectedpaint.setStyle(Style.FILL);

		unselectedpaint = new Paint();
		unselectedpaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		unselectedpaint.setStrokeWidth(2);
		unselectedpaint.setStyle(Style.FILL);

		shadowpaint = new Paint();
		shadowpaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		shadowpaint.setColor(0x60000000);
		shadowpaint.setStrokeWidth(2);
		shadowpaint.setStyle(Style.FILL_AND_STROKE);

		titlepaint = new Paint();
		titlepaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		titlepaint.setColor(0xFFFFFFFF);
		titlepaint.setStyle(Style.FILL_AND_STROKE);
		titlepaint.setTextSize(16);
		titlepaint.setStrokeWidth(0);

		subpaint = new Paint();
		subpaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		subpaint.setTextSize(11);
		subpaint.setStrokeWidth(0.5f);
		subpaint.setColor(0xFFFFFFFF);
		subpaint.setStyle(Style.FILL_AND_STROKE);

		path = new Path();
	}

	public abstract int getViewResource();

	public final View getView(MainActivity mainActivity, ViewGroup parentView) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mainActivity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(getViewResource(), null);
			Translator.getTranslator().translate(view);
			parentView.addView(view);
			onViewCreate(mainActivity);
		}
		return view;
	}

	public final void dropView() {
		if (view != null) {
			((ViewGroup) view.getParent()).removeView(view);
			view = null;
		}
	}

	public abstract void onViewCreate(MainActivity mainActivity);

	public void drawDiagram(Canvas canvas, float x, float y) {
		canvas.drawCircle(x, y, 30, diagramPaint);
	}

	public void draw(Instrument instrument, Resources resources, Canvas canvas, boolean editing, boolean colorIcons) {
		if (!canvas.quickReject(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight(), EdgeType.AA)) {
			// Draw the box (with shadow first)
			canvas.drawRoundRect(new RectF(module.xPosition + 2, module.yPosition + 2, module.xPosition + 6 + module.getWidth(), module.yPosition + 6 + module.getHeight()), 12, 12, shadowpaint);
			if (module.isSelected) {
				Shader shader = new LinearGradient(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight(), //
						0xC0CCCCCC, 0xC0888888, TileMode.MIRROR);
				selectedpaint.setShader(shader);
				canvas.drawRoundRect(new RectF(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight()), 10, 10, selectedpaint);
				canvas.drawRoundRect(new RectF(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight()), 10, 10, paint);
			} else {
				Shader shader = new LinearGradient(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight(), //
						0xC0888888, 0xC0444444, TileMode.MIRROR);
				unselectedpaint.setShader(shader);
				canvas.drawRoundRect(new RectF(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight()), 10, 10, unselectedpaint);
				paint.setColor(0xFFC0C0C0);
				canvas.drawRoundRect(new RectF(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight()), 10, 10, paint);
				paint.setColor(0xFFFFFFFF);
			}

			// Draw the title
//			if (translatedTitle == null) {
				translatedTitle = Translator.getTranslator().translate(module.getClass().getSimpleName());
//			}
			float titleWidth = titlepaint.measureText(translatedTitle);
			canvas.drawText(translatedTitle, module.xPosition + module.getWidth() / 2 - titleWidth / 2, module.yPosition + 16, titlepaint);

			// Draw the diagram
			if (module != null) {
				if (colorIcons) {
					int red = (int) (-Math.max(-1, module.lastmin * 2) * 255);
					int green = (int) (Math.min(1, module.lastmax * 2) * 255);
					int blue = 0;
					int levelColor = Color.rgb(red, green, blue);
					diagramPaint.setColor(levelColor);
				} else {
					diagramPaint.setColor(0xFFD0D0D0);
				}
				diagramPaint.setStyle(Style.STROKE);
				drawDiagram(canvas, module.xPosition + 50, module.yPosition + 7 + module.getHeight() / 2);
			}

			// If editing, draw "nubs" at outputs
			if (editing) {
				for (int i = 0; i < module.getOutputCount(); i++) {
					canvas.drawLine(module.getOutputX(i), module.getOutputY(i), module.getOutputX(i) + 5, module.getOutputY(i), linkpaint);
					boolean linked = false;
					for (Module otherModule : instrument.modules) {
						if (otherModule.input1 != null && otherModule.input1.module == module && otherModule.input1.value == module.getOutput(i).value) {
							linked = true;
						}
						if (otherModule.input2 != null && otherModule.input2.module == module && otherModule.input2.value == module.getOutput(i).value) {
							linked = true;
						}
						if (otherModule.input3 != null && otherModule.input3.module == module && otherModule.input3.value == module.getOutput(i).value) {
							linked = true;
						}
						if (otherModule.mod1 != null && otherModule.mod1.module == module && otherModule.mod1.value == module.getOutput(i).value) {
							linked = true;
						}
						if (otherModule.mod2 != null && otherModule.mod2.module == module && otherModule.mod2.value == module.getOutput(i).value) {
							linked = true;
						}
					}
					if (!linked) {
						String text = Translator.getTranslator().translate(module.getOutputName(i));
						canvas.drawText(text, module.getOutputX(i) + 5, module.getOutputY(i) + 3, subpaint);
					}
				}
			}
		}

		// Draw the output->input links
		for (int i = 0; i < module.getInputCount(); i++) {
			float startx;
			float starty;
			float endx;
			float endy;
			Link link = null;
			if (i == 0) {
				link = module.input1;
			} else if (i == 1) {
				link = module.input2;
			} else if (i == 2) {
				link = module.input3;
			} else {
				link = module.input4;
			}
			endx = module.getInputX(i);
			endy = module.getInputY(i);
			if (link == null) {
				if (editing) { // draw input nubs and labels
					if (!canvas.quickReject(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight(), EdgeType.AA)) {
						canvas.drawLine(endx - 5, endy, endx, endy, linkpaint);
						String text = Translator.getTranslator().translate(module.getInputName(i));
						canvas.drawText(text, endx - 5 - subpaint.measureText(text), endy + 3, subpaint);
					}
				}
			} else { // draw links
				startx = link.module.getOutputX(link.outputN - 1);
				starty = link.module.getOutputY(link.outputN - 1);
				path.reset();
				path.moveTo(startx, starty);
				path.cubicTo(startx + 25, starty, endx - 35, endy, endx - 10, endy);
				canvas.drawPath(path, linkpaint);
				path.reset();
				path.moveTo(endx - 10, endy - 5);
				path.lineTo(endx, endy);
				path.lineTo(endx - 10, endy + 5);
				canvas.drawPath(path, fillpaint);
			}
		}

		// Draw the output->mod links
		for (int i = 0; i < module.getModCount(); i++) {
			float startx;
			float starty;
			float endx;
			float endy;
			Link link = null;
			if (i == 0) {
				link = module.mod1;
			} else {
				link = module.mod2;
			}
			endx = module.getModX(i);
			endy = module.getModY(i);
			if (link == null) {
				if (editing) { // draw input nubs
					if (!canvas.quickReject(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight(), EdgeType.AA)) {
						String text = Translator.getTranslator().translate(module.getModName(i));
						if (i == 0) { // modulator link
							canvas.drawLine(endx, endy + 5, endx, endy, linkpaint);
							canvas.drawText(text, endx - subpaint.measureText(text) / 2, endy + 17, subpaint);
						} else { // top controller link
							canvas.drawLine(endx, endy - 5, endx, endy, linkpaint);
							canvas.drawText(text, endx - subpaint.measureText(text) / 2, endy - 12, subpaint);
						}
					}
				}
			} else { // draw links
				startx = link.module.getOutputX(link.outputN - 1);
				starty = link.module.getOutputY(link.outputN - 1);
				if (i == 0) { // modulator link
					path.reset();
					path.moveTo(startx, starty);
					path.cubicTo(startx + 25, starty, endx, endy + 35, endx, endy + 10);
					canvas.drawPath(path, linkpaint);
					path.reset();
					path.moveTo(endx - 5, endy + 10);
					path.lineTo(endx, endy);
					path.lineTo(endx + 5, endy + 10);
					canvas.drawPath(path, fillpaint);
				} else { // top controller link
					path.reset();
					path.moveTo(startx, starty);
					path.cubicTo(startx + 25, starty, endx, endy - 35, endx, endy - 10);
					canvas.drawPath(path, linkpaint);
					path.reset();
					path.moveTo(endx - 5, endy - 10);
					path.lineTo(endx, endy);
					path.lineTo(endx + 5, endy - 10);
					canvas.drawPath(path, fillpaint);
				}
			}
		}

	}

	/**
	 * Invoked for result of a file chooser or other activities.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	/**
	 * Override in viewers to respond to changes in MIDI controls.
	 * 
	 * @param cc
	 * @param value
	 */
	public void updateCC(int cc, double value) {
	}
}
