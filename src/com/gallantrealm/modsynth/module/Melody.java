package com.gallantrealm.modsynth.module;

import java.util.ArrayList;
import com.gallantrealm.modsynth.Note;

public class Melody extends Module {
	private static final long serialVersionUID = 1L;

	public ArrayList<Note> notes = new ArrayList<Note>();
	public boolean looping = true;
	public boolean retrigger;
	public boolean stutter;
	public int voices;
	public boolean random;
	public int beatsPerMeasure = 8;

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
		return 2;
	}

	@Override
	public String getInputName(int n) {
		if (n == 0) {
			return "Pitch";
		} else {
			return "Gate";
		}
	}

	@Override
	public String getModName(int n) {
		return "Clock";
	}

	@Override
	public String getOutputName(int n) {
		if (n == 0) {
			return "Pitch";
		} else {
			return "Gate";
		}
	}

	private transient int step;
	private transient float sampledPitch;
	private transient float sampledGate;
	private transient float currentPitch;
	private transient float currentGate;
	private transient float lastInputGate;
	private transient float lastStepTrigger;
	private transient Note[] nextNotes;
	private transient int nnextNotes;
	private transient boolean[] nextVoicesUsed;
	private transient int nextVoice;
	private transient Note[] outNotes;
	private transient float[] outPitches;
	private transient float[] outLevels;

	public int getRequiredVoices() {
		return Math.max(1, voices);
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		if (beatsPerMeasure == 0) {
			beatsPerMeasure = 16;
		}
		nextNotes = new Note[100];
		nextVoicesUsed = new boolean[voices];
		outNotes = new Note[voices];
		outPitches = new float[voices];
		outLevels = new float[voices];
	}

	@Override
	public void doEnvelope(int voice) {
		if (voices <= 0) {
			voices = 1;
		}
		if (voice > 0) {
			return;
		}
		float inputPitch;
		if (input1 != null) {
			inputPitch = input1.value[voice];
		} else {
			inputPitch = 36 / (float) Module.NOTES_PER_VOLT; // C3
		}
		float inputGate = 1;
		if (input2 != null) {
			inputGate = input2.value[voice];
		}
		float inputStepTrigger = 0;
		if (mod1 != null) {
			inputStepTrigger = mod1.value[voice];
		}

		// Note: outPitches and outLevels delays change till the next envelope iteration
		// so voices are retriggered when they get stolen.
		for (int v = 0; v < voices; v++) {
			output1.value[v] = outPitches[v];
			output2.value[v] = (inputGate > 0.0f) ? outLevels[v] : 0.0f;
		}

		int maxDuration = getMaxDuration();
		if ((lastInputGate <= 0 && inputGate > 0) || (lastInputGate > 0 && inputGate <= 0)) {
			sampledPitch = inputPitch;
			sampledGate = inputGate;
			if (retrigger || step > maxDuration) {
				if (inputGate > 0) { // note pressed
					step = -1; // to retrigger start of sequence
					for (int v = 0; v < voices; v++) {
						outNotes[v] = null;
					}
				}
			} else if (stutter) {
				if (inputGate > 0) {
					step--;
				}
			}
		}

		if (inputStepTrigger > 0 && lastStepTrigger <= 0) {

			// move to the next step
			if (random) { // random
				step = (int) (Math.random() * maxDuration);
			} else {
				step += 1;
				if (looping || input2 == null) {
					try {
						step %= maxDuration;
					} catch (ArithmeticException e) {
						step = 0;
					}
				} else {
					step = Math.min(step, maxDuration + 1);
				}
			}
			if (viewer != null) {
				try {
					viewer.getClass().getMethod("setCurrentBeat", Integer.class).invoke(viewer, step);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			currentPitch = sampledPitch;
			currentGate = sampledGate;
			for (int v = 0; v < voices; v++) {
				nextVoicesUsed[v] = false;
			}

			nnextNotes = getNotesAt(step, nextNotes);
			for (int n = 0; n < nnextNotes; n++) {
				Note note = nextNotes[n];
				if (step < maxDuration && note != null) {
					boolean voiceFound = false;
					for (int v = 0; v < voices; v++) {
						if (note == outNotes[v]) {
							voiceFound = true;
							nextVoicesUsed[v] = true;
						}
					}
					if (!voiceFound) {
						nextVoice += 1;
						nextVoice %= voices;
						outNotes[nextVoice] = note;
						outPitches[nextVoice] = currentPitch + (note.pitch - 12.0f) / 100.0f;
						outLevels[nextVoice] = currentGate * note.velocity * note.velocity;
						nextVoicesUsed[nextVoice] = true;
						//System.out.println("Play voice " + nextVoice + " pitch " + outPitches[nextVoice] + " volume " + outLevels[nextVoice]);
						output2.value[nextVoice] = 0; // to cause retriggering
					}
				}
			}

			// close the gate on any unused voices
			for (int v = 0; v < voices; v++) {
				if (!nextVoicesUsed[v]) {
					outLevels[v] = 0;
					if (output2.value[v] != 0) {
						output2.value[v] = 0;
						//System.out.println("Stop voice " + v);
					}
				}
			}

		}

		lastInputGate = inputGate;
		lastStepTrigger = inputStepTrigger;
	}

	private int getNotesAt(int step, Note[] nextNotes) {
		int n = 0;
		for (Note note : notes) {
			if (note.start <= step && note.start + note.duration > step) {
				nextNotes[n] = note;
				n += 1;
			}
		}
		return n;
	}

	private int getMaxDuration() {
		int max = 0;
		for (Note note : notes) {
			max = Math.max(max, note.start + note.duration);
		}
		return max;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
	}

	public boolean doesSynthesis() {
		return false;
	}

}
