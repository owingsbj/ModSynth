package com.gallantrealm.modsynth.module;

import com.gallantrealm.modsynth.Stringifier;

public class Amp extends Module {
	private static final long serialVersionUID = 1L;

	public double overdrive = 0.0;
	public double volume = 1.0;
	public double tone = Math.sqrt(0.5);
	public double modulation = 1.0; // unused
	public CC volumeCC;
	public CC toneCC;
	public CC overdriveCC;
	public boolean distortion;
	public CC distortionCC;

	private transient float tone2;
	private transient float[] smoothAmp;
	private transient float[] lastFilteredValue;

	@Override
	public void stringify(Stringifier s) {
		super.stringify(s);
		s.add("overdrive", overdrive);
		s.add("volume", volume);
		s.add("tone", tone);
	}

	@Override
	public int getInputCount() {
		return 1;
	}

	@Override
	public int getModCount() {
		return 1;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}

	@Override
	public String getInputName(int n) {
		return "In";
	}

	@Override
	public String getModName(int n) {
		return "Gain";
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		lastFilteredValue = new float[voices];
		smoothAmp = new float[voices];
		if (volumeCC == null) {
			volumeCC = new CC();
		}
		if (toneCC == null) {
			toneCC = new CC();
		}
		if (overdriveCC == null) {
			overdriveCC = new CC();
		}
		if (distortionCC == null) {
			distortionCC = new CC();
		}
		distortionCC.setRangeLimits(0, 1);
	}

	private transient float fvolume;

	@Override
	public void doEnvelope(int voice) {
		tone2 = Math.min((float) Math.sqrt(tone), 0.99f);
		fvolume = (float) (volume * (overdrive + 1.0));
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		if (input1 == null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				output1.value[voice] = 0.0f;
			}
		} else if (mod1 == null) {
			if (tone2 > 0 && !distortion) {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float value = fvolume * input1.value[voice];
					value = lastFilteredValue[voice] * tone2 + value * (1.0f - tone2);
					lastFilteredValue[voice] = value;
					output1.value[voice] = value;
				}
			} else {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float value = fvolume * input1.value[voice];
					output1.value[voice] = value;
				}
			}
		} else {
			if (tone2 > 0 && !distortion) {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float amp = 0.9f * smoothAmp[voice] + 0.1f * mod1.value[voice];
					smoothAmp[voice] = amp;
					float value = fvolume * input1.value[voice] * amp;
					value = lastFilteredValue[voice] * tone2 + value * (1.0f - tone2);
					lastFilteredValue[voice] = value;
					output1.value[voice] = value;
				}
			} else {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float amp = 0.9f * smoothAmp[voice] + 0.1f * mod1.value[voice];
					smoothAmp[voice] = amp;
					float value = fvolume * input1.value[voice] * amp;
					output1.value[voice] = value;
				}
			}
		}
		if (distortion) {
			float v = 0;
			for (int voice = startVoice; voice <= endVoice; voice++) {
				v += output1.value[voice];
				output1.value[voice] = 0;
			}
			if (tone2 > 0) {
				v = lastFilteredValue[0] * tone2 + v * (1.0f - tone2);
				lastFilteredValue[0] = v;
			}
			if (v > 0.5) {
				output1.value[0] = v / (v + 0.5f);
			} else if (v < -0.5) {
				v = -v;
				v = v / (v + 0.5f);
				output1.value[0] = -v;
			} else {
				output1.value[0] = v;
			}
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (volumeCC.cc == cc) {
			volume = volumeCC.range(value);
		}
		if (toneCC.cc == cc) {
			tone = 1.0 - Math.sqrt(toneCC.range(value));
		}
		if (overdriveCC.cc == cc) {
			overdrive = 10.0 * overdriveCC.range(value);
		}
		if (distortionCC.cc == cc) {
			distortion = distortionCC.range(value) < 0.5 ? false : true;
		}
	}

}
