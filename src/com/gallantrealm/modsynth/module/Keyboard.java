package com.gallantrealm.modsynth.module;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.Settings;

public class Keyboard extends Module {
	private static final long serialVersionUID = 1L;

	public static final int TUNING_EQUAL_TEMPERAMENT = 0;
	public static final int TUNING_JUST_INTONATION = 1;
	public static final int TUNING_BAD = 2;

	public int tuning = 0;
	public int octave = 3;
	public int key = 0;
	public double portamento = 0.0;
	public boolean hold; // not used currently
	public int voices = 0;  // mono
	public CC portamentoCC;
	public boolean legato = false;  // deprecated, voices used now (0=mono, 1=legato)
	public CC voicesCC;
	public CC tuningCC;
	public CC octaveCC;
	public CC sustainCC;

//	private transient float[] prepreLevel;
	private transient float[] preLevel;
	private transient boolean[] pressed;
	private transient float[] pressedVelocity;
	private transient boolean[] justReleased;
	private transient boolean sustaining;
	private transient float bend;
	private transient float[] pressure;
	private transient float[] smoothPressure;
	private transient boolean[] damped;
	private transient int[] noteForVoice;
	public transient boolean sliding; // used to determine when to apply legato
	public transient float[] noteVelocities;

	@Override
	public int getInputCount() {
		return 0;
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
	public String getOutputName(int n) {
		if (n == 0) {
			return "Pitch";
		} else {
			return "Gate";
		}
	}

	public void notePress(int note, float velocity) {
		if (voices <= 1 && isAnyNotePressed()) {
			sliding = true;
		} else {
			sliding = false;
		}
		noteVelocities[note] = velocity;
		int voice = getVoice(note);
		//System.out.println("Press: note " + note + " voice " + voice + " velociy " + velocity);
		lastNote[voice] = note;
		voicePlaying[voice] = true;
		preLevel[voice] = frequencyToPitch(noteToFrequency(note, key, 0, 0)) + Settings.tuningCents / 100.0f / 100.0f;
		pressed[voice] = true;
		noteForVoice[voice] = note;
		damped[voice] = false;
		pressedVelocity[voice] = velocity;
		pressure[voice] = velocity;
		smoothPressure[voice] = velocity;
	}

	public void noteRelease(int note) {
		noteVelocities[note] = 0.0f;
		if (!isPlaying(note)) {
			//System.out.println("Release: note " + note + " voice stolen");
			return; // stolen
		}
		int voice = getVoice(note);
		//System.out.println("Release: note " + note + " voice " + voice);
		if (sustaining) {
			damped[voice] = true;
		} else {
//			output2.value[voice] = 0.0f;
			damped[voice] = false;
		}
		pressed[voice] = false;
		voicePlaying[voice] = false;
		justReleased[voice] = true;
		if (voices == 1) {
			// shift to play the highest not pressed
			for (int i = noteVelocities.length - 1; i >= 0; i--) {
				if (noteVelocities[i] > 0.0) {
					if (voices == 1) { // legato
						justReleased[voice] = false;  // to avoid retriggering note
					}
					notePress(i, noteVelocities[i]);
					return;
				}
			}
		}
	}
	
	public boolean isAnyNotePressed() {
		for (int i = 0; i < noteVelocities.length; i++) {
			if (noteVelocities[i] > 0.0f) {
				return true;
			}
		}
		return false;
	}

	public boolean isPlaying(int note) {
		int playingVoices = voices == 0 ? 1 : voices;
		for (int i = 0; i < playingVoices; i++) {
			if (pressed[i] || damped[i]) {
				if (noteForVoice[i] == note) {
					return true;
				}
			}
		}
		return false;
	}

	private transient int[] lastNote;
	private transient boolean[] voicePlaying;
	private transient int nextVoice;

	/**
	 * Returns either the quietest voice, or the voice that last played the note
	 */
	public int getVoice(int note) {
		if (voices <= 1) {
			return 0;
		}
		// return voice used on same note if available
		for (int v = 0; v < voices; v++) {
			if (note == lastNote[v]) {
				return v;
			}
		}
		// use the next voice not playing
		for (int i = 0; i < voices; i++) {
			nextVoice = (nextVoice + 1) % voices;
			if (!voicePlaying[nextVoice]) {
				return nextVoice;
			}
		}
		// if all voices playing, steal a voice
		nextVoice = (nextVoice + 1) % voices;
		return nextVoice;
	}

	public int getLastNoteForVoice(int voice) {
		return lastNote[voice];
	}

	public void setSustaining(boolean sustaining) {
		this.sustaining = sustaining;
		System.out.println("Sustaining: "+this.sustaining);
		if (!sustaining) {
			for (int v = 0; v < voices; v++) {
				if (damped[v]) {
//					output2.value[v] = 0.0f;
					pressed[v] = false;
					voicePlaying[v] = false;
					damped[v] = false;
				}
			}
		}
	}

	public boolean getSustaining() {
		return sustaining;
	}

	public void pitchBend(float bend) {
		this.bend = bend;
	}

	public void pressure(int voice, float amount) {
		this.pressure[voice] = amount;
	}

	public void pressure(float amount) {
		boolean voicePressed = false;
		int playingVoices = voices == 0 ? 1 : voices;
		for (int i = 0; i < playingVoices; i++) {
			if (voicePlaying[i]) {
				pressure[i] = amount;
				voicePressed = true;
			}
		}
		// if no voice is playing, start up the last voice played. EWI will send pressure without note on so need to use last note off
//		if (!voicePressed) {
//			System.out.println("SIMULATED keypress " + lastNotePressedOrReleased + " " + amount);
//			press(lastNotePressedOrReleased, amount);
//		}
	}

	float noteToFrequency(int note, int key, float detune, float bend) {
		if (tuning == TUNING_EQUAL_TEMPERAMENT) {
			return (float) (Math.pow(2, (note + key) / 12.0f + octave - 3 + (detune + bend))) * C0;
		} else if (tuning == TUNING_JUST_INTONATION) { // this is 5 limit tuning
			int octaveAdjusted = octave - 3;
			while (note < 0) {
				note += 12;
				octaveAdjusted -= 1;
			}
			while (note >= 12) {
				note -= 12;
				octaveAdjusted += 1;
			}
			float freq;
			if (note == 0) {
				freq = C0;
			} else if (note == 1) {
				freq = C0 * 16.0f / 15.0f;
			} else if (note == 2) {
				freq = C0 * 9.0f / 8.0f; // 5 limit
				// freq = C0 * 8.0f / 7.0f; // 7 limit
			} else if (note == 3) {
				freq = C0 * 6.0f / 5.0f;
			} else if (note == 4) {
				freq = C0 * 5.0f / 4.0f;
			} else if (note == 5) {
				freq = C0 * 4.0f / 3.0f;
			} else if (note == 6) {
				// freq = C0 * 7.0f / 5.0f; // augmented fourth (vs 10/7 for
				// diminished fifth)
				freq = C0 * 1.4142f; // sqrt, see
										// nicksworldofsynthesizers.com/flashorgan.php
			} else if (note == 7) {
				freq = C0 * 3.0f / 2.0f;
			} else if (note == 8) {
				freq = C0 * 8.0f / 5.0f;
			} else if (note == 9) {
				freq = C0 * 5.0f / 3.0f;
			} else if (note == 10) {
				freq = C0 * 9.0f / 5.0f; // 5 limit
				// freq = C0 * 7.0f / 4.0f; // 7 limit
			} else if (note == 11) {
				freq = C0 * 15.0f / 8.0f;
			} else {
				freq = C0 * 2.0f;
			}
			return (float) (Math.pow(2, key / 12.0f + octaveAdjusted + (detune + bend))) * freq;
		} else if (tuning == TUNING_BAD) {
			int octaveAdjusted = octave - 3;
			while (note < 0) {
				note += 12;
				octaveAdjusted -= 1;
			}
			while (note >= 12) {
				note -= 12;
				octaveAdjusted += 1;
			}
			float freq;
			if (note == 0) {
				freq = C0;
			} else if (note == 1) {
				freq = C0 * 16.0f / 15.0f;
			} else if (note == 2) {
				freq = C0 * 90.0f / 81.0f;
			} else if (note == 3) {
				freq = C0 * 60.0f / 51.0f;
			} else if (note == 4) {
				freq = C0 * 50.0f / 41.0f;
			} else if (note == 5) {
				freq = C0 * 40.0f / 31.0f;
			} else if (note == 6) {
				freq = C0 * 70.0f / 51.0f;
			} else if (note == 7) {
				freq = C0 * 30.0f / 21.0f;
			} else if (note == 8) {
				freq = C0 * 80.0f / 51.0f;
			} else if (note == 9) {
				freq = C0 * 50.0f / 31.0f;
			} else if (note == 10) {
				freq = C0 * 70.0f / 41.0f;
			} else if (note == 11) {
				freq = C0 * 15.0f / 8.0f;
			} else {
				freq = C0 * 2.0f;
			}
			return (float) (Math.pow(2, key / 12.0f + octaveAdjusted + (detune + bend))) * freq;
		}
		return 0.0f;
	}

	public int getRequiredVoices() {
		return voices > 1 ? voices : 1;
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		//prepreLevel = new float[voices];
		preLevel = new float[voices];
		pressed = new boolean[voices];
		pressedVelocity = new float[voices];
		justReleased = new boolean[voices];
		pressure = new float[voices];
		smoothPressure = new float[voices];
		damped = new boolean[voices];
		noteForVoice = new int[voices];
		lastNote = new int[voices];
		voicePlaying = new boolean[voices];
		if (portamentoCC == null) {
			portamentoCC = new CC();
		}
		if (voicesCC == null) {
			voicesCC = new CC();
			voicesCC.setRangeLimits(0, voices + 1);
		}
		if (tuningCC == null) {
			tuningCC = new CC();
			tuningCC.setRangeLimits(0, 2);
		}
		if (octaveCC == null) {
			octaveCC = new CC();
			octaveCC.setRangeLimits(0, 9);
		}
		if (sustainCC == null) {
			sustainCC = new CC();
			sustainCC.setRangeLimits(0, 1);
			sustainCC.cc = 64; //Use default sustain CC
		}
		noteVelocities = new float[256];
		
		  // replaced legato with zero voices
		if (!legato && voices == 1) { 
			voices = 0;
		} else if (legato && voices == 0) {
			legato = false;
		}
	}

	private static final double PORTAMENTO_RATE = 250.0 / Instrument.ENVELOPE_RATE;

	@Override
	public void doEnvelope(int voice) {
		smoothPressure[voice] = 0.9f * smoothPressure[voice] + 0.1f * pressure[voice];
		if (pressed[voice] || damped[voice]) {
			if (justReleased[voice]) {
				output2.value[voice] = 0.0f;  // need at least one envelope cycle at zero to gate 
				justReleased[voice] = false;
				return;
			} else {
				if (pressure[voice] != pressedVelocity[voice]) {
					output2.value[voice] = smoothPressure[voice];
				} else {
					output2.value[voice] = pressedVelocity[voice];
				}
			}
			//preLevel[voice] = prepreLevel[voice]; // so voice pitch changes when it is first triggered
		} else {
			output2.value[voice] = 0.0f;
		}
		justReleased[voice] = false;
		if (portamento < 0.01 || (voices == 1 && !sliding)) {
			output1.value[voice] = preLevel[voice] + bend * 2.0f / 100.0f; // bend by 2 semitones up and down
		} else {
			float p = (float) Math.pow(portamento, PORTAMENTO_RATE);
			output1.value[voice] = output1.value[voice] * p + preLevel[voice] * (1.0f - p) + bend * 2.0f / 100.0f; // bend by 2 semitones up and down
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
		if (portamentoCC.cc == cc) {
			double v = portamentoCC.range(value);
			portamento = Math.pow(v, 0.1);
		}
		if (voicesCC.cc == cc) {
			double v = voicesCC.range(value);
			voices = Math.min((int)v, getRequiredVoices());
		}
		if (tuningCC.cc == cc) {
			double v = tuningCC.range(value);
			tuning = (int)v;
		}
		if (octaveCC.cc == cc) {
			double v = octaveCC.range(value);
			octave = (int)v;
		}
		if (sustainCC.cc == cc) {
			setSustaining(value > 0.5);
		}
	}

	public boolean isSounding() {
		int playingVoices = voices == 0 ? 1 : voices;
		for (int i = 0; i < playingVoices; i++) {
			if (pressed[i]) {
				return true;
			}
		}
		return false;
	}

}
