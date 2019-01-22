package com.gallantrealm.modsynth.module;

import java.io.Serializable;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.FastMath;

public class Oscillator extends Module {
	private static final long serialVersionUID = 1L;

	transient float[][] sharedSquareTable;
	transient float[][] sharedSawtoothTable;
	transient float[][] sharedTriangleTable;
	transient float[][] sharedSinTable;

	static final int K64 = 65536;
	static final int WAVE_SIZE = 4096; // number of samples in the wave table
	static final int WAVE_MASK = 0xfff; // one less than wave size
	static final int MAX_OCTAVE = 10;

	public enum WaveForm implements Serializable {
		SQUARE_WAVE, SAWTOOTH_WAVE, TRIANGLE_WAVE, SINE_WAVE, PULSE_WAVE, HARMONICS;
//		private static final long serialVersionUID = 1L;

		public String toString() {
			String string;
			if (this == SQUARE_WAVE) {
				string = "Square";
			} else if (this == SAWTOOTH_WAVE) {
				string = "Sawtooth";
			} else if (this == TRIANGLE_WAVE) {
				string = "Triangle";
			} else if (this == SINE_WAVE) {
				string = "Sine";
			} else if (this == PULSE_WAVE) {
				string = "Pulse";
			} else if (this == HARMONICS) {
				string = "Harmonics";
			} else {
				string = "Square";
			}
			return Translator.getTranslator().translate(string);
		}
	};

	public WaveForm waveForm = WaveForm.SQUARE_WAVE;
	public double duty = 0.5;
	public int octave = 0;
	public int pitch = 0;
	public double detune = 0.0;
	public double modulation = 0.5;
	public double[] harmonics;

	// public double[] waveTable; unused but don't use
	public transient double wavesizeDivSamplerate; // changed from non-transient
	// public double frequency; unused but don't use
	// public int phase; unused but don't use
	// public int fp_freq; unused but don't use
	public double noise;

	public CC octaveCC;
	public CC pitchCC;
	public CC detuneCC;
	public CC noiseCC;
	public CC modulationCC;
	public CC waveformCC;

	transient int[] fp_freq1;
	transient int[] fp_phase1;
	transient float[][] newWaveTable;
	transient int[] waveOctave; // used to index new wave table
	transient float[] waveOctaveWeight; // used to blend wave tables between octaves
	transient float[] randomizer; // used in noise
	transient float[] lastTrigger;
	transient float[] lastPitch;
	transient double smoothModulation; // debounce modulation

	public Oscillator() {
	}

	@Override
	public int getInputCount() {
		return 1;
	}

	@Override
	public int getModCount() {
		return 2;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}

	@Override
	public String getInputName(int n) {
		return "Pitch";
	}

