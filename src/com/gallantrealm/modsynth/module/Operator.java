package com.gallantrealm.modsynth.module;

import com.gallantrealm.modsynth.Instrument;

public class Operator extends Module {
	private static final long serialVersionUID = 1L;

	static final double K64 = 65536;
	static final int WAVE_SIZE = 4096; // number of samples in the wave table
	static final int WAVE_MASK = 0xfff; // one less than wave size

	public int octave = 0;
	public int pitch = 0;
	public int detune = 0;
	public double feedback = 0.0;
	public double delay = 0.0;
	public double attack = 0.0;
	public double hold = 0.0;
	public double decay = 0.5;
	public double sustain = 0.0;
	public double release = 0.1;
	public double min = 0.0;
	public double max = 1.0;
	public SlopeType slopeType = SlopeType.LINEAR;
	public boolean velocitySensitive = false;
	public boolean wideDetune = true;
	public boolean keyScale = false;
	public CC octaveCC;
	public CC pitchCC;
	public CC detuneCC;
	public CC feedbackCC;
	public CC delayCC;
	public CC attackCC;
	public CC holdCC;
	public CC decayCC;
	public CC sustainCC;
	public CC releaseCC;
	public CC minCC;
	public CC maxCC;

	private transient float[] waveTable = new float[WAVE_SIZE];
	private transient float wavesizeDivSamplerate;
	private transient int[] fp_freq1;
	private transient int[] fp_phase1;
	private transient float[] oscOutput;

	public static final float HIGHEST = 1.0f;
	public static final float LOWEST = 0.0f;

	private transient float[] envLevel;
	private transient float[] smoothEnvLevel;
	private transient float[] envTime;

	public enum SlopeType {
		LINEAR, ASYMPTOTIC;
		public String toString() {
			if (this == ASYMPTOTIC) {
				return "Asymptotic";
			} else {
				return "Linear";
			}
		}
	}

	private enum Phase {
		RELEASED, DELAYING, ATTACKING, HOLDING, DECAYING, SUSTAINING
	};
	private transient Phase[] envPhase;

	public Operator() {
	}

	@Override
	public int getInputCount() {
		return 3;
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
		if (n == 0) {
			return "Mod 1";
		} else if (n == 1) {
			return "Mod 2";
		} else {
			return "Mod 3";
		}
	}

	@Override
	public String getModName(int n) {
		if (n == 0) {
			return "Gate";
		} else {
			return "Pitch";
		}
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		if (!wideDetune) {
			detune = 10 * detune;
			wideDetune = true;
		}
		wavesizeDivSamplerate = (float) WAVE_SIZE / (float) sampleRate;
		waveTable = new float[WAVE_SIZE];
		for (int i = 0; i < WAVE_SIZE; i++) {
			waveTable[i] = (float) Math.sin(Math.toRadians(360.0 * i / WAVE_SIZE));
		}
		fp_freq1 = new int[voices];
		fp_phase1 = new int[voices];
		envPhase = new Phase[voices];
		envLevel = new float[voices];
		envTime = new float[voices];
		smoothEnvLevel = new float[voices];
		oscOutput = new float[voices];
		if (octaveCC == null) {
			octaveCC = new CC();
			pitchCC = new CC();
			detuneCC = new CC();
			attackCC = new CC();
			decayCC = new CC();
			sustainCC = new CC();
			releaseCC = new CC();
			minCC = new CC();
			maxCC = new CC();
		}
		if (feedbackCC == null) {
			feedbackCC = new CC();
			delayCC = new CC();
			holdCC = new CC();
		}
	}

	public static final float DECAY_FACTOR = 0.1f / Instrument.ENVELOPE_RATE;
	public static final float ASYM_WEIGHT = Instrument.ENVELOPE_RATE * 5.0f;

	private transient float fmax, fmin, fattack, fdecay, fsustain, frelease;

