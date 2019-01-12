package com.gallantrealm.modsynth.module;

public class Sequencer extends Module {
	private static final long serialVersionUID = 1L;

	public static final int MAXSTEPS = 16;

	public int[] sequence = new int[MAXSTEPS + 1]; // adding 1 because of index-out-of-bounds reports
	public boolean[] sequenceOn = new boolean[MAXSTEPS + 1];
	public boolean looping = true;
	public int activeSteps = 8;

	public boolean supportRetrigger = true;
	public boolean retrigger;
	public boolean stutter;
	public int voices;
	public boolean random;
	public int octave;
	
	public Sequencer() {
		for (int i = 0; i < MAXSTEPS; i++) {
			sequence[i] = 12;
			sequenceOn[i] = true;
		}
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
		} else if (n == 1) {
			return "Gate";
		} else {
			return "Chain";
		}
	}

	private transient int step;
	private transient float sampledPitch;
	private transient float sampledGate;
	private transient float currentPitch;
	private transient float currentGate;
	private transient float lastInputGate;
	private transient float lastStepTrigger;
	private transient int nextVoice;

	public int getRequiredVoices() {
		return voices;
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		if (sequenceOn == null) {
			sequenceOn = new boolean[MAXSTEPS + 1];
			for (int i = 0; i < MAXSTEPS; i++) {
				if (sequence[i] <= 0) {
					sequence[i] = 12;
				}
				if (i >= activeSteps || sequence[i] > 0) {
					sequenceOn[i] = true;
				}
			}
		}
	}

	@Override
	public void doEnvelope(int voice) {
		if (voices <= 0) {
			voices = 1;
		}
		if (voice > 0) {
			return;  // polyphonic input is ignored
		}
		float inputPitch;
		if (input1 != null) {
			inputPitch = input1.value[voice] + octave * 12 / (float) NOTES_PER_VOLT;
		} else {
			inputPitch = (3 + octave) * 12 / (float) NOTES_PER_VOLT;
			sampledPitch = inputPitch;
		}
		float inputGate = 1;
		if (input2 != null) {
			inputGate = input2.value[voice];
		}
		float inputStepTrigger = 0;
		if (mod1 != null) {
			inputStepTrigger = mod1.value[voice];
		}

		if ((lastInputGate <= 0 && inputGate > 0) || (lastInputGate > 0 && inputGate <= 0)) {
			sampledPitch = inputPitch;
			sampledGate = inputGate;
			if (retrigger) {
				if (inputGate > 0) { // note pressed
					step = -1; // to retrigger start of sequence
				}
			} else if (stutter) {
				if (inputGate > 0) {
					step--;
				}
			}
		}

		if (inputStepTrigger > 0 && (looping || step < activeSteps)) {
			if (step > activeSteps) {
				try {
					step %= activeSteps;
				} catch (ArithmeticException e) {
					step = 0;
				}
			}

			if (lastStepTrigger <= 0) {

				// move to the next note
				if (random) { // random
					step = (int) (Math.random() * activeSteps);
				} else {
					step += 1;
				}
				output2.value[nextVoice] = 0;
				nextVoice += 1;
				nextVoice %= voices;
				if (looping || mod1 == null) {
					try {
						step %= activeSteps;
					} catch (ArithmeticException e) {
						step = 0;
					}
				}
				// if (step == 0) {
				currentPitch = sampledPitch;
				currentGate = sampledGate;
				// }
				if (step < activeSteps && sequenceOn[step]) {
					output1.value[nextVoice] = currentPitch + (sequence[step] - 12.0f) / 100.0f;
					output2.value[nextVoice] = currentGate;
				} else {
					output2.value[nextVoice] = 0;
				}

				try {
					viewer.getClass().getMethod("setCurrentStep", Integer.class).invoke(viewer, step);
				} catch (Exception e) {
				}

			} else {

				// keep playing same note
//				output2.value[voice] = sampledGate[voice];
			}
		} else {

			// quiet
			output2.value[nextVoice] = 0.0f;

		}

		lastInputGate = inputGate;
		lastStepTrigger = inputStepTrigger;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
	}

	public boolean doesSynthesis() {
		return false;
	}

	@Override
	public boolean isSounding() {
		if (input2 == null) { // always sound if no gate input
			return true;
		}
		return false;
	}

}
