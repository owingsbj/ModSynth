package com.gallantrealm.modsynth.module;

import com.gallantrealm.modsynth.Stringifier;

public class Delay extends Module {
	private static final long serialVersionUID = 1L;

	public double delayTime = 0.1;
	public double flangeAmount = 1.0;
	public boolean respectdelayLevel = true;
	public double delayLevel = 0.5;
	public double feedback = 0.0;
	public CC delayTimeCC;
	public CC flangeAmountCC;
	public CC delayLevelCC;
	public CC feedbackCC;

	@Override
	public void stringify(Stringifier s) {
		super.stringify(s);
		if (!respectdelayLevel) {
			delayLevel = 1.0;
			respectdelayLevel = true;
		}
		s.add("delayTime", delayTime);
		s.add("flangeAmount", flangeAmount);
		s.add("delayLevel", delayLevel);
		s.add("feedback", feedback);
	}

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

	private transient int[] fp_delayTime;
	private transient int[] echoIndex;
	private transient float[][] echoTable;

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		fp_delayTime = new int[voices];
		if (!respectdelayLevel) {
			delayLevel = 1.0;
			respectdelayLevel = true;
		}
		echoTable = new float[voices][sampleRate];
		echoIndex = new int[voices];
		if (delayTimeCC == null) {
			delayTimeCC = new CC();
		}
		if (flangeAmountCC == null) {
			flangeAmountCC = new CC();
		}
		if (delayLevelCC == null) {
			delayLevelCC = new CC();
		}
		if (feedbackCC == null) {
			feedbackCC = new CC();
		}
	}

	private float fdelayLevel;
	
	@Override
	public void doEnvelope(int voice) {
		fdelayLevel = (float)delayLevel;
		if (mod1 != null) {
			fp_delayTime[voice] = (int) ((delayTime + flangeAmount * (mod1.value[voice]) / 100.0) * sampleRate);
		} else {
			fp_delayTime[voice] = (int) (delayTime * sampleRate);
		}
		fp_delayTime[voice] = Math.max(0, Math.min(sampleRate, fp_delayTime[voice]));
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		if (input1 == null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				output1.value[voice] = 0;
			}
		} else {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				if (mod1 != null) {
					int delay = (int) ((delayTime + flangeAmount * (mod1.value[voice]) / 100.0) * sampleRate);
					fp_delayTime[voice] = (delay < 0) ? 0 : ((delay > sampleRate) ? sampleRate : delay);
				}
				float sample = input1.value[voice];
				int ei = (echoIndex[voice] + 1);
				if (ei == sampleRate) {
					ei = 0;
				}
				echoTable[voice][ei] = sample;
				int delayIndex = ei - fp_delayTime[voice];
				if (delayIndex < 0) {
					delayIndex += sampleRate;
				}
				float delayedSampleL = echoTable[voice][delayIndex];
				output1.value[voice] = fdelayLevel * delayedSampleL + (1.0f - fdelayLevel) * sample;
				echoTable[voice][ei] += feedback * delayedSampleL;
				echoIndex[voice] = ei;
			}
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (delayLevelCC.cc == cc) {
			delayLevel = delayLevelCC.range(value);
		}
		if (delayTimeCC.cc == cc) {
			double v = delayTimeCC.range(value);
			delayTime = v * v;
		}
		if (feedbackCC.cc == cc) {
			feedback = feedbackCC.range(value);
		}
		if (flangeAmountCC.cc == cc) {
			flangeAmount = flangeAmountCC.range(value);
		}
	}

}
