package com.gallantrealm.modsynth.module;

public class Glide extends Module {
	private static final long serialVersionUID = 1L;

	public double glideUp = 0.0;
	public double glideDown = 0.0;
	public boolean audioSpeed = false;
	public CC glideUpCC;
	public CC glideDownCC;

	private transient float lastLevel[];
	private transient float lastGate[];

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
		return "Gate";
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		lastLevel = new float[voices];
		lastGate = new float[voices];
		if (glideUpCC == null) {
			glideUpCC = new CC();
		}
		if (glideDownCC == null) {
			glideDownCC = new CC();
		}
	}

	private transient float fglideUpGraded;
	private transient float fglideDownGraded;

	@Override
	public void doEnvelope(int voice) {
		if (voice == 0) {
			if (audioSpeed) {
				fglideUpGraded = (float)((1.0 - glideUp) / glideUp / sampleRate * 1000.0);
				fglideDownGraded = (float)((1.0 - glideDown) / glideDown / sampleRate * 1000.0);
			} else {
				fglideUpGraded = (float)((1.0 - glideUp) / glideUp / sampleRate / 1.0);
				fglideDownGraded = (float)((1.0 - glideDown) / glideDown / sampleRate / 1.0);
			}
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		for (int voice = startVoice; voice <= endVoice; voice++) {
			float level = 0;
			if (input1 != null) {
				level = input1.value[voice];
			}
			float nextLevel;
			if (lastLevel[voice] < level) {
				nextLevel = lastLevel[voice] + fglideUpGraded;
				if (nextLevel > level) {
					nextLevel = level;
				}
			} else if (lastLevel[voice] > level) {
				nextLevel = lastLevel[voice] - fglideDownGraded;
				if (nextLevel < level) {
					nextLevel = level;
				}
			} else {
				nextLevel = level;
			}
			if (mod1 != null) {
				double gate = mod1.value[voice];
				if (gate > 0 && lastGate[voice] <= 0) {
					nextLevel = level;
				}
				lastGate[voice] = (float)gate;
			}
			output1.value[voice] = nextLevel;
			lastLevel[voice] = nextLevel;
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (glideUpCC.cc == cc) {
			double v = glideUpCC.range(value);
			glideUp = v * v;
		}
		if (glideDownCC.cc == cc) {
			double v = glideDownCC.range(value);
			glideDown = v * v;
		}
	}
}