	@Override
	public void doEnvelope(int voice) {
		fmax = (float) max;
		fmin = (float) min;
		fattack = (float) attack;
		fdecay = (float) decay;
		fsustain = (float) sustain;
		frelease = (float) release;

		// oscillator
		float mod2value = (mod2 != null) ? mod2.value[voice] : 0;
		float frequency = (float) Math.pow(2, octave + pitch / 12.0 + detune / 12.0 / 100.0) * pitchToFrequency(mod2value);
		fp_freq1[voice] = (int) (wavesizeDivSamplerate * frequency * K64);

		// envelope
		float keyfactor;
		if (keyScale) {
			keyfactor = 1.0f - (float) Math.min(1.0, Math.sqrt(mod2value * 0.9f));
		} else {
			keyfactor = 1.0f;
		}
		float gate = 0.0f;
		if (mod1 != null) {
			gate = mod1.value[voice];
		}
		if (gate > 0.0) {
			float highest;
			if (velocitySensitive) {
				highest = HIGHEST * gate;
			} else {
				highest = HIGHEST;
			}
			if (envPhase[voice] == Phase.RELEASED) {
				if (delay > 0.0) {
					envPhase[voice] = Phase.DELAYING;
					envTime[voice] = 0.0f;
				} else {
					envPhase[voice] = Phase.ATTACKING;
				}
			}
			if (envPhase[voice] == Phase.DELAYING) {
				envTime[voice] += 1.0f / Instrument.ENVELOPE_RATE;
				if (envTime[voice] > delay) {
					envPhase[voice] = Phase.ATTACKING;
				}
			}
			if (envPhase[voice] == Phase.ATTACKING) {
				envLevel[voice] = Math.min(highest, envLevel[voice] + DECAY_FACTOR / (fattack + DECAY_FACTOR));
				if (envLevel[voice] >= highest - 0.01) {
					if (hold > 0.0) {
						envPhase[voice] = Phase.HOLDING;
						envTime[voice] = 0.0f;
					} else {
						envPhase[voice] = Phase.DECAYING;
					}
				}
			}
			if (envPhase[voice] == Phase.HOLDING) {
				envTime[voice] += 1.0f / Instrument.ENVELOPE_RATE;
				if (envTime[voice] > hold) {
					envPhase[voice] = Phase.DECAYING;
				}
			}
			if (envPhase[voice] == Phase.DECAYING) {
				float tdecay = fdecay * keyfactor;
				if (slopeType == null || slopeType == SlopeType.LINEAR) {
					envLevel[voice] = Math.max(LOWEST, envLevel[voice] - DECAY_FACTOR / (tdecay + DECAY_FACTOR));
				} else {
					envLevel[voice] = (fsustain * (1.0f - tdecay) + ASYM_WEIGHT * envLevel[voice] * tdecay) / (1.0f - tdecay + ASYM_WEIGHT * tdecay);
				}
				if (envLevel[voice] <= sustain) {
					envPhase[voice] = Phase.SUSTAINING;
				}
			}
			if (envPhase[voice] == Phase.SUSTAINING) {
				envLevel[voice] = fsustain;
			}
		} else { // gate <= 0
			if (envPhase[voice] != Phase.HOLDING && envPhase[voice] != Phase.RELEASED) {
				envPhase[voice] = Phase.RELEASED;
			}
			if (envPhase[voice] == Phase.HOLDING) {
				envTime[voice] += 1.0f / Instrument.ENVELOPE_RATE;
				if (envTime[voice] > hold) {
					envPhase[voice] = Phase.RELEASED;
				}
			}
			if (envPhase[voice] == Phase.RELEASED) {
				if (slopeType == null || slopeType == SlopeType.LINEAR) {
					envLevel[voice] = Math.max(LOWEST, envLevel[voice] - DECAY_FACTOR / (frelease + DECAY_FACTOR));
				} else {
					envLevel[voice] = (LOWEST * (1.0f - frelease) + ASYM_WEIGHT * envLevel[voice] * frelease) / (1.0f - frelease + ASYM_WEIGHT * frelease);
				}
			}
		}

	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		for (int voice = startVoice; voice <= endVoice; voice++) {

			// Mix modulators
			double modulation = 0.0;
			if (input1 != null) {
				modulation += input1.value[voice];
			}
			if (input2 != null) {
				modulation += input2.value[voice];
			}
			if (input3 != null) {
				modulation += input3.value[voice];
			}
			modulation += feedback * oscOutput[voice] * 0.1;

			// run oscillator
			int new_phase = fp_phase1[voice] + fp_freq1[voice];
			fp_phase1[voice] = new_phase;
			int offset = (int) (modulation * 32767.0);
			oscOutput[voice] = waveTable[((new_phase >> 16) + offset) & WAVE_MASK];

			// scale w envelope and min/max
			smoothEnvLevel[voice] = 0.9f * smoothEnvLevel[voice] + 0.1f * envLevel[voice];
			float value = oscOutput[voice] * smoothEnvLevel[voice] * (fmax - fmin);
			value += oscOutput[voice] * min;
			output1.value[voice] = value;
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (octaveCC.cc == cc) {
			octave = (int) (octaveCC.range(value) * 10 - 5);
		}
		if (pitchCC.cc == cc) {
			pitch = (int) (pitchCC.range(value) * 12);
		}
		if (detuneCC.cc == cc) {
			detune = (int) (detuneCC.range(value) * 100 - 50);
		}
		if (feedbackCC.cc == cc) {
			feedback = feedbackCC.range(value);
		}
		if (delayCC.cc == cc) {
			double v = delayCC.range(value);
			delay = v;
		}
		if (attackCC.cc == cc) {
			double v = attackCC.range(value);
			attack = v * v;
		}
		if (holdCC.cc == cc) {
			double v = holdCC.range(value);
			hold = v;
		}
		if (decayCC.cc == cc) {
			double v = decayCC.range(value);
			decay = v * v;
		}
		if (sustainCC.cc == cc) {
			sustain = sustainCC.range(value);
		}
		if (releaseCC.cc == cc) {
			double v = releaseCC.range(value);
			release = v * v;
		}
		if (minCC.cc == cc) {
			double v = minCC.range(value);
			min = v * v * v;
		}
		if (maxCC.cc == cc) {
			double v = maxCC.range(value);
			max = v * v * v;
		}
	}

}
