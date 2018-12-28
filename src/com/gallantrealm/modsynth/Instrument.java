package com.gallantrealm.modsynth;

import java.io.Serializable;
import java.util.ArrayList;
import com.gallantrealm.modsynth.module.Arpeggiator;
import com.gallantrealm.modsynth.module.Compressor;
import com.gallantrealm.modsynth.module.Crusher;
import com.gallantrealm.modsynth.module.Function;
import com.gallantrealm.modsynth.module.Keyboard;
import com.gallantrealm.modsynth.module.Melody;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.modsynth.module.MultiOsc;
import com.gallantrealm.modsynth.module.Operator;
import com.gallantrealm.modsynth.module.Output;
import com.gallantrealm.modsynth.module.Pan;
import com.gallantrealm.modsynth.module.Reverb;
import com.gallantrealm.modsynth.module.SampleHold;
import com.gallantrealm.modsynth.module.SpectralFilter;
import com.gallantrealm.modsynth.module.Unison;

public class Instrument extends com.gallantrealm.mysynth.Instrument implements Stringifiable {
	private static final long serialVersionUID = 1L;

	public static final int MAX_VOICES = 10;
	public static final int ENVELOPE_RATE = 1000; // 1000 times per second  (note, if this is lowered, fast vibrato/tremelo sounds wrong)

	int mode; // UNUSED
	boolean chorusWidthSet; // UNUSED
	float chorusWidth; // UNUSED
	public ArrayList<Module> modules = new ArrayList<Module>();
	public Module selectedModule;

	public transient boolean initialized;
	public transient boolean editing;
	public transient boolean dirty;

