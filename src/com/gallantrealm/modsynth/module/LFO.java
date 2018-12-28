package com.gallantrealm.modsynth.module;

import java.io.Serializable;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.Stringifier;

public class LFO extends Module {
	private static final long serialVersionUID = 1L;

	static final double K64 = 65536;
	static final int WAVE_SIZE = 4096; // number of samples in the wave table
	static final int WAVE_MASK = 0xfff; // one less than wave size
	static final int MAX_OCTAVE = 10;

	public enum WaveForm implements Serializable {
		SQUARE_WAVE, SAWTOOTH_WAVE, REVERSE_SAW, TRIANGLE_WAVE, SINE_WAVE, CUSTOM;
		private static final long serialVersionUID = 1L;

		public String toString() {
			String string;
			if (this == SQUARE_WAVE) {
				string = "Square";
			} else if (this == SAWTOOTH_WAVE) {
				string = "Sawtooth";
			} else if (this == REVERSE_SAW) {
				string = "Reverse Saw";
			} else if (this == TRIANGLE_WAVE) {
				string = "Triangle";
			} else if (this == SINE_WAVE) {
				string = "Sine";
			} else if (this == CUSTOM) {
				string = "Custom";
			} else {
				string = "Square";
			}
			return Translator.getTranslator().translate(string);
		}
	};

	public WaveForm waveForm = WaveForm.SQUARE_WAVE;
	public int octave = -1;
	public boolean positive;
	public boolean pulse;
	public boolean midiSync;

	public double[] waveTable = new double[WAVE_SIZE];
	public double wavesizeDivSamplerate;
	public double frequency = 10;
	public double random;
	public boolean removedOctave = true;
	public double fadeIn;

	public CC octaveCC;
	public CC frequencyCC;
	public CC randomCC;
	public CC modulationCC;
	public CC waveformCC;
	public CC fadeInCC;

	transient int fp_freq;
	transient int fp_phase;
	transient double randomizer; // used in noise
	transient double[] lastTrigger;
	transient double[] fadeInLevel;
	transient float bpm;

	public LFO() {
	}

	@Override
	public void stringify(Stringifier s) {
		super.stringify(s);
		s.add("waveForm", waveForm.ordinal());
		s.add("exponent", octave);
		s.add("frequency", frequency);
		s.add("positive", positive);
		s.add("pulse", pulse);
		s.add("waveTable", waveTable);
		s.add("random", random);
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
		return "Reset";
	}

