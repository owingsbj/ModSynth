package com.gallantrealm.modsynth.module;

public class Divider extends Module {
	private static final long serialVersionUID = 1L;
	
	public int divisor = 2;
	public int phase = 0;
	public int duty = 1;
	public boolean positive;

	private transient double lastGate[];
	private transient int counts[];

	@Override
	public int getInputCount() {
		return 1;
	}

	@Override
	public int getModCount() {
		return 1;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}

	@Override
	public String getInputName(int n) {
		return "Gate";
	}

	@Override
	public String getModName(int n) {
		return "Reset";
	}

	@Override
	public String getOutputName(int n) {
		return "Gate";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		lastGate = new double[voices];
		counts = new int[voices];
	}

	@Override
	public boolean doesSynthesis() {
		return false;
	}

	@Override
	public void doEnvelope(int voice) {
		if (input1 == null) {
			return;
		}
		if (lastGate[voice] <= 0 && input1.value[voice] > 0) { // triggered
			counts[voice] = counts[voice] + 1;
		}
		lastGate[voice] = input1.value[voice];
		if (positive) {
			output1.value[voice] = (counts[voice] + phase) % divisor < (divisor - duty) ? 0.0f : 1.0f;
		} else {
			output1.value[voice] = (counts[voice] + phase) % divisor < (divisor - duty) ? -1.0f : 1.0f;
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
	}

}