	public void stringify(Stringifier s) {
		s.add("nmodules", modules.size());
		s.add("nvoices", getVoices());
		s.add("sampleRate", sampleRate);
		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			module.stringifyId = i;
		}
		for (Module module : modules) {
			s.add("module", module);
		}
	}

	private synchronized void orderModules() {
		ArrayList<Module> newModules = new ArrayList<Module>();
		Output speaker = getOutputModule();
		if (speaker != null) {
			for (Module module : modules) {
				module.predecessorCount = -1;
			}
			for (Module module : modules) {
				countPredecessors(module);
			}
			for (Module module : modules) {
				int position = 0;
				for (Module newModule : newModules) {
					if (module.predecessorCount > newModule.predecessorCount) {
						position++;
					}
				}
				newModules.add(position, module);
			}
			modules = newModules;
			System.out.println("---- Sorted modules:");
			for (Module module : modules) {
				System.out.println(module.predecessorCount + " " + module.getClass().getSimpleName());
			}
			System.out.println("----");
		}
	}

	private int countPredecessors(Module module) {
		if (module.predecessorCount >= 0) {
			return module.predecessorCount;
		}
		module.predecessorCount = 0;
		if (module.input1 != null && module.input1.module != module) {
			module.predecessorCount += 1 + countPredecessors(module.input1.module);
		}
		if (module.mod1 != null && module.mod1.module != module) {
			module.predecessorCount += 1 + countPredecessors(module.mod1.module);
		}
		if (module.mod1 != null && module.mod1.module != module) {
			module.predecessorCount += 1 + countPredecessors(module.mod1.module);
		}
		if (module.mod2 != null && module.mod2.module != module) {
			module.predecessorCount += 1 + countPredecessors(module.mod2.module);
		}
		return module.predecessorCount;
	}

	public synchronized boolean isDirty() {
		if (dirty) {
			return true;
		}
		for (Module module : modules) {
			if (module.dirty) {
				return true;
			}
		}
		return false;
	}

	public synchronized void clearDirty() {
		dirty = false;
		for (Module module : modules) {
			if (module.viewer != null) {
				module.dirty = false;
			}
		}
	}

	public synchronized void deleteModule(Module removeModule) {
		// remove all links to the module
		for (Module module : modules) {
			if (module.input1 != null && module.input1.module == removeModule) {
				module.input1 = null;
			}
			if (module.input2 != null && module.input2.module == removeModule) {
				module.input2 = null;
			}
			if (module.input3 != null && module.input3.module == removeModule) {
				module.input3 = null;
			}
			if (module.mod1 != null && module.mod1.module == removeModule) {
				module.mod1 = null;
			}
			if (module.mod2 != null && module.mod2.module == removeModule) {
				module.mod2 = null;
			}
		}
		// remove the module
		modules.remove(removeModule);
		if (selectedModule == removeModule) {
			selectedModule = null;
		}
		dirty = true;
	}

	public synchronized Output getOutputModule() {
		for (Module module : modules) {
			if (module instanceof Output) {
				return (Output) module;
			}
		}
		return null;
	}

	public synchronized Keyboard getKeyboardModule() {
		for (Module module : modules) {
			if (module instanceof Keyboard) {
				return (Keyboard) module;
			}
		}
		return null;
	}

	public synchronized int getVoices() {
		int voices = 1;
		for (Module module : modules) {
			voices = Math.max(voices, module.getRequiredVoices());
		}
		return Math.min(MAX_VOICES, voices);
	}

	public synchronized void setEditing(boolean edit) {
		this.editing = edit;
		if (!editing) {
			initialized = false;
			initialize();
		}
	}
	
	public synchronized boolean isEditing() {
		return editing;
		
	}

	public String requiresUpgrade() {
		for (int m = 0; m < modules.size(); m++) {
			Module module = modules.get(m);
			if (module.requiresUpgrade()) {
				return module.getClass().getSimpleName();
			}
		}
		return null;
	}

	private transient int sampleRate;

	public synchronized void initialize(int sampleRate) {
		this.sampleRate = sampleRate;
		initialize();
	}

	private synchronized void initialize() {
		System.out.println(">>Instrument.initialize");
		if (!initialized) {
			orderModules();
			for (int m = 0; m < modules.size(); m++) {
				Module module = modules.get(m);
				module.initialize(MAX_VOICES, sampleRate); // use MAX_VOICES so modules don't have to be reinitialized if voicecount changes
			}
			initialized = true;
		}
		System.out.println("<<Instrument.initialize");
	}

	public synchronized void terminate() {
		System.out.println(">>Instrument.terminate");
		if (initialized) {
			for (int m = 0; m < modules.size(); m++) {
				Module module = modules.get(m);
				module.terminate();
			}
			initialized = false;
		}
		System.out.println("<<Instrument.terminate");
	}

	public synchronized void addModule(Module module) {
		modules.add(module);
		module.initialize(MAX_VOICES, sampleRate);
		dirty = true;
	}

	public boolean isSounding() {
		if (initialized) {
			for (int m = 0; m < modules.size(); m++) {
				if (modules.get(m).isSounding()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasAdvancedModules() {
		for (Module m : modules) {
			if (m instanceof Arpeggiator) {
				return true;
			}
			if (m instanceof Compressor) {
				return true;
			}
			if (m instanceof Crusher) {
				return true;
			}
			if (m instanceof Function) {
				return true;
			}
			if (m instanceof Melody) {
				return true;
			}
			if (m instanceof MultiOsc) {
				return true;
			}
			if (m instanceof Operator) {
				return true;
			}
			if (m instanceof Pan) {
				return true;
			}
			if (m.getClass().getSimpleName().equals("PCM")) {
				return true;
			}
			if (m instanceof Reverb) {
				return true;
			}
			if (m instanceof SampleHold) {
				return true;
			}
			if (m instanceof SpectralFilter) {
				return true;
			}
			if (m instanceof Unison) {
				return true;
			}
		}
		return false;
	}

	public void doEnvelopes() {
		if (initialized) {
			int moduleCount = modules.size();
			int voiceCount = getVoices();
			for (int m = 0; m < moduleCount; m++) {
				Module module = modules.get(m);
				for (int voice = 0; voice < voiceCount; voice++) {
					module.doEnvelope(voice);
				}
			}
		}
	}

}
