package com.gallantrealm.modsynth.module;

import com.gallantrealm.modsynth.BiQuadraticFilter;

public class Filter extends Module {
	private static final long serialVersionUID = 1L;

	public BiQuadraticFilter.Type filterType = BiQuadraticFilter.Type.LOWPASS;
	public double resonance = 0.5f;
	public double cutoff = 1.0f;
	public double sweep = 1.0f;
	// private double smoothCutoff[]; // unused, should have been transient
	public CC filterTypeCC;
	public CC resonanceCC;
	public CC cutoffCC;
	public CC sweepCC;

	private transient BiQuadraticFilter[] filter;

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
		return "Cutoff";
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	public void setupFilters() {
		if (filter != null) {
			for (int v = 0; v < filter.length; v++) {
				filter[v] = new BiQuadraticFilter(filterType, 100, sampleRate, resonance);
			}
		}
	}

	transient double[] lastCutoff;

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		lastCutoff = new double[voices];
		filter = new BiQuadraticFilter[voices];
		setupFilters();
		if (resonanceCC == null) {
			resonanceCC = new CC();
		}
		if (cutoffCC == null) {
			cutoffCC = new CC();
		}
		if (sweepCC == null) {
			sweepCC = new CC();
		}
		if (filterTypeCC == null) {
			filterTypeCC = new CC();
			filterTypeCC.setRangeLimits(0, BiQuadraticFilter.Type.values().length - 1);
		}
	}

	transient double smoothResonance, smoothCutofff;

	@Override
	public void doEnvelope(int voice) {
		if (resonance > 0) {
			if (voice == 0) {
				if (smoothResonance - resonance > 0.25 || smoothResonance - resonance < -0.25) {
					smoothResonance = resonance;
				} else {
					smoothResonance = 0.9 * smoothResonance + 0.1 * resonance;
				}
				if (smoothCutofff - cutoff > 0.25 || smoothCutofff - cutoff < -0.25) {
					smoothCutofff = cutoff;
				} else {
					smoothCutofff = 0.9 * smoothCutofff + 0.1 * cutoff;
				}
			}
			if (smoothResonance != resonance) {
				filter[voice].setQ(smoothResonance);
			}
			float fcutoff = (float) (smoothCutofff + ((mod1 != null) ? sweep * mod1.value[voice] : 0));
			if (smoothResonance != resonance || lastCutoff[voice] != fcutoff) {
				lastCutoff[voice] = fcutoff;
				float frequency = Math.max(1, Math.min(10000, cutoffToFrequency(fcutoff)));
				frequency = Math.max(0.0f, Math.min(frequency, sampleRate / 2.0f));
				filter[voice].setFrequency(frequency);
				if (output1.value[voice] > 100 || output1.value[voice] < -100) {
					filter[voice].reset();
				}
			}
		} else {
			filter[voice].setQ(0);
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		if (input1 == null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				output1.value[voice] = 0;
			}
		} else if (resonance <= 0) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				output1.value[voice] = input1.value[voice];
			}
		} else {
			if (filter == null) {
				setupFilters();
			}
			for (int voice = startVoice; voice <= endVoice; voice++) {
				output1.value[voice] = (float) filter[voice].filter(input1.value[voice]);
			}
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (resonanceCC != null && resonanceCC.cc == cc) {
			double v = resonanceCC.range(value) * 100.0 / 10.0;
			resonance = v * v;
		}
		if (cutoffCC != null && cutoffCC.cc == cc) {
			cutoff = cutoffCC.range(value);
		}
		if (sweepCC != null && sweepCC.cc == cc) {
			double v = sweepCC.range(value);
			sweep = v * v;
		}
		if (filterTypeCC != null && filterTypeCC.cc == cc) {
			BiQuadraticFilter.Type newFilterType = BiQuadraticFilter.Type.values()[(int) Math.round(filterTypeCC.range(value))];
			if (!newFilterType.equals(filterType)) {
				filterType = newFilterType;
				setupFilters();
			}
		}
	}

}
