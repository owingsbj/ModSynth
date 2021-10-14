package com.gallantrealm.modsynth;

import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.viewer.ModuleViewer;
import com.gallantrealm.mysynth.MySynth;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class ModGraph extends View implements View.OnTouchListener, View.OnClickListener {

	static final float GRID = 12.5f;

	private static int near = 25;

	public interface OnSelectionListener {
		public void selected(Module module);
	}

	float d;
	private Instrument instrument;
	float zoom = 0.5f;
	private boolean editing = false;
	float startX, startY;
	float startX2, startY2;
	int startScrollX, startScrollY;
	Module startModule;
	Module.Link startLink;
	float linkX, linkY;
	float startModuleX, startModuleY;
	boolean moving;
	boolean linking;
	boolean scrolling;
	boolean zooming;
	float startZoom;
	OnSelectionListener onSelectionListener;
	MySynth synth;
	boolean colorIcons;

	public ModGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ModGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ModGraph(Context context) {
		super(context);
		init();
	}

	private void init() {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		setZoom(0.75f);

		// establish gesture handling
		this.setClickable(true);
		setOnTouchListener(this);
		setOnClickListener(this);

		setFocusable(true);

//		setLayerType(View.LAYER_TYPE_SOFTWARE, null);  // so shadows draw

	}

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float newzoom) {
		zoom = Math.min(Math.max(newzoom, 0.5f), 2.0f);
		// Set up d, the display density
		DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
		float widthPixels = Math.max(displayMetrics.heightPixels, displayMetrics.widthPixels);
		float dpi = Math.max(displayMetrics.xdpi, displayMetrics.ydpi);
		float width = widthPixels / dpi;
		d = zoom * dpi / 100.0f * (width / 10.0f);
		invalidate();
	}

	public boolean getColorIcons() {
		return colorIcons;
	}

	public void setColorIcons(boolean colorIcons) {
		this.colorIcons = colorIcons;
		postInvalidate();
	}

	private Module getModuleAt(float x, float y) {
		if (instrument == null || instrument.modules == null) {
			return null;
		}
		Module moduleAt = null;
		for (int i = 0; i < instrument.modules.size(); i++) {
			Module module = instrument.modules.get(i);
			if (module.isOnModule(x / d, y / d)) {
				moduleAt = module;
			}
		}
		return moduleAt;
	}

	private Module getModuleNear(float x, float y) {
		if (instrument == null || instrument.modules == null) {
			return null;
		}
		Module moduleAt = null;
		for (int i = 0; i < instrument.modules.size(); i++) {
			Module module = instrument.modules.get(i);
			if (nearModule(module, x / d, y / d)) {
				moduleAt = module;
			}
		}
		return moduleAt;
	}

	private boolean nearModule(Module module, float x, float y) {
		if (x >= module.xPosition - near && x <= module.xPosition + near + module.getWidth()) {
			if (y >= module.yPosition - near && y <= module.yPosition + near + module.getHeight()) {
				return true;
			}
		}
		return false;
	}

	private boolean nearOutput(Module module, int n, float x, float y) {
		if (zoom < 0.75) {
			return false;
		}
		return x >= module.getOutputX(n) - near && x <= module.getOutputX(n) + near //
				&& y >= module.getOutputY(n) - near && y <= module.getOutputY(n) + near;
	}

	private Module.Link getOutputAt(Module module, float x, float y) {
		x = x / d;
		y = y / d;
		if (module != null) {
			for (int i = 0; i < module.getOutputCount(); i++) {
				if (nearOutput(module, i, x, y)) {
					return module.getOutput(i);
				}
			}
		}
		return null;
	}

	private boolean nearInput(Module module, int n, float x, float y) {
		if (zoom < 0.75) {
			return false;
		}
		return x >= module.getInputX(n) - near && x <= module.getInputX(n) + near //
				&& y >= module.getInputY(n) - near && y <= module.getInputY(n) + near;
	}

	private int getInputAt(Module module, float x, float y) {
		x = x / d;
		y = y / d;
		if (module != null) {
			for (int i = 0; i < module.getInputCount(); i++) {
				if (nearInput(module, i, x, y)) {
					return i + 1;
				}
			}
		}
		return 0;
	}

	private boolean nearMod(Module module, int n, float x, float y) {
		if (zoom < 0.75) {
			return false;
		}
		return x >= module.getModX(n) - near && x <= module.getModX(n) + near //
				&& y >= module.getModY(n) - near && y <= module.getModY(n) + near;
	}

	private int getModAt(Module module, float x, float y) {
		x = x / d;
		y = y / d;
		if (module != null) {
			for (int i = 0; i < module.getModCount(); i++) {
				if (nearMod(module, i, x, y)) {
					return i + 1;
				}
			}
		}
		return 0;
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
			if (event.getPointerCount() > 1) {
				startX = event.getX(0);
				startY = event.getY(0);
				startX2 = event.getX(1);
				startY2 = event.getY(1);
				startZoom = zoom;
				startScrollX = getScrollX();
				startScrollY = getScrollY();
				moving = false;
				linking = false;
				scrolling = false;
				zooming = true;
				return true;
			}
			startX = event.getX();
			startY = event.getY();
			startScrollX = getScrollX();
			startScrollY = getScrollY();
			startModule = getModuleAt(startX + startScrollX, startY + startScrollY);
			if (editing) {
				Module nearModule = getModuleNear(startX + startScrollX, startY + startScrollY);
				startLink = getOutputAt(nearModule, startX + startScrollX, startY + startScrollY);
				if (startLink == null) {
					int input = getInputAt(nearModule, startX + startScrollX, startY + startScrollY);
					if (input == 1) {
						startLink = nearModule.input1;
					} else if (input == 2) {
						startLink = nearModule.input2;
					} else if (input == 3) {
						startLink = nearModule.input3;
					} else if (input == 4) {
						startLink = nearModule.input4;
					}
					int mod = getModAt(nearModule, startX + startScrollX, startY + startScrollY);
					if (mod == 1) {
						startLink = nearModule.mod1;
					} else if (mod == 2) {
						startLink = nearModule.mod2;
					}
				}
				if (startLink != null) {
					startModule = startLink.module;
				}
			}
			if (startModule != null) {
				startModuleX = startModule.xPosition;
				startModuleY = startModule.yPosition;
			}
			moving = false;
			linking = false;
			scrolling = false;
			zooming = false;
		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
			if (zooming) {
				if (event.getPointerCount() > 1) { // zoom
					zooming = true;
					moving = false;
					scrolling = false;
					linking = false;
					float initialDelX = startX - startX2;
					float nowDelX = event.getX(0) - event.getX(1);
					float newZoom = Math.abs(nowDelX / initialDelX);
					setZoom(startZoom * newZoom);
					scrollTo((int) (Math.max(0, startScrollX * newZoom + startX - event.getX(0))), (int) (Math.max(0, startScrollY * newZoom + startY - event.getY(0))));
					return true;
				}
			} else {
				if (Math.abs(event.getX() - startX + event.getY() - startY) > 10) {
					if (editing && startModule != null) {
						if (startLink != null) { // link
							linking = true;
							linkX = event.getX();
							linkY = event.getY();
							invalidate();
							// Now that the touch is a linking, unlink existing if necessary
							Module nearModule = getModuleNear(startX + startScrollX, startY + startScrollY);
							if (nearModule != null) {
								if (getOutputAt(nearModule, startX + startScrollX, startY + startScrollY) == null) {
									int input = getInputAt(nearModule, startX + startScrollX, startY + startScrollY);
									if (input == 1) {
										nearModule.input1 = null;
									} else if (input == 2) {
										nearModule.input2 = null;
									} else if (input == 3) {
										nearModule.input3 = null;
									} else if (input == 4) {
										nearModule.input4 = null;
									}
									int mod = getModAt(nearModule, startX + startScrollX, startY + startScrollY);
									if (mod == 1) {
										nearModule.mod1 = null;
									} else if (mod == 2) {
										nearModule.mod2 = null;
									}
								}
							}
						} else { // move
							moving = true;
							float originalXPosition = startModule.xPosition;
							float originalYPosition = startModule.yPosition;
							startModule.xPosition = (event.getX() - startX) / d + startModuleX;
							startModule.yPosition = (event.getY() - startY) / d + startModuleY;
							startModule.xPosition = (int) ((startModule.xPosition + GRID / 2) / GRID) * GRID;
							startModule.yPosition = (int) ((startModule.yPosition + GRID / 2) / GRID) * GRID;
							if (startModule.xPosition != originalXPosition || startModule.yPosition != originalYPosition) {
								instrument.dirty = true;
								RectF rectf = new RectF(originalXPosition, originalYPosition, originalXPosition + startModule.getWidth(), originalYPosition + startModule.getHeight());
								rectf = new RectF(startModule.xPosition, startModule.yPosition, startModule.xPosition + startModule.getWidth(), startModule.yPosition + startModule.getHeight());
								ModGraph.this.invalidate();
							}
						}
					} else { // scroll
						scrollTo((int) (Math.max(0, startScrollX + startX - event.getX())), (int) (Math.max(0, startScrollY + startY - event.getY())));
						scrolling = true;
					}
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (linking) {
				Module targetModule = getModuleNear(event.getX() + startScrollX, event.getY() + startScrollY);
				if (targetModule != null) { // link
					synchronized (instrument) {
						int input = getInputAt(targetModule, event.getX() + startScrollX, event.getY() + startScrollY);
						if (input == 1) {
							targetModule.link(1, startLink);
							instrument.dirty = true;
						} else if (input == 2) {
							targetModule.link(2, startLink);
							instrument.dirty = true;
						} else if (input == 3) {
							targetModule.link(3, startLink);
							instrument.dirty = true;
						} else if (input == 4) {
							targetModule.link(4, startLink);
							instrument.dirty = true;
						}
						int mod = getModAt(targetModule, event.getX() + startScrollX, event.getY() + startScrollY);
						if (mod == 1) {
							targetModule.link(-1, startLink);
							instrument.dirty = true;
						} else if (mod == 2) {
							targetModule.link(-2, startLink);
							instrument.dirty = true;
						}
					}
				}
				linking = false;
				invalidate();
				return true;
			}
			if (moving) {
				startModule.xPosition = (int) ((startModule.xPosition + GRID / 2) / GRID) * GRID;
				startModule.yPosition = (int) ((startModule.yPosition + GRID / 2) / GRID) * GRID;
				instrument.dirty = true;
				moving = false;
				invalidate();
				return true;
			}
			if (scrolling) {
				scrolling = false;
				return true;
			}
		}
		return false;
	}
	public void onClick(View v) {
		if (instrument != null) {
			Module module = getModuleAt(startX + startScrollX, startY + startScrollY);
			selectModule(module);
		}
	}

	public void selectModule(Module selectedModule) {
		instrument.selectedModule = selectedModule;
		for (Module module : instrument.modules) {
			if (module == selectedModule) {
				module.isSelected = true;
				RectF rectf = new RectF(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight());
				ModGraph.this.invalidate();
			} else if (module.isSelected) {
				module.isSelected = false;
				RectF rectf = new RectF(module.xPosition, module.yPosition, module.xPosition + module.getWidth(), module.yPosition + module.getHeight());
				ModGraph.this.invalidate();
			}
		}

		if (onSelectionListener != null) {
			onSelectionListener.selected(selectedModule);
		}
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument, MySynth synth) {
		this.instrument = instrument;
		this.synth = synth;
		postInvalidate();
	}

	public void setEditing(boolean edit) {
		if (instrument != null) {
			this.editing = edit;
			instrument.setEditing(edit);
		}
	}

	public boolean isEditing() {
		return editing;
	}

	public void deleteModule(Module module) {
		if (instrument != null) {
			instrument.deleteModule(module);
		}
		ModGraph.this.invalidate();
	}

	public void addModule(Module module) {
		if (instrument != null) {
			module.xPosition = getScrollX() + 350;
			module.yPosition = getScrollY() + 200;
			instrument.addModule(module);
			ModGraph.this.invalidate();
			selectModule(module);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();

		float t = 0.0f + this.getScrollY();
		float l = 0.0f + this.getScrollX();
		float r = this.getWidth() + this.getScrollX();
		float b = this.getHeight() + this.getScrollY();

		// clear background
		paint.setColor(0x80000000);
		canvas.drawRect(l, t, r, b, paint);

		canvas.scale(d, d);

		// draw modules (which also draws the links)
		if (instrument != null) {
			for (Module module : instrument.modules) {
				((ModuleViewer) module.getViewer(instrument)).draw(instrument, getContext().getResources(), canvas, editing, colorIcons);
			}
		}

		// draw a "in progress" link
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		if (linking) {
			paint.setColor(0xFFFFFFFF);
			paint.setStrokeWidth(2);
			float x = startLink.module.getOutputX(startLink.outputN - 1);
			float y = startLink.module.getOutputY(startLink.outputN - 1);
			canvas.drawLine(x, y, (linkX + startScrollX) / d, (linkY + startScrollY) / d, paint);
		}

		// draw a border
		canvas.scale(1.0f / d, 1.0f / d);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
		paint.setColor(0xC0404040);
		canvas.drawLine(l, t, l, b, paint);
		canvas.drawLine(l, t, r, t, paint);
		paint.setColor(0xC0C0C0C0);
		canvas.drawLine(r, b, l, b, paint);
		canvas.drawLine(r, b, r, t, paint);
	}

	public void setOnSelectionListener(OnSelectionListener listener) {
		this.onSelectionListener = listener;
	}

	@SuppressLint("NewApi")
	public void updateLevels() {
		if (colorIcons) {
			postInvalidate();
		} else if (instrument != null && instrument.getOutputModule() != null && instrument.getOutputModule().limiter < 0.9) {
			postInvalidate();
//			int l = (int)(instrument.getOutputModule().xPosition * d);
//			int t = (int)(instrument.getOutputModule().yPosition * d);
//			int w = (int)(100 * d);
//			int h = (int)(100 * d);
//			postInvalidate(l, t, w, h);
		}
	}

}