	@Override
	public String getModName(int n) {
		if (n == 0) {
			return "Phase";
		} else {
			return "Reset";
		}
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	public void updateWaveTable() {
		int rateDivisor = 0;
		if (sampleRate >= 40000) {
			rateDivisor = 0;
		} else if (sampleRate >= 20000) {
			rateDivisor = 1;
		} else if (sampleRate >= 10000) {
			rateDivisor = 2;
		} else {
			rateDivisor = 3;
		}
		if (waveForm == WaveForm.SQUARE_WAVE) {
			if (sharedSquareTable == null) {
				sharedSquareTable = new float[MAX_OCTAVE][WAVE_SIZE];
				for (int i = 0; i < WAVE_SIZE; i++) {
					if (i < WAVE_SIZE / 2) {
						sharedSquareTable[0][i] = 1.0f;
					} else {
						sharedSquareTable[0][i] = -1.0f;
					}
				}
				for (int w = 1; w < MAX_OCTAVE; w++) {
					if (MAX_OCTAVE - 1 - w - rateDivisor >= 0) {
						int maxHarmonic = 1 << MAX_OCTAVE - 1 - w - rateDivisor;
						for (int i = 0; i < WAVE_SIZE; i++) {
							sharedSquareTable[w][i] = 0;
							for (int h = 1; h <= maxHarmonic; h += 2) {
								sharedSquareTable[w][i] += FastMath.sin(FastMath.toRadians(360.0f * h * i / WAVE_SIZE)) / h;
							}
						}
					}
				}
			}
			newWaveTable = sharedSquareTable;
		} else if (waveForm == WaveForm.SAWTOOTH_WAVE || waveForm == WaveForm.PULSE_WAVE) {
			if (sharedSawtoothTable == null) {
				sharedSawtoothTable = new float[MAX_OCTAVE][WAVE_SIZE];
				for (int i = 0; i < WAVE_SIZE; i++) {
					sharedSawtoothTable[0][i] = 2.0f * (WAVE_SIZE - i) / WAVE_SIZE - 1.0f;
				}
				for (int w = 1; w < MAX_OCTAVE; w++) {
					if (MAX_OCTAVE - 1 - w - rateDivisor >= 0) {
						int maxHarmonic = 1 << MAX_OCTAVE - 1 - w - rateDivisor;
						for (int i = 0; i < WAVE_SIZE; i++) {
							sharedSawtoothTable[w][i] = 0;
							for (int h = 1; h <= maxHarmonic; h++) {
								sharedSawtoothTable[w][i] += FastMath.sin(FastMath.toRadians(360.0f * h * i / WAVE_SIZE)) / h / 2.0;
							}
						}
					}
				}
			}
			newWaveTable = sharedSawtoothTable;
		} else if (waveForm == WaveForm.TRIANGLE_WAVE) {
			if (sharedTriangleTable == null) {
				sharedTriangleTable = new float[MAX_OCTAVE][WAVE_SIZE];
				for (int i = 0; i < WAVE_SIZE; i++) {
					if (i < WAVE_SIZE / 2) {
						sharedTriangleTable[0][i] = (float) (4 * i) / (float) WAVE_SIZE - 1.0f;
					} else {
						sharedTriangleTable[0][i] = 3.0f - (float) (4 * i) / (float) WAVE_SIZE;
					}
				}
				for (int w = 1; w < MAX_OCTAVE; w++) {
					if (MAX_OCTAVE - 1 - w - rateDivisor >= 0) {
						int maxHarmonic = 1 << MAX_OCTAVE - 1 - w - rateDivisor;
						for (int i = 0; i < WAVE_SIZE; i++) {
							sharedTriangleTable[w][i] = 0;
							int sign = 2;
							for (int h = 1; h <= maxHarmonic; h += 2) {
								sharedTriangleTable[w][i] += sign * FastMath.sin(FastMath.toRadians(360.0f * h * i / WAVE_SIZE)) / (2 * h * h);
								sign *= -1;
							}
						}
					}
				}
			}
			newWaveTable = sharedTriangleTable;
		} else if (waveForm == WaveForm.SINE_WAVE) {
			if (sharedSinTable == null) {
				sharedSinTable = new float[MAX_OCTAVE][WAVE_SIZE];
				for (int i = 0; i < WAVE_SIZE; i++) {
					float y = (float) FastMath.sin(FastMath.toRadians(360.0f * i / WAVE_SIZE));
					for (int w = 0; w < MAX_OCTAVE; w++) {
						if (MAX_OCTAVE - 1 - w - rateDivisor >= 0) {
							sharedSinTable[w][i] = y;
						}
					}
				}
			}
			newWaveTable = sharedSinTable;
		} else if (waveForm == WaveForm.HARMONICS) {
			newWaveTable = new float[MAX_OCTAVE][WAVE_SIZE];
			int harmonicCount = 0;
			if (harmonics != null) {
				harmonicCount = harmonics.length;
			}
			float max = 0.0f;
			for (int w = 0; w < MAX_OCTAVE; w++) {
				if (MAX_OCTAVE - 1 - w - rateDivisor >= 0) {
					int maxHarmonic = Math.min(harmonicCount, 1 << MAX_OCTAVE - 1 - w - rateDivisor);
					for (int i = 0; i < WAVE_SIZE; i++) {
						newWaveTable[w][i] = 0;
						for (int h = 0; h < maxHarmonic; h++) {
							float y = (float) (FastMath.sin((h + 1) * FastMath.toRadians(360.0f * i / WAVE_SIZE)) * harmonics[h] / harmonicCount);
							newWaveTable[w][i] += y;
						}
						max = FastMath.max(max, newWaveTable[w][i]);
					}
					if (max > 0.0) {
						for (int i = 0; i < WAVE_SIZE; i++) {
							newWaveTable[w][i] = newWaveTable[w][i] / max;
						}
					}
				}
			}
		}
	}

	public double getDuty() {
		return duty;
	}

	public void setDuty(double duty) {
		this.duty = duty;
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		wavesizeDivSamplerate = (double) WAVE_SIZE / (double) sampleRate;
		fp_freq1 = new int[voices];
		fp_phase1 = new int[voices];
		waveOctave = new int[voices];
		waveOctaveWeight = new float[voices];
		randomizer = new float[voices];
		lastTrigger = new float[voices];
		lastPitch = new float[voices];
		for (int v = 0; v < voices; v++) {
			lastPitch[v] = -1000;
		}
		updateWaveTable();
		if (octaveCC == null) {
			octaveCC = new CC();
		}
		if (pitchCC == null) {
			pitchCC = new CC();
		}
		if (detuneCC == null) {
			detuneCC = new CC();
		}
		if (noiseCC == null) {
			noiseCC = new CC();
		}
		if (modulationCC == null) {
			modulationCC = new CC();
		}
		if (waveformCC == null) {
			waveformCC = new CC();
			waveformCC.setRangeLimits(0, WaveForm.values().length - 1);
		}
	}

	@Override
	public void doEnvelope(int voice) {
		float newPitch = (float) ((octave * 12 + pitch + detune / 100.0) / NOTES_PER_VOLT + (input1 != null ? input1.value[voice] : 0));
		if (newPitch != lastPitch[voice]) {
			lastPitch[voice] = newPitch;
			float frequency = pitchToFrequency(newPitch);
			frequency = Math.max(0.0f, Math.min(frequency, sampleRate / 2.0f));
			fp_freq1[voice] = (int) (wavesizeDivSamplerate * frequency * K64);
			waveOctave[voice] = (int) FastMath.max(0, FastMath.min(MAX_OCTAVE - 1, frequencyToPitch(frequency) * NOTES_PER_VOLT / 12));
			if (waveOctave[voice] >= MAX_OCTAVE - 1) {
				waveOctaveWeight[voice] = 0.0f;
			} else {
				waveOctaveWeight[voice] = Math.max(0.0f, frequencyToPitch(frequency) * NOTES_PER_VOLT / 12.0f) - waveOctave[voice];
			}
		}
		if (voice == 0) {
			if (smoothModulation - modulation > 0.25 || smoothModulation - modulation < -0.25) {
				smoothModulation = modulation;
			} else {
				smoothModulation = 0.9 * smoothModulation + 0.1 * modulation;
			}
		}
	}

	int t; // unused

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		if (mod2 != null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				if (mod2.value[voice] > 0 && lastTrigger[voice] <= 0) {
					fp_phase1[voice] = 0;
				}
				lastTrigger[voice] = mod2.value[voice];
			}
		}
		if (waveForm == WaveForm.PULSE_WAVE) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				int new_phase = fp_phase1[voice] + (int) ((1.0 + randomizer[voice]) * fp_freq1[voice]);
				fp_phase1[voice] = new_phase;
				int w = waveOctave[voice];
				int dduty = (int) (duty * WAVE_SIZE / 2.0);
				if (mod1 != null) {
					dduty -= (int) (smoothModulation * mod1.value[voice] * WAVE_SIZE / 4);
				}
				float newValue = newWaveTable[w][(new_phase >> 16) & WAVE_MASK] - newWaveTable[w][((new_phase >> 16) + dduty) & WAVE_MASK];
				float weight = waveOctaveWeight[voice];
				if (weight == 0.0f) {
					output1.value[voice] = newValue;
				} else {
					float newValue2 = newWaveTable[w + 1][(new_phase >> 16) & WAVE_MASK] - newWaveTable[w + 1][((new_phase >> 16) + dduty) & WAVE_MASK];
					output1.value[voice] = (1.0f - weight) * newValue + weight * newValue2;
				}
				if (noise > 0.0 && newValue >= 0 && output1.value[voice] < 0) {
					randomizer[voice] = (float) noise * ((float) FastMath.random() - 0.5f);
				}
			}
		} else {
			if (noise > 0.0) {
				if (mod1 != null) {
					for (int voice = startVoice; voice <= endVoice; voice++) {
						int new_phase = fp_phase1[voice] + (int) ((1.0 + randomizer[voice]) * fp_freq1[voice]);
						fp_phase1[voice] = new_phase;
						int w = waveOctave[voice];
						int offset = (int) (smoothModulation * mod1.value[voice] * 32767.0);
						float newValue = newWaveTable[w][((new_phase >> 16) + offset) & WAVE_MASK];
						if (newValue >= 0 && output1.value[voice] < 0) {
							randomizer[voice] = (float) noise * ((float) FastMath.random() - 0.5f);
						}
						output1.value[voice] = newValue;
					}
				} else { // mod1 == null
					for (int voice = startVoice; voice <= endVoice; voice++) {
						int new_phase = fp_phase1[voice] + (int) ((1.0 + randomizer[voice]) * fp_freq1[voice]);
						fp_phase1[voice] = new_phase;
						int w = waveOctave[voice];
						float newValue = newWaveTable[w][(new_phase >> 16) & WAVE_MASK];
						if (newValue >= 0 && output1.value[voice] < 0) {
							randomizer[voice] = (float) noise * ((float) FastMath.random() - 0.5f);
						}
						output1.value[voice] = newValue;
					}
				}
			} else { // noise == 0
				if (mod1 != null) {
					for (int voice = startVoice; voice <= endVoice; voice++) {
						int new_phase = fp_phase1[voice] + (int) (fp_freq1[voice]);
						fp_phase1[voice] = new_phase;
						int wave = waveOctave[voice];
						int offset = (int) (smoothModulation * mod1.value[voice] * 32767.0);
						float newValue = newWaveTable[wave][((new_phase >> 16) + offset) & WAVE_MASK];
						float weight = waveOctaveWeight[voice];
						if (weight == 0.0f) {
							output1.value[voice] = newValue;
						} else {
							float newValue2 = newWaveTable[wave + 1][((new_phase >> 16) + offset) & WAVE_MASK];
							output1.value[voice] = (1.0f - weight) * newValue + weight * newValue2;
						}
					}
				} else { // mod1 == null
					for (int voice = startVoice; voice <= endVoice; voice++) {
						int new_phase = fp_phase1[voice] + (int) (fp_freq1[voice]);
						fp_phase1[voice] = new_phase;
						int wave = waveOctave[voice];
						float newValue = newWaveTable[wave][(new_phase >> 16) & WAVE_MASK];
						float weight = waveOctaveWeight[voice];
						if (weight == 0.0f) {
							output1.value[voice] = newValue;
						} else {
							float newValue2 = newWaveTable[wave + 1][((new_phase >> 16)) & WAVE_MASK];
							output1.value[voice] = (1.0f - weight) * newValue + weight * newValue2;
						}
					}
				}
			}
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (octaveCC.cc == cc) {
			octave = (int) (octaveCC.range(value) * 10.0) - 5;
		}
		if (pitchCC.cc == cc) {
			pitch = (int) (pitchCC.range(value) * 12.0);
		}
		if (detuneCC.cc == cc) {
			detune = detuneCC.range(value) * 100 - 50;
		}
		if (noiseCC.cc == cc) {
			noise = Math.pow(noiseCC.range(value), 2.0);
		}
		if (modulationCC.cc == cc) {
			modulation = Math.pow(modulationCC.range(value), 2.0);
		}
		if (waveformCC.cc == cc) {
			WaveForm newWaveForm = WaveForm.values()[(int) Math.round(waveformCC.range(value))];
			if (!newWaveForm.equals(waveForm)) {
				waveForm = newWaveForm;
				updateWaveTable();
			}
		}
	}

}
