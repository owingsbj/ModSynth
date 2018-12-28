package com.gallantrealm.modsynth.module;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.PentaBandpassFilter;
import com.gallantrealm.modsynth.Stringifier;

public class SpectralFilter extends Module {
	private static final long serialVersionUID = 1L;

	public static final int BANDS = 25;
	public static final int BAND_GROUPS = 5;
	public static final int STEPS = 25;
	public static final int SUBSTEPS = 10; // how many times to adjust levels per step

	public boolean[][] spectralMap2 = new boolean[STEPS][BANDS];

	public double resonance = 0.25f;
	public double spread = 1.0f;
	public double modulation = 1.0f;
	public CC resonanceCC;
	public CC spreadCC;
	public CC modulationCC;

	private transient PentaBandpassFilter[][] filter;
	private transient double[][] smoothLevels;
	private transient int[] step, lastStep, subStep;

	@Override
	public void stringify(Stringifier s) {
		super.stringify(s);
		// TODO need to stringify
	}

	@Override
	public int getInputCount() {
		return 2;
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
		if (n == 0) {
			return "In";
		} else {
			return "Pitch";
		}
	}

	@Override
	public String getModName(int n) {
		return "Cutoff";
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	public void setupFilters(int voices) {
		if (filter == null) {
			filter = new PentaBandpassFilter[Instrument.MAX_VOICES][BAND_GROUPS];
			for (int v = 0; v < Instrument.MAX_VOICES; v++) {
				for (int b = 0; b < BAND_GROUPS; b++) {
					filter[v][b] = new PentaBandpassFilter(100, sampleRate, resonance * 10 * BANDS);
				}
			}
		} else {
			for (int v = 0; v < voices; v++) {
				for (int b = 0; b < BAND_GROUPS; b++) {
					filter[v][b].configure(sampleRate, resonance * 10 * BANDS);
				}
			}
		}
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		if (resonanceCC == null) {
			resonanceCC = new CC();
		}
		if (spreadCC == null) {
			spreadCC = new CC();
		}
		if (modulationCC == null) {
			modulationCC = new CC();
		}
		if (spectralMap2 == null) {
			spectralMap2 = new boolean[STEPS][BANDS];
		}
		smoothLevels = new double[voices][BANDS];
		step = new int[voices];
		lastStep = new int[voices];
		subStep = new int[voices];
		setupFilters(voices);
	}
	
	private transient float fspread;

	@Override
	public void doEnvelope(int voice) {
		fspread = (float)spread;
		float f1, f2, f3, f4, f5;
		for (int b = 0; b < BAND_GROUPS; b++) {
			if (input2 != null) {
				f1 = Math.max(1, Math.min(10000, pitchToFrequency(input2.value[voice] + ((5 * b) * fspread / BANDS))));
				f2 = Math.max(1, Math.min(10000, pitchToFrequency(input2.value[voice] + ((5 * b + 1) * fspread / BANDS))));
				f3 = Math.max(1, Math.min(10000, pitchToFrequency(input2.value[voice] + ((5 * b + 2) * fspread / BANDS))));
				f4 = Math.max(1, Math.min(10000, pitchToFrequency(input2.value[voice] + ((5 * b + 3) * fspread / BANDS))));
				f5 = Math.max(1, Math.min(10000, pitchToFrequency(input2.value[voice] + ((5 * b + 4) * fspread / BANDS))));
			} else {
				f1 = Math.max(1, Math.min(10000, pitchToFrequency((5 * b) * fspread / BANDS)));
				f2 = Math.max(1, Math.min(10000, pitchToFrequency((5 * b + 1) * fspread / BANDS)));
				f3 = Math.max(1, Math.min(10000, pitchToFrequency((5 * b + 2) * fspread / BANDS)));
				f4 = Math.max(1, Math.min(10000, pitchToFrequency((5 * b + 3) * fspread / BANDS)));
				f5 = Math.max(1, Math.min(10000, pitchToFrequency((5 * b + 4) * fspread / BANDS)));
			}
			filter[voice][b].setFrequencies(f1, f2, f3, f4, f5);
		}
		step[voice] = 0;
		if (mod1 != null) {
			step[voice] = (int) (modulation * mod1.value[voice] * STEPS);
			subStep[voice] = (int) (modulation * mod1.value[voice] * STEPS * SUBSTEPS - step[voice] * SUBSTEPS);
			step[voice] = step[voice] < 0 ? 0 : (step[voice] >= STEPS ? STEPS - 1 : step[voice]);
			if (step[voice] != lastStep[voice] && viewer != null) {
				try {
					viewer.getClass().getMethod("setCurrentStep", Integer.class).invoke(viewer, step[voice]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			lastStep[voice] = step[voice];
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		if (input1 == null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				output1.value[voice] = 0;
			}
		} else {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				int voiceStep = step[voice];
				int nextVoiceStep = voiceStep + 1 < STEPS - 1 ? voiceStep + 1 : voiceStep;
				double subStepWeight = subStep[voice] / (double) SUBSTEPS;
				float value = 0;
				for (int b = 0; b < BAND_GROUPS; b++) {
					int c = 5 * b;
					double vol1 = (spectralMap2[voiceStep][c] ? 0.01 : 0.00) * (1 - subStepWeight);
					double vol2 = (spectralMap2[nextVoiceStep][c] ? 0.01 : 0.00) * subStepWeight;
					double l1 = smoothLevels[voice][c] = 0.99 * smoothLevels[voice][c] + vol1 + vol2;
					c += 1;
					vol1 = (spectralMap2[voiceStep][c] ? 0.01 : 0.00) * (1 - subStepWeight);
					vol2 = (spectralMap2[nextVoiceStep][c] ? 0.01 : 0.00) * subStepWeight;
					double l2 = smoothLevels[voice][c] = 0.99 * smoothLevels[voice][c] + vol1 + vol2;
					c += 1;
					vol1 = (spectralMap2[voiceStep][c] ? 0.01 : 0.00) * (1 - subStepWeight);
					vol2 = (spectralMap2[nextVoiceStep][c] ? 0.01 : 0.00) * subStepWeight;
					double l3 = smoothLevels[voice][c] = 0.99 * smoothLevels[voice][c] + vol1 + vol2;
					c += 1;
					vol1 = (spectralMap2[voiceStep][c] ? 0.01 : 0.00) * (1 - subStepWeight);
					vol2 = (spectralMap2[nextVoiceStep][c] ? 0.01 : 0.00) * subStepWeight;
					double l4 = smoothLevels[voice][c] = 0.99 * smoothLevels[voice][c] + vol1 + vol2;
					c += 1;
					vol1 = (spectralMap2[voiceStep][c] ? 0.01 : 0.00) * (1 - subStepWeight);
					vol2 = (spectralMap2[nextVoiceStep][c] ? 0.01 : 0.00) * subStepWeight;
					double l5 = smoothLevels[voice][c] = 0.99 * smoothLevels[voice][c] + vol1 + vol2;
					value += filter[voice][b].filter(input1.value[voice], l1, l2, l3, l4, l5);
				}
				output1.value[voice] = value;
			}
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (resonanceCC != null && resonanceCC.cc == cc) {
			double v = resonanceCC.range(value);
			resonance = v * v;
			setupFilters(Instrument.MAX_VOICES);
		}
		if (spreadCC != null && spreadCC.cc == cc) {
			double v = spreadCC.range(value);
			spread = v;
		}
		if (modulationCC != null && modulationCC.cc == cc) {
			double v = modulationCC.range(value);
			modulation = v * v;
		}
	}

}
