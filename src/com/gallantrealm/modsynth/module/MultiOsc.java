package com.gallantrealm.modsynth.module;

public class MultiOsc extends Oscillator {
	private static final long serialVersionUID = 1L;

	public double chorusWidth = 1.0;
	public CC chorusWidthCC;

	private transient int[] fp_freq2;
	private transient int[] fp_phase2;
	private transient int[] fp_freq3;
	private transient int[] fp_phase3;
	private transient int[] fp_freq4;
	private transient int[] fp_phase4;
	private transient int[] fp_freq5;
	private transient int[] fp_phase5;
	private transient double[] lastChorusWidth;

	public MultiOsc() {
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		fp_freq2 = new int[voices];
		fp_phase2 = new int[voices];
		fp_freq3 = new int[voices];
		fp_phase3 = new int[voices];
		fp_freq4 = new int[voices];
		fp_phase4 = new int[voices];
		fp_freq5 = new int[voices];
		fp_phase5 = new int[voices];
		for (int i = 0; i < voices; i++) {
			fp_phase1[i] = (int) (10000000000.0 * Math.random());
			fp_phase2[i] = (int) (-10000000000.0 * Math.random());
			fp_phase3[i] = (int) (20000000000.0 * Math.random());
			fp_phase4[i] = (int) (-20000000000.0 * Math.random());
			fp_phase5[i] = (int) (30000000000.0 * Math.random());
		}
		lastChorusWidth = new double[voices];
		updateWaveTable();
		if (chorusWidthCC == null) {
			chorusWidthCC = new CC();
		}
	}

	private transient float foctave, fpitch, fdetune, fchorusWidth;

	@Override
	public void doEnvelope(int voice) {
		float newPitch = (float) ((octave * 12 + pitch + detune / 100.0) / NOTES_PER_VOLT + (input1 != null ? input1.value[voice] : 0));
		if (newPitch != lastPitch[voice] || lastChorusWidth[voice] != chorusWidth) {
			lastPitch[voice] = newPitch;
			lastChorusWidth[voice] = chorusWidth;
			foctave = (float) octave;
			fpitch = (float) pitch;
			fdetune = (float) detune;
			fchorusWidth = (float) chorusWidth;
			float frequency, frequency2, frequency3, frequency4, frequency5;
			float value;
			if (input1 != null) {
				value = input1.value[voice];
			} else {
				value = 0;
			}
			float f = pitchToFrequency(value);
			frequency = (float) Math.pow(2, foctave + fpitch / 12.0f + fdetune / 12.0f / 100.0f) * f;
			frequency2 = (float) Math.pow(2, foctave + fpitch / 12.0f + (fdetune - 5.0f * fchorusWidth) / 12.0f / 100.0f) * f;
			frequency3 = (float) Math.pow(2, foctave + fpitch / 12.0f + (fdetune + 7.0f * fchorusWidth) / 12.0f / 100.0f) * f;
			frequency4 = (float) Math.pow(2, foctave + fpitch / 12.0f + (fdetune - 11.0f * fchorusWidth) / 12.0f / 100.0f) * f;
			frequency5 = (float) Math.pow(2, foctave + fpitch / 12.0f + (fdetune + 13.0f * fchorusWidth) / 12.0f / 100.0f) * f;
			fp_freq1[voice] = (int) (wavesizeDivSamplerate * frequency * K64);
			fp_freq2[voice] = (int) (wavesizeDivSamplerate * frequency2 * K64);
			fp_freq3[voice] = (int) (wavesizeDivSamplerate * frequency3 * K64);
			fp_freq4[voice] = (int) (wavesizeDivSamplerate * frequency4 * K64);
			fp_freq5[voice] = (int) (wavesizeDivSamplerate * frequency5 * K64);
			waveOctave[voice] = (int) Math.max(0, Math.min(MAX_OCTAVE - 1, frequencyToPitch(frequency) * NOTES_PER_VOLT / 12));
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		for (int voice = startVoice; voice <= endVoice; voice++) {
			int new_phase = fp_phase1[voice] + fp_freq1[voice];
			int new_phase2 = fp_phase2[voice] + fp_freq2[voice];
			int new_phase3 = fp_phase3[voice] + fp_freq3[voice];
			int new_phase4 = fp_phase4[voice] + fp_freq4[voice];
			int new_phase5 = fp_phase5[voice] + fp_freq5[voice];
			fp_phase1[voice] = new_phase;
			fp_phase2[voice] = new_phase2;
			fp_phase3[voice] = new_phase3;
			fp_phase4[voice] = new_phase4;
			fp_phase5[voice] = new_phase5;
			int w = waveOctave[voice];
			if (mod1 != null && waveForm != WaveForm.PULSE_WAVE) {
				int offset = (int) (modulation * mod1.value[voice] * 32767.0);
				output1.value[voice] = newWaveTable[w][((new_phase >> 16) + offset) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase2 >> 16) + offset) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase3 >> 16) + offset) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase4 >> 16) + offset) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase5 >> 16) + offset) & WAVE_MASK];
			} else {
				output1.value[voice] = newWaveTable[w][(new_phase >> 16) & WAVE_MASK] //
						+ newWaveTable[w][(new_phase2 >> 16) & WAVE_MASK] //
						+ newWaveTable[w][(new_phase3 >> 16) & WAVE_MASK] //
						+ newWaveTable[w][(new_phase4 >> 16) & WAVE_MASK] //
						+ newWaveTable[w][(new_phase5 >> 16) & WAVE_MASK];
			}
			if (waveForm == WaveForm.PULSE_WAVE) {
				int dduty = (int) (duty * WAVE_SIZE / 2.0);
				if (mod1 != null) {
					dduty += modulation * mod1.value[voice] * WAVE_SIZE / 4;
				}
				output1.value[voice] -= (newWaveTable[w][((new_phase >> 16) + dduty) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase2 >> 16) + dduty) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase3 >> 16) + dduty) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase4 >> 16) + dduty) & WAVE_MASK] //
						+ newWaveTable[w][((new_phase5 >> 16) + dduty) & WAVE_MASK]);
			}
			output1.value[voice] *= 0.2;
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (chorusWidthCC.cc == cc) {
			double v = chorusWidthCC.range(value);
			chorusWidth = 12.0 * Math.pow(Math.max(1.0 / 100.0, v), 2.0);
		}
	}

}
