package com.gallantrealm.modsynth.module;

public class SampleHold extends Module {
	private static final long serialVersionUID = 1L;

	private transient double lastGate[];

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
		return "In";
	}

	@Override
	public String getModName(int n) {
		return "Latch";
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		lastGate = new double[voices];
	}

	@Override
	public boolean doesEnvelope() {
		return false;
	}

	@Override
	public void doEnvelope(int voice) {
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		for (int voice = startVoice; voice <= endVoice; voice++) {
			float value = 0;
			if (input1 != null) {
				value = input1.value[voice];
			}
			float gate = 0;
			if (mod1 != null) {
				gate = mod1.value[voice];
			}
			if (lastGate[voice] <= 0 && gate > 0) {
				output1.value[voice] = value;
			}
			lastGate[voice] = gate;
		}
	}

}
