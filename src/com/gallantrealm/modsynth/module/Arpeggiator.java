package com.gallantrealm.modsynth.module;

import com.gallantrealm.android.Translator;

public class Arpeggiator extends Module {
	private static final long serialVersionUID = 1L;

	public enum Type {
		UP, DOWN, UP_DOWN, DOWN_UP;
		public String toString() {
			String string;
			if (this == UP) {
				string = "Up";
			} else if (this == DOWN) {
				string = "Down";
			} else if (this == UP_DOWN) {
				string = "Up Down";
			} else if (this == DOWN_UP) {
				string = "Down Up";
			} else {
				string = "Up";
			}
			return Translator.getTranslator().translate(string);
		}
	};

	public Type type = Type.UP;
	public boolean looping = false;
	public boolean bypass;
	public CC typeCC;
	public CC loopingCC;
	public CC bypassCC;

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

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		notes = new Note[voices];
		for (int i = 0; i < voices; i++) {
			notes[i] = new Note();
		}
		if (typeCC == null) {
			typeCC = new CC();
			typeCC.setRangeLimits(0, Type.values().length -1);
		}
		if (loopingCC == null) {
			loopingCC = new CC();
			loopingCC.setRangeLimits(0, 1);
		}
		if (bypassCC == null) {
			bypassCC = new CC();
			bypassCC.setRangeLimits(0,1);
		}
	}

	private transient float lastTick;
	private transient int playingVoice;

	class Note {
		float pitch;
		float velocity;
	}

	private transient Note[] notes;
	private transient int notesPressed;
	private boolean starting;
	private boolean playingUp;

	@Override
	public void doEnvelope(int voice) {
		if (input1 == null || input2 == null || mod1 == null) {
			return; // no need to process if not hooked up
		}
		if (bypass) {
			output1.value[voice] = input1.value[voice];
			output2.value[voice] = input2.value[voice];
			return;
		}
		if (voice > 0) {
			return; // only need to process for one voice
		}
		float tick = mod1.value[0];
		if (lastTick <= 0.0 && tick > 0.0) {

			// shut off all currently playing notes
			for (int i = 0; i < notes.length; i++) {
				output2.value[i] = 0.0f;
			}

			// scan through and find all playing voices
			notesPressed = 0;
			for (int i = 0; i < notes.length; i++) {
				if (input2.value[i] > 0) {
					notes[notesPressed].pitch = input1.value[i];
					notes[notesPressed].velocity = input2.value[i];
					notesPressed += 1;
				}
			}

			// order the notes
			boolean sorted = false;
			while (!sorted) {
				sorted = true;
				for (int i = 0; i < notesPressed - 1; i++) {
					if (notes[i].pitch > notes[i + 1].pitch) {
						sorted = false;
						Note tnote = notes[i];
						notes[i] = notes[i + 1];
						notes[i + 1] = tnote;
					}
				}
			}

			// play the appropriate note
			if (starting) {
				if (type == Type.UP || type == Type.UP_DOWN) {
					playingVoice = 0;
					playingUp = true;
				} else {
					playingVoice = Math.max(0, notesPressed - 1);
					playingUp = false;
				}
				starting = false;
			} else {
				if (playingUp) {
					playingVoice = playingVoice + 1;
				} else {
					playingVoice = playingVoice - 1;
				}
				if (playingVoice >= notesPressed) {
					if (type == Type.UP) {
						if (looping) {
							playingVoice = 0;
						}
					} else {
						if (looping || type == Type.UP_DOWN) {
							playingVoice = Math.max(0, notesPressed - 2);
							playingUp = false;
						}
					}
				} else if (playingVoice < 0) {
					if (type == Type.DOWN) {
						if (looping) {
							playingVoice = Math.max(0, notesPressed - 1);
						}
					} else {
						if (looping || type == Type.DOWN_UP) {
							playingVoice = Math.max(0, Math.min(notesPressed - 1, 1));
							playingUp = true;
						}
					}
				}
			}
			if (playingVoice >= 0 && playingVoice < notesPressed) {
				output1.value[playingVoice] = notes[playingVoice].pitch;
				output2.value[playingVoice] = notes[playingVoice].velocity;
			} else {
				if (notesPressed == 0) {
					starting = true;
				}
			}
		}
		lastTick = tick;
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
	}
	
	public boolean doesSynthesis() {
		return false;
	}

	@Override
	public void updateCC(int cc, double value) {
		if (typeCC.cc == cc) {
			type = Type.values()[(int)Math.round(typeCC.range(value))];
		}
		if (loopingCC.cc == cc) {
			looping = loopingCC.range(value) < 0.5 ? false : true;
		}
		if (bypassCC.cc == cc) {
			bypass = bypassCC.range(value) < 0.5 ? false : true;
		}
	}
	
}
