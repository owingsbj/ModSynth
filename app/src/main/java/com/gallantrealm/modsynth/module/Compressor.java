package com.gallantrealm.modsynth.module;

public class Compressor extends Module {
	private static final long serialVersionUID = 1L;

	public double amount = 0.0;
	public double delay = 0.0;
	public double gain = 1.0;
	public CC amountCC;
	public CC delayCC;
	public CC gainCC;

	private transient int voices;

	@Override
	public int getInputCount() {
		return 1;
	}

	@Override
	public int getModCount() {
		return 0;
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
	public String getOutputName(int n) {
		return "Out";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		this.voices = voices;
		if (amountCC == null) {
			amountCC = new CC();
			delayCC = new CC();
			gainCC = new CC();
		}
	}

	transient float fdelay, famount, fmax, fgain;

	@Override
	public void doEnvelope(int voice) {
		fdelay = (float)Math.pow(delay, 0.001);
		famount = (float)amount;
		fgain = (float)gain;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		float value = 0.0f;
		for (int voice = startVoice; voice <= endVoice; voice++) {
			float input;
			if (input1 != null) {
				input = input1.value[voice];
			} else {
				input = 0;
			}
			value += input;
		}
		float m = Math.abs(value);
		fmax = Math.max(m, fmax * fdelay);
		value = value / (1.0f + famount * fmax);
		value = value * fgain;
		output1.value[0] = value;
	}

	@Override
	public void updateCC(int cc, double value) {
		if (amountCC.cc == cc) {
			amount = amountCC.range(value) * 10;
		}
		if (delayCC.cc== cc) {
			delay = delayCC.range(value);
		}
		if (gainCC.cc== cc) {
			gain = 10.0 * gainCC.range(value);
		}
	}

}
