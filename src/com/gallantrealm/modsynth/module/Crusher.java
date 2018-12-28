package com.gallantrealm.modsynth.module;

public class Crusher extends Module {
	private static final long serialVersionUID = 1L;

	public double level = 0.0;
	public double modulation = 0.5;
	public double rate = 0.0;
	public boolean modulateRate;
	public CC levelCC;
	public CC rateCC;
	public CC modulationCC;

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
		return "Modulation";
	}
	
	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	private transient float[] rateCount;

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		rateCount = new float[voices];
		if (levelCC == null) {
			levelCC = new CC();
		}
		if (rateCC == null) {
			rateCC = new CC();
		}
		if (modulationCC == null) {
			modulationCC = new CC();
		}
	}

	@Override
	public boolean doesEnvelope() {
		return false;
	}
	
	private transient float frate, flevel, fmodulation;

	@Override
	public void doEnvelope(int voice) {
		frate = (float)rate;
		flevel = (float)level;
		fmodulation = (float)modulation;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		for (int voice = startVoice; voice <= endVoice; voice++) {
			if (mod1 != null && modulateRate) {
				rateCount[voice] += (1.0 - rate - mod1.value[voice] * modulation);
			} else {
				rateCount[voice] += (1.0 - rate);
			}
			if (rateCount[voice] < 1.0) {
				return;
			}
			rateCount[voice] -= 1.0;
			float input;
			if (input1 != null) {
				input = input1.value[voice];
			} else {
				input = 0;
			}
			float modulationAmount = 0.0f;
			if (mod1 != null && !modulateRate) {
				modulationAmount = mod1.value[voice] * fmodulation;
			}
			float crush = 10.0f * (1.0f - flevel - modulationAmount);
			float value = 0.0f;
			if (crush != 0.0) {
				value = ((int) (input * crush + 0.999)) / crush;
			}
			output1.value[voice] = value;
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (levelCC.cc == cc) {
			level = levelCC.range(value);
		}
		if (rateCC.cc == cc) {
			rate = rateCC.range(value);
		}
		if (modulationCC.cc == cc) {
			modulation = modulationCC.range(value);
		}
	}

}
