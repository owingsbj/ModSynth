package com.gallantrealm.modsynth.module;

import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.Stringifier;

public class Envelope extends Module {
	private static final long serialVersionUID = 1L;

	public enum SlopeType {
		LINEAR, ASYMPTOTIC;
		public String toString() {
			if (this == ASYMPTOTIC) {
				return Translator.getTranslator().translate("Asymptotic");
			} else {
				return Translator.getTranslator().translate("Linear");
			}
		}
	}

	public enum Phase {
		RELEASED, DELAYING, ATTACKING, HOLDING, DECAYING, SUSTAINING
	};

	public double delay = 0.0;
	public double attack = 0.0;
	public double hold = 0.0;
	public double decay = 0.25;
	public double sustain = 0.5;
	public double release = 0.0625;
	public SlopeType slopeType = SlopeType.LINEAR;
	public boolean velocitySensitive = false;
	public CC delayCC;
	public CC attackCC;
	public CC holdCC;
	public CC decayCC;
	public CC sustainCC;
	public CC releaseCC;
	public CC slopeTypeCC;
	public CC velocitySensitiveCC;

	public static final float HIGHEST = 1.0f;
	public static final float LOWEST = 0.0f;

	private transient Phase[] envPhase;
	private transient float[] envTime;

	@Override
	public void stringify(Stringifier s) {
		super.stringify(s);
		s.add("attack", attack);
		s.add("decay", decay);
		s.add("sustain", sustain);
		s.add("release", release);
		s.add("slopeType", slopeType == null ? 0 : slopeType.ordinal());
		s.add("velocitySensitive", velocitySensitive);
	}

	@Override
	public int getInputCount() {
		return 1;
	}

	@Override
	public int getModCount() {
		return 0;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}

	@Override
	public String getInputName(int n) {
		return "Gate";
	}

	@Override
	public String getOutputName(int n) {
		return "Gain";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		envPhase = new Phase[voices];
		envTime = new float[voices];
		if (delayCC == null) {
			delayCC = new CC();
		}
		if (attackCC == null) {
			attackCC = new CC();
		}
		if (holdCC == null) {
			holdCC = new CC();
		}
		if (decayCC == null) {
			decayCC = new CC();
		}
		if (sustainCC == null) {
			sustainCC = new CC();
		}
		if (releaseCC == null) {
			releaseCC = new CC();
		}
		if (slopeTypeCC == null) {
			slopeTypeCC = new CC();
			slopeTypeCC.setRangeLimits(0, SlopeType.values().length - 1);
		}
		if (velocitySensitiveCC == null) {
			velocitySensitiveCC = new CC();
			velocitySensitiveCC.setRangeLimits(0, 1);
		}
	}

	public static final float DECAY_FACTOR = 0.1f / Instrument.ENVELOPE_RATE;
	public static final float ASYM_WEIGHT = Instrument.ENVELOPE_RATE * 5.0f;

	@Override
	public void doEnvelope(int voice) {
		float fattack = (float) attack;
		float fdecay = (float) decay;
		float fsustain = (float) sustain;
		float frelease = (float) release;
		if (input1 != null && input1.value[voice] > 0.0) {
			float highest;
			if (velocitySensitive) {
				highest = input1.value[voice];
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
				output1.value[voice] = Math.min(highest, output1.value[voice] + DECAY_FACTOR / (fattack + DECAY_FACTOR));
				if (output1.value[voice] >= highest) {
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
				if (slopeType == null || slopeType == SlopeType.LINEAR) {
					output1.value[voice] = Math.max(LOWEST, output1.value[voice] - DECAY_FACTOR / (fdecay + DECAY_FACTOR));
				} else {
					output1.value[voice] = (fsustain * (1.0f - fdecay) + ASYM_WEIGHT * output1.value[voice] * fdecay) / (1.0f - fdecay + ASYM_WEIGHT * fdecay);
				}
				if (output1.value[voice] <= sustain) {
					envPhase[voice] = Phase.SUSTAINING;
				}
			}
			if (envPhase[voice] == Phase.SUSTAINING) {
				output1.value[voice] = (float) sustain;
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
					output1.value[voice] = Math.max(LOWEST, output1.value[voice] - DECAY_FACTOR / (frelease + DECAY_FACTOR));
				} else {
					output1.value[voice] = (LOWEST * (1.0f - frelease) + ASYM_WEIGHT * output1.value[voice] * frelease) / (1.0f - frelease + ASYM_WEIGHT * frelease);
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

	@Override
	public void updateCC(int cc, double value) {
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
		if (slopeTypeCC.cc == cc) {
			double v = slopeTypeCC.range(value);
			slopeType = SlopeType.values()[(int) Math.round(v)];
		}
		if (velocitySensitiveCC.cc == cc) {
			double v = velocitySensitiveCC.range(value);
			velocitySensitive = v < 0.5 ? false : true;
		}
	}

}
