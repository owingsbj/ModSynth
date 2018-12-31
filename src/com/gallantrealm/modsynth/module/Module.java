package com.gallantrealm.modsynth.module;

import java.io.Serializable;
import com.gallantrealm.modsynth.Instrument;

public abstract class Module implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final float C0 = 16.352f;
	public static final int NOTES_PER_VOLT = 100;   // NOTE: don't change this unless you provide a new option!!

	static final float[] zeros = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	static final float[] ones = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	static final float[] big = {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000};
	static final float[] small = {-1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000, -1000};

	public int iconId;
	public float xPosition;
	public float yPosition;

	public transient int sampleRate = 22050;
	public transient boolean isSelected;
	public transient Object viewer;    // Object to avoid a dependency on the viewer (for VST).  Cast it when needed
	public transient boolean dirty;
	public transient int predecessorCount;
	public transient int stringifyId;
	
	public transient double min;
	public transient double max;
	public transient double lastmin;
	public transient double lastmax;

	public static final class Link implements Serializable {
		private static final long serialVersionUID = 1L;

		public Module module;
		public int outputN;

		public transient float[] value;

		public Link(Module module, int outputN) {
			this.module = module;
			this.outputN = outputN;
		}

		public String toString() {
			return "" + module.stringifyId + ":" + outputN;
		}
	}

	public Link input1;
	public Link input2;
	public Link input3;
	public Link input4; // not used anymore
	public boolean modLinks = true;
	public Link mod1;
	public Link mod2;
	public Link output1 = new Link(this, 1);
	public Link output2 = new Link(this, 2);
	public Link output3 = new Link(this, 3);

	public Module() {
	}

	public final boolean isOnModule(float x, float y) {
		if (x >= xPosition - 10 && x <= xPosition + 10 + getWidth()) {
			if (y >= yPosition - 10 && y <= yPosition + 10 + getHeight()) {
				return true;
			}
		}
		return false;
	}

	public float getHeight() {
		return 100; // + Math.max(50 * Math.max(0, getOutputCount() - 2), 50 * Math.max(0, getInputCount() - 3));
	}

	public float getWidth() {
		return 100;
	}
	
	public String getInputName(int n) {
		return "";
	}
	
	public String getOutputName(int n) {
		return "";
	}
	
	public String getModName(int n) {
		return "";
	}

	public final float getInputX(int n) {
		return xPosition;
	}

	public final float getInputY(int n) {
		return yPosition + getHeight() / getInputCount() * (n + 0.5f);
	}

	public final float getModX(int n) {
		if (n == 0) { // the control input bottom
			return xPosition + getWidth() / 2;
		} else { // the control input top
			return xPosition + getWidth() / 2;
		}
	}

	public final float getModY(int n) {
		if (n == 0) { // the control input bottom
			return yPosition + getHeight();
		} else { // the control input top
			return yPosition;
		}
	}

	public final float getOutputX(int n) {
		return xPosition + getWidth();
	}

	public final float getOutputY(int n) {
		return yPosition + getHeight() / getOutputCount() * (n + 0.5f);
	}

	public Object getViewer(Instrument instrument) {
		if (viewer == null) {
			try {
				Class viewerClass = getClass().getClassLoader().loadClass("com.gallantrealm.modsynth.viewer." + getClass().getSimpleName() + "Viewer");
				viewer = viewerClass.getConstructor(Module.class, Instrument.class).newInstance(this, instrument);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return viewer;
	}

	public abstract int getInputCount();

	public abstract int getModCount();

	public abstract int getOutputCount();

	/**
	 * Links an input of this module to an output of another module.
	 * 
	 * @param inputN
	 *            the input, starting at 1, or mod input, starting at -1
	 * @param desiredOutput
	 *            the link object at the output of another module.
	 */
	public void link(int inputN, Link desiredOutput) {
		if (inputN == 1) {
			input1 = desiredOutput;
		} else if (inputN == 2) {
			input2 = desiredOutput;
		} else if (inputN == 3) {
			input3 = desiredOutput;
		} else if (inputN == 4) {
			input4 = desiredOutput;
		} else if (inputN == -1) {
			mod1 = desiredOutput;
		} else if (inputN == -2) {
			mod2 = desiredOutput;
		}
	}

	public String toString() {
		return this.getClass().getSimpleName();
	}

	public int getRequiredVoices() {
		return 1;
	}

	public void initialize(int voices, int sampleRate) {
		this.sampleRate = sampleRate;
		if (!modLinks) {
			mod1 = input2;
			mod2 = input4;
			input2 = input3;
			input3 = null;
			input4 = null;
			modLinks = true;
		}
		if (output3 == null) {
			output3 = new Link(this, 3);
		}
		for (int i = 0; i < getOutputCount(); i++) {
			Link link = getOutput(i);
			if (link != null) {
				link.value = new float[voices];
			}
		}
	}
	
	/**
	 * Subclasses can implement this to cleanup anything if necessary
	 */
	public void  terminate() {
	}
	
	public Link getInput(int i) {
		if (i == 0) {
			return input1;
		} else if (i == 1) {
			return input2;
		} else if (i == 2) {
			return input3;
		}
		return null;
	}

	public Link getOutput(int i) {
		if (i == 0) {
			return output1;
		} else if (i == 1) {
			return output2;
		} else if (i == 2) {
			return output3;
		}
		return null;
	}

	public boolean requiresUpgrade() {
		return false;
	}

	public abstract void doEnvelope(int voice);

	public abstract void doSynthesis(int startVoice, int endVoice);
	
	/**
	 * Allows optimization to avoid calling doSynthesis.
	 */
	public boolean doesSynthesis() {
		return true;
	}

	public boolean doesEnvelope() {
		return true;
	}

	// pitch is represented as the piano key number (C0 being zero)
	public final float pitchToFrequency(float pitch) {
		float note = pitch * NOTES_PER_VOLT;
		return (float)Math.pow(2, note / 12) * C0;
	}

	public final float frequencyToPitch(float frequency) {
		float note = 12 * (float)Math.log(frequency / C0) / (float)Math.log(2);
		return note / NOTES_PER_VOLT;
	}
	
	// cutoff (filters) is the piano key number divided by 96
	public final float cutoffToFrequency(float cutoff) {
		double note = cutoff * NOTES_PER_VOLT;
		return (float)Math.pow(2, note / 12) * C0;
	}

	/**
	 * Override in modules to respond to changes in MIDI controls.
	 * 
	 * @param cc
	 * @param value
	 */
	public void updateCC(int cc, double value) {
	}
	
	/**
	 * Return true if the module is generating sound such that the sound engine shouldn't quiese
	 */
	public boolean isSounding() {
		return false;
	}

	/**
	 * Override in modules to handle midi clock notifications
	 */
	public void midiClock() {
	}
	
}