	@Override
	public String getModName(int n) {
		return "";
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	public void updateWaveTable() {
		if (waveForm == WaveForm.SQUARE_WAVE) {
			for (int i = 0; i < WAVE_SIZE; i++) {
				if (i < WAVE_SIZE / 2) {
					waveTable[i] = 1.0f;
				} else {
					waveTable[i] = -1.0f;
				}
			}
		} else if (waveForm == WaveForm.SAWTOOTH_WAVE) {
			for (int i = 0; i < WAVE_SIZE; i++) {
				waveTable[i] = 2.0 * (WAVE_SIZE - i) / WAVE_SIZE - 1.0;
			}
		} else if (waveForm == WaveForm.REVERSE_SAW) {
			for (int i = 0; i < WAVE_SIZE; i++) {
				waveTable[i] = 2.0 * i / WAVE_SIZE - 1.0;
			}
			waveTable[WAVE_SIZE - 1] = -1.0;
		} else if (waveForm == WaveForm.TRIANGLE_WAVE) {
			for (int i = 0; i < WAVE_SIZE; i++) {
				if (i < WAVE_SIZE / 2) {
					waveTable[i] = (double) (4 * i) / (double) WAVE_SIZE - 1.0;
				} else {
					waveTable[i] = 3.0 - (double) (4 * i) / (double) WAVE_SIZE;
				}
			}
		} else if (waveForm == WaveForm.SINE_WAVE) {
			for (int i = 0; i < WAVE_SIZE; i++) {
				waveTable[i] = Math.sin(Math.toRadians(360.0 * i / WAVE_SIZE - 90.0));
			}
		} else if (waveForm == WaveForm.CUSTOM) {
			// up to user to draw it!
		}
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		wavesizeDivSamplerate = (double) WAVE_SIZE / (double) sampleRate;
		lastTrigger = new double[voices];
		fadeInLevel = new double[voices];
		updateWaveTable();
		if (!removedOctave) {
			frequency = frequency * Math.pow(10, octave);
			removedOctave = true;
		}
		if (octaveCC == null) {
			octaveCC = new CC();
		}
		if (frequencyCC == null) {
			frequencyCC = new CC();
		}
		if (randomCC == null) {
			randomCC = new CC();
		}
		if (fadeInCC == null) {
			fadeInCC = new CC();
		}
		if (modulationCC == null) {
			modulationCC = new CC();
		}
		if (waveformCC == null) {
			waveformCC = new CC();
			waveformCC.setRangeLimits(0,  WaveForm.values().length -1);
		}
		bpm = 120;
	}

	@Override
	public void doEnvelope(int voice) {
		if (voice == 0) {
			if (midiSync) {
				double f = ((int)(frequency * 16)) / 16.0 / 120.0 * bpm;
				fp_freq = (int) (wavesizeDivSamplerate * f * K64);
			} else {
				fp_freq = (int) (wavesizeDivSamplerate * frequency * K64);
			}
		}
		if (fadeIn == 0.0) {
			fadeInLevel[voice] = 1.0;
		} else {
			fadeInLevel[voice] = Math.min(1.0,  fadeInLevel[voice] + 10 * (1.01 - fadeIn) / Instrument.ENVELOPE_RATE);
		}
	}

	int t; // unused

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		if (input1 != null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				if (input1.value[voice] > 0 && lastTrigger[voice] <= 0) {
					fp_phase = 0;
					fadeInLevel[voice] = 0.0;
				}
				lastTrigger[voice] = input1.value[voice];
			}
		}
		float newValue;
		if (random > 0.0) {
			if (pulse && input1 != null && (fp_phase >> 16) >= WAVE_SIZE) {
				newValue = (float) (waveTable[WAVE_SIZE - 1] + (positive ? 1.0 : 0.0));
			} else {
				int new_phase = fp_phase + (int) ((1.0 + randomizer) * fp_freq);
				fp_phase = new_phase;
				newValue = (float) (waveTable[(new_phase >> 16) & WAVE_MASK]);
				if (newValue >= 0 && output1.value[0] < (positive ? 1.0 : 0.0)) {
					randomizer = random * (Math.random() - 0.5);
				}
				newValue = newValue + (positive ? 1.0f : 0.0f);
			}
		} else { // noise == 0
			if (pulse && input1 != null && (fp_phase >> 16) >= WAVE_SIZE) {
				newValue = (float) (waveTable[WAVE_SIZE - 1] + (positive ? 1.0 : 0.0));
			} else {
				int new_phase = fp_phase + (int) (fp_freq);
				fp_phase = new_phase;
				newValue = (float) (waveTable[(new_phase >> 16) & WAVE_MASK]);
				newValue = newValue + (positive ? 1.0f : 0.0f);
			}
		}
		for (int voice = startVoice; voice <= endVoice; voice++) {
			if (fadeIn > 0.0) {
				output1.value[voice] = newValue * (float)fadeInLevel[voice];
			} else {
				output1.value[voice] = newValue;
			}
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (frequencyCC.cc == cc) {
			frequency = Math.max(0.0001, Math.pow(frequencyCC.range(value), 2.0) * 20.0);   // 0.0001hz to 20 hz
		}
		if (randomCC.cc == cc) {
			random = Math.pow(randomCC.range(value), 2.0);
		}
		if (waveformCC.cc == cc) {
			WaveForm newWaveForm = WaveForm.values()[(int) Math.round(waveformCC.range(value))];
			if (!newWaveForm.equals(waveForm)) {
				waveForm = newWaveForm;
				updateWaveTable();
			}
		}
		if (fadeInCC.cc == cc) {
			fadeIn = Math.pow(fadeInCC.range(value), 0.25);
		}
	}
	
	long lastMidiClock, lastLastMidiClock, lastLastLastMidiClock, lastLastLastLastMidiClock;
	
	@Override
	public void midiClock() {
		long midiClock = System.currentTimeMillis();
		if (lastMidiClock != 0 && lastLastMidiClock != 0 && lastLastLastMidiClock != 0 && lastLastLastLastMidiClock != 0) {
			long timeSpan = midiClock - lastLastLastLastMidiClock;
			if (timeSpan > 0 && timeSpan < 1000) { // a reasonable clock rate
				bpm = 1000.0f / (timeSpan * 6.0f) * 60.0f;   // 24 times a quarter note (ie, 6 every-fourth time in a beat)
			}
		}
		lastLastLastLastMidiClock = lastLastLastMidiClock;
		lastLastLastMidiClock = lastLastMidiClock;
		lastLastMidiClock = lastMidiClock;
		lastMidiClock = midiClock;
	}

}
