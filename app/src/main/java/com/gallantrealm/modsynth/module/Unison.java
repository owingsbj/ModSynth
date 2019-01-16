package com.gallantrealm.modsynth.module;

public class Unison extends Module {
	private static final long serialVersionUID = 1L;

	public int voices = 3;
	public int polyphony = 3;
	public double chorusWidth = 0.1;
	public double chorusSpread = 0.0;
	public CC chorusWidthCC;
	public CC chorusSpreadCC;

	@Override
	public int getRequiredVoices() {
		return voices * polyphony;
	}

	@Override
	public int getInputCount() {
		return 2;
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
	public String getInputName(int n) {
		if (n == 0) {
			return "Pitch";
		} else {
			return "Gate";
		}
	}

	@Override
	public String getOutputName(int n) {
		if (n == 0) {
			return "Pitch";
		} else if (n == 1) {
			return "Gate";
		} else {
			return "Balance";
		}
	}

	private transient int maxVoices;

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		maxVoices = voices;
		if (chorusWidthCC == null) {
			chorusWidthCC = new CC();
		}
		if (chorusSpreadCC == null) {
			chorusSpreadCC = new CC();
		}
	}

	private transient float fchorusWidth, fchorusSpread;

	@Override
	public void doEnvelope(int voice) {
		fchorusWidth = (float) chorusWidth;
		fchorusSpread = (float) chorusSpread;
		if (voice >= polyphony) {
			return;
		}
		int baseVoice = voice * voices;
		if (input1 != null) {
			float v = voices / 2.0f;
			for (int i = 0; i < voices; i++) {
				if (baseVoice + i < output1.value.length) {
					output1.value[baseVoice + i] = input1.value[voice] - fchorusWidth * (i - v) / 100.0f;
					output3.value[baseVoice + i] = fchorusSpread * (i - voices / 2.0f) / voices;
				}
			}
		}
		if (input2 != null) {
			for (int i = 0; i < voices; i++) {
				if (baseVoice + i < output2.value.length) {
					output2.value[baseVoice + i] = input2.value[voice];
				}
			}
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
	}

	public boolean doesSynthesis() {
		return false;
	}

	public void updateCC(int cc, double value) {
		if (chorusWidthCC.cc == cc) {
			chorusWidth = chorusWidthCC.range(value);
		}
		if (chorusSpreadCC.cc == cc) {
			chorusSpread = chorusSpreadCC.range(value);
		}
	}

}
