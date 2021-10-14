package com.gallantrealm.modsynth.module;

public class Reverb extends Module {
	private static final long serialVersionUID = 1L;

	private static int NDELAYS = 128;

	public double amount = 0.5;
	public double depth = 0.5;
	public double tone = 0.9;
	// public double echo = 0.5;
	public CC amountCC;
	public CC depthCC;
	public CC toneCC;
	// public CC echoCC;

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
		return 2;
	}

	@Override
	public String getInputName(int n) {
		if (n == 0) {
			return "In L";
		} else {
			return "In R";
		}
	}

	@Override
	public String getOutputName(int n) {
		if (n == 0) {
			return "Out L";
		} else {
			return "Out R";
		}
	}

	private transient int delayLength;
	private transient float[] delayTimeCoefficients;
	private transient float[] delayVolumeCoefficients;
	private transient int[] fp_delayTimes;
	private transient int currentIndex;
	private transient float[] delayTableL;
	private transient float[] delayTableR;
	private transient float lastLeftValue;
	private transient float lastRightValue;
	private transient float tone2;
	private transient float famount;
	private transient double lastDepth;

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		delayLength = (int) (2.5f * sampleRate);
		delayTimeCoefficients = new float[NDELAYS];
		delayVolumeCoefficients = new float[NDELAYS];
		for (int i = 0; i < NDELAYS; i++) {
			delayTimeCoefficients[i] = (float)(0.05 + 0.95 * Math.pow(Math.random(), 2));
			delayVolumeCoefficients[i] = (1.0f - (float) (Math.pow(delayTimeCoefficients[i], 0.05f)));
		}
		fp_delayTimes = new int[NDELAYS];
		delayTableL = new float[delayLength];
		delayTableR = new float[delayLength];
		currentIndex = 0;
		lastLeftValue = 0.0f;
		lastRightValue = 0.0f;
		if (amountCC == null) {
			amountCC = new CC();
			depthCC = new CC();
			toneCC = new CC();
		}
	}

	@Override
	public void doEnvelope(int voice) {
		if (depth != lastDepth) {
			for (int i = 0; i < NDELAYS; i++) {
				fp_delayTimes[i] = (int) (delayTimeCoefficients[i] * depth * delayLength);
				fp_delayTimes[i] = Math.min(delayLength, fp_delayTimes[i]);
			}
			lastDepth = depth;
		}
		tone2 = Math.min((float) Math.sqrt(tone), 0.99f);
		famount = (float) amount;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		float sample1 = 0.0f;
		float sample2 = 0.0f;
		for (int voice = startVoice; voice <= endVoice; voice++) {
			if (input1 != null) {
				sample1 += input1.value[voice];
			}
			if (input2 != null) {
				sample2 += input2.value[voice];
			} else {
				sample2 = sample1;
			}
		}
		int ci = (currentIndex - 1);
		if (ci < 0) {
			ci = delayLength -1;
		}
		float reverbLeft = 0.0f;
		float reverbRight = 0.0f;
		for (int i = 0; i < NDELAYS; i++) {
			int dil1 = (ci + fp_delayTimes[i]) % delayLength;
			float rl1 = delayVolumeCoefficients[i] * delayTableL[dil1];
			reverbLeft += rl1;
			i += 1;
			int dir1 = (ci + fp_delayTimes[i]) % delayLength;
			;
			float rr1 = delayVolumeCoefficients[i] * delayTableR[dir1];
			reverbRight += rr1;
			i += 1;
			int dil2 = (ci + fp_delayTimes[i]) % delayLength;
			;
			float rl2 = delayVolumeCoefficients[i] * delayTableL[dil2];
			reverbLeft -= rl2;
			i += 1;
			int dir2 = (ci + fp_delayTimes[i]) % delayLength;
			;
			float rr2 = delayVolumeCoefficients[i] * delayTableR[dir2];
			reverbRight -= rr2;
		}
		reverbLeft = lastLeftValue * tone2 + reverbLeft * (1.0f - tone2);
		reverbRight = lastRightValue * tone2 + reverbRight * (1.0f - tone2);
		lastLeftValue = reverbLeft;
		lastRightValue = reverbRight;
		float dry1 = (1.0f - famount) * sample1;
		float dry2 = (1.0f - famount) * sample2;
		float wet1 = famount * reverbLeft;
		float wet2 = famount * reverbRight;
		output1.value[0] = dry1 + wet1;
		output2.value[0] = dry2 + wet2;
		delayTableL[ci] = sample1 + wet2 * 0.5f;
		delayTableR[ci] = sample2 + wet1 * 0.5f;
		currentIndex = ci;
	}

	@Override
	public void updateCC(int cc, double value) {
		if (amountCC.cc == cc) {
			amount = amountCC.range(value);
		}
		if (depthCC.cc == cc) {
			double v = depthCC.range(value);
			depth = v * v;
		}
		if (toneCC.cc == cc) {
			tone = 1.0 - toneCC.range(value);
		}
	}

}
