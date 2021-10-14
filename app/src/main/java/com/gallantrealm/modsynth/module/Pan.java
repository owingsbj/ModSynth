package com.gallantrealm.modsynth.module;

public class Pan extends Module {
	private static final long serialVersionUID = 1L;

	public double balance = 0.5;
	public double modulation = 0.5;
	public CC balanceCC;
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
		return 2;
	}

	@Override
	public String getInputName(int n) {
		return "In";
	}
	
	@Override
	public String getModName(int n) {
		return "Balance";
	}
	
	@Override
	public String getOutputName(int n) {
		if (n == 0) {
			return "Out L";
		} else {
			return "Out R";
		}
	}

	private transient float[] modulatedBalance;

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		modulatedBalance = new float[voices];
		if (balanceCC == null) {
			balanceCC = new CC();
		}
		if (modulationCC == null) {
			modulationCC = new CC();
		}
	}

	private transient float fbalance, fmodulation;
	
	@Override
	public void doEnvelope(int voice) {
		fbalance = (float)balance;
		fmodulation = (float)modulation;
		modulatedBalance[voice] = 2.0f * (fbalance - 0.5f) + 0.5f;
		if (mod1 != null) {
			modulatedBalance[voice] += (2.0f * fmodulation) * mod1.value[voice];
		}
		modulatedBalance[voice] = Math.max(0.0f, Math.min(1.0f, modulatedBalance[voice]));
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		for (int voice = startVoice; voice <= endVoice; voice++) {
			float value1 = 0.0f;
			float value2 = 0.0f;
			if (input1 != null) {
				float value = input1.value[voice];
				value1 = value * (1.0f - modulatedBalance[voice]);
				value2 = value * modulatedBalance[voice];
			}
			output1.value[voice] = value1;
			output2.value[voice] = value2;
		}
	}

	public void updateCC(int cc, double value) {
		if (balanceCC.cc == cc) {
			balance = balanceCC.range(value);
		}
		if (modulationCC.cc == cc) {
			modulation = modulationCC.range(value);
		}
	}

}
