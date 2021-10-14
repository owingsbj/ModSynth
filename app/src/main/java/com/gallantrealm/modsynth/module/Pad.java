package com.gallantrealm.modsynth.module;

import com.gallantrealm.android.Translator;

public class Pad extends Module {
	private static final long serialVersionUID = 1L;

	public enum PadType {
		Continuous, Chromatic, Major, Velocity;
		public String toString() {
			String string;
			if (this == Continuous) {
				string = "Continuous";
			} else if (this == Chromatic) {
				string = "Chromatic";
			} else if (this == Major) {
				string = "Major";
			} else {
				string = "Velocity";
			}
			return Translator.getTranslator().translate(string);
		}
	}

	public double xMin = 0.0;
	public double xMax = 1.0;
	public double yMin = 0.0;
	public double yMax = 1.0;
	public PadType xType = PadType.Continuous;
	public PadType yType = PadType.Continuous;

	public int voices = 1;

	@Override
	public int getInputCount() {
		return 0;
	}

	@Override
	public int getModCount() {
		return 0;
	}

	@Override
	public int getOutputCount() {
		return 3;
	}

	@Override
	public String getOutputName(int n) {
		if (n == 0) {
			return "X";
		} else if (n == 1) {
			return "Y";
		} else {
			return "Gate";
		}
	}

	private transient float[] x;
	private transient float[] y;
	public transient boolean[] pressed;
	public transient float[] newX;
	public transient float[] newY;
	public transient float[] smoothX;
	public transient float[] smoothY;
	public transient boolean[] lastPressed;
	public transient boolean dragging;
	private transient float[][] xd, yd;
	private transient float[] lastx, lasty;
	private transient float[] vx, vy;

	public int getRequiredVoices() {
		return voices;
	}

	public void setX(int voice, float value) {
		// in the case where mono pad is used with another multi-voice module, set all voices.
		if (voices <= 1) {
			for (int i = 0; i < x.length; i++) {
				x[i] = value;
			}
		} else {
			x[voice] = value;
		}
	}

	public void setY(int voice, float value) {
		// in the case where mono pad is used with another multi-voice module, set all voices.
		if (voices <= 1) {
			for (int i = 0; i < y.length; i++) {
				y[i] = value;
			}
		} else {
			y[voice] = value;
		}
	}

	private static final int DSTEPS = 25;

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		x = new float[voices];
		y = new float[voices];
		xd = new float[DSTEPS][voices];
		yd = new float[DSTEPS][voices];
		lastx = new float[voices];
		lasty = new float[voices];
		vx = new float[voices];
		vy = new float[voices];
		pressed = new boolean[voices];
		newX = new float[voices];
		newY = new float[voices];
		smoothX = new float[voices];
		smoothY = new float[voices];
		lastPressed = new boolean[voices];
	}

	transient int t;

	@Override
	public void doEnvelope(int voice) {
		float oldX = newX[voice];
		float oldY = newY[voice];
		if (xType == PadType.Velocity) {
			if (pressed[voice] && !lastPressed[voice]) {
				lastx[voice] = x[voice];
			}
			float dx = 100.0f * (x[voice] - lastx[voice]);
			vx[voice] = 0.99f * vx[voice] + 0.01f * dx;
			lastx[voice] = x[voice];
			if (dragging) {
				newX[voice] = (float) (Math.abs(vx[voice]) * (xMax - xMin) + xMin);
			} else {
				newX[voice] = (float) xMin;
			}
		} else {
			newX[voice] = (float) (x[voice] * (xMax - xMin) + xMin);
		}
		if (yType == PadType.Velocity) {
			if (pressed[voice] && !lastPressed[voice]) {
				lasty[voice] = y[voice];
			}
			float dy = 100.0f * (y[voice] - lasty[voice]);
			vy[voice] = 0.99f * vy[voice] + 0.01f * dy;
			lasty[voice] = y[voice];
			if (dragging) {
				newY[voice] = (float) (Math.abs(vy[voice]) * (yMax - yMin) + yMin);
			} else {
				newY[voice] = (float) yMin;
			}
		} else {
			newY[voice] = (float) (y[voice] * (yMax - yMin) + yMin);
		}
		output3.value[voice] = pressed[voice] ? 1.0f : 0.0f;
		if (xType == PadType.Chromatic) {
			newX[voice] = ((int) (newX[voice] * 100.0f)) / 100.0f;
			smoothX[voice] = newX[voice];
			if (newX[voice] != oldX) {
				output3.value[voice] = 0.0f;
			}
		} else if (xType == PadType.Major) {
			newX[voice] = majorScale((int) (newX[voice] * 100.0f)) / 100.0f;
			smoothX[voice] = newX[voice];
			if (newX[voice] != oldX) {
				output3.value[voice] = 0.0f;
			}
		}
		if (yType == PadType.Chromatic) {
			newY[voice] = ((int) (newY[voice] * 100.0f)) / 100.0f;
			smoothY[voice] = newY[voice];
			if (newY[voice] != oldY) {
				output3.value[voice] = 0.0f;
			}
		} else if (yType == PadType.Major) {
			newY[voice] = majorScale((int) (newY[voice] * 100.0f)) / 100.0f;
			smoothY[voice] = newY[voice];
			if (newY[voice] != oldY) {
				output3.value[voice] = 0.0f;
			}
		}
		if (dragging) {
			if (xType == PadType.Continuous) {
				smoothX[voice] = 0.9f * smoothX[voice] + 0.1f * newX[voice];
			} else {
				smoothX[voice] = newX[voice];
			}
			if (yType == PadType.Continuous) {
				smoothY[voice] = 0.9f * smoothY[voice] + 0.1f * newY[voice];
			} else {
				smoothY[voice] = newY[voice];
			}
		} else {
			smoothX[voice] = newX[voice];
			smoothY[voice] = newY[voice];
		}
		output1.value[voice] = smoothX[voice];
		output2.value[voice] = smoothY[voice];
		lastPressed[voice] = pressed[voice];
	}

	private int majorScale(int note) {
		int octave = note / 7;
		int noteOnScale = note % 7;
		int noteEqual = 0;
		switch (noteOnScale) {
		case 0:
			noteEqual = 0;
			break;
		case 1:
			noteEqual = 2;
			break;
		case 2:
			noteEqual = 4;
			break;
		case 3:
			noteEqual = 5;
			break;
		case 4:
			noteEqual = 7;
			break;
		case 5:
			noteEqual = 9;
			break;
		case 6:
			noteEqual = 11;
			break;
		}
		return octave * 12 + noteEqual;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
	}

	public boolean doesSynthesis() {
		return false;
	}

	@Override
	public boolean isSounding() {
		for (int i = 0; i < voices; i++) {
			if (pressed[i]) {
				return true;
			}
		}
		return false;
	}

}
