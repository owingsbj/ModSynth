package com.gallantrealm.modsynth.module;

import com.gallantrealm.android.Translator;

public class Mixer extends Module {
	private static final long serialVersionUID = 1L;

	public enum MixFunction {
		ADD, SUBTRACT, MULTIPLY, MAX, MIN;
		public String toString() {
			String string;
			if (this == SUBTRACT) {
				string = "Subtract";
			} else if (this == MULTIPLY) {
				string = "Multiply";
			} else if (this == MAX) {
				string = "Max";
			} else if (this == MIN) {
				string = "Min";
			} else {
				string = "Add";
			}
			return Translator.getTranslator().translate(string);
		}
	};

	public double balance = 0.5;
	public double modulation = 0.5;
	public MixFunction mixFunction = MixFunction.ADD;
	public double bias = 0.0;
	public CC balanceCC;
	public CC biasCC;
	public CC modulationCC;
	public boolean usingLevels = true;
	public double level1 = 1.0;
	public double level2 = 1.0;
	public CC level1CC;
	public CC level2CC;
	public boolean usingThreeInputs = true;
	public double level3 = 1.0;
	public CC level3CC;
	public CC mixFunctionCC;

	@Override
	public int getInputCount() {
		return 3;
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
		return "In " + (n + 1);
	}

	@Override
	public String getModName(int n) {
		return "Balance";
	}

	@Override
	public String getOutputName(int n) {
		return "Out";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		if (!usingLevels) {
			if (input1 != null) {
				level1 = 1.0 - balance;
			}
			if (input2 != null) {
				level2 = balance;
			}
			usingLevels = true;
		}
		if (level1CC == null) {
			level1CC = new CC();
		}
		if (level2CC == null) {
			level2CC = new CC();
		}
		if (level3CC == null) {
			level3CC = new CC();
		}
		if (biasCC == null) {
			biasCC = new CC();
		}
		if (modulationCC == null) {
			modulationCC = new CC();
		}
		if (!usingThreeInputs) {
			input3 = input2;
			input2 = null;
			level3 = level2;
			level2 = 0.5;
			level3CC = level2CC;
			level2CC = new CC();
			usingThreeInputs = true;
		}
		if (mixFunctionCC == null) {
			mixFunctionCC = new CC();
			mixFunctionCC.setRangeLimits(0, MixFunction.values().length - 1);
		}
		in1 = zeros;
		in2 = zeros;
		in3 = zeros;
	}

	private transient float flevel1, flevel2, flevel3, fbias;
	private transient float[] in1, in2, in3;

	@Override
	public void doEnvelope(int voice) {
		if (voice == 0) {
			if (mixFunction == MixFunction.MULTIPLY) {
				in1 = (input1 != null && input1.value != null) ? input1.value : ones;
				in2 = (input2 != null && input2.value != null) ? input2.value : ones;
				in3 = (input3 != null && input3.value != null) ? input3.value : ones;
				flevel1 = (input1 != null && input1.value != null) ? (float) level1 : 1.0f;
				flevel2 = (input2 != null && input2.value != null) ? (float) level2 : 1.0f;
				flevel3 = (input3 != null && input3.value != null) ? (float) level3 : 1.0f;
			} else if (mixFunction == MixFunction.MAX) {
				in1 = (input1 != null && input1.value != null) ? input1.value : small;
				in2 = (input2 != null && input2.value != null) ? input2.value : small;
				in3 = (input3 != null && input3.value != null) ? input3.value : small;
				flevel1 = (input1 != null && input1.value != null) ? (float) level1 : 1.0f;
				flevel2 = (input2 != null && input2.value != null) ? (float) level2 : 1.0f;
				flevel3 = (input3 != null && input3.value != null) ? (float) level3 : 1.0f;
			} else if (mixFunction == MixFunction.MIN) {
				in1 = (input1 != null && input1.value != null) ? input1.value : big;
				in2 = (input2 != null && input2.value != null) ? input2.value : big;
				in3 = (input3 != null && input3.value != null) ? input3.value : big;
				flevel1 = (input1 != null && input1.value != null) ? (float) level1 : 1.0f;
				flevel2 = (input2 != null && input2.value != null) ? (float) level2 : 1.0f;
				flevel3 = (input3 != null && input3.value != null) ? (float) level3 : 1.0f;
			} else {
				in1 = (input1 != null && input1.value != null) ? input1.value : zeros;
				in2 = (input2 != null && input2.value != null) ? input2.value : zeros;
				in3 = (input3 != null && input3.value != null) ? input3.value : zeros;
				flevel1 = (input1 != null && input1.value != null) ? (float) level1 : 0.0f;
				flevel2 = (input2 != null && input2.value != null) ? (float) level2 : 0.0f;
				flevel3 = (input3 != null && input3.value != null) ? (float) level3 : 0.0f;
			}
			fbias = (float) bias;
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		if (mod1 == null) { // the typical case
			if (mixFunction == MixFunction.ADD) {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float value1 = flevel1 * in1[voice];
					float value2 = flevel2 * in2[voice];
					float value3 = flevel3 * in3[voice] + fbias;
					output1.value[voice] = value1 + value2 + value3;
				}
			} else if (mixFunction == MixFunction.SUBTRACT) {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float value1 = flevel1 * in1[voice];
					float value2 = flevel2 * in2[voice];
					float value3 = flevel3 * in3[voice] + fbias;
					output1.value[voice] = value1 - value2 - value3;
				}
			} else if (mixFunction == MixFunction.MULTIPLY) {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float value1 = flevel1 * in1[voice];
					float value2 = flevel2 * in2[voice];
					float value3 = flevel3 * in3[voice] + fbias;
					output1.value[voice] = value1 * value2 * value3;
				}
			} else if (mixFunction == MixFunction.MAX) {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float value1 = flevel1 * in1[voice];
					float value2 = flevel2 * in2[voice];
					float value3 = flevel3 * in3[voice] + fbias;
					float v = ((value1 > value2) ? value1 : value2);
					output1.value[voice] = ((v > value3) ? v : value3);
				}
			} else if (mixFunction == MixFunction.MIN) {
				for (int voice = startVoice; voice <= endVoice; voice++) {
					float value1 = flevel1 * in1[voice];
					float value2 = flevel2 * in2[voice];
					float value3 = flevel3 * in3[voice] + fbias;
					float v = ((value1 < value2) ? value1 : value2);
					output1.value[voice] = ((v < value3) ? v : value3);
				}
			}
		} else { // modulated or other cases
			for (int voice = startVoice; voice <= endVoice; voice++) {
				float modulatedLevel1 = flevel1;
				float modulatedLevel2 = flevel2;
				float modulatedLevel3 = flevel3;
				if (mod1 != null) {
					modulatedLevel1 += modulation * mod1.value[voice];
					modulatedLevel3 -= modulation * mod1.value[voice];
					modulatedLevel2 = flevel2;
				}
				float value1 = in1[voice] * modulatedLevel1;
				float value2 = in2[voice] * modulatedLevel2;
				float value3 = in3[voice] * modulatedLevel3 + fbias;
				if (mixFunction == MixFunction.ADD) {
					output1.value[voice] = value1 + value2 + value3;
				} else if (mixFunction == MixFunction.SUBTRACT) {
					output1.value[voice] = value1 - value2 - value3;
				} else if (mixFunction == MixFunction.MULTIPLY) {
					output1.value[voice] = value1 * value2 * value3;
				} else if (mixFunction == MixFunction.MAX) {
					float v = ((value1 > value2) ? value1 : value2);
					output1.value[voice] = (v > value3) ? v : value3;
				} else if (mixFunction == MixFunction.MIN) {
					float v = ((value1 < value2) ? value1 : value2);
					output1.value[voice] = (v < value3) ? v : value3;
				}
			}
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (balanceCC != null && balanceCC.cc == cc) {
			balance = balanceCC.range(value);
		}
		if (level1CC != null && level1CC.cc == cc) {
			level1 = Math.pow(level1CC.range(value), 2);
		}
		if (level2CC != null && level2CC.cc == cc) {
			level2 = Math.pow(level2CC.range(value), 2);
		}
		if (level3CC != null && level3CC.cc == cc) {
			level3 = Math.pow(level3CC.range(value), 2);
		}
		if (biasCC != null && biasCC.cc == cc) {
			bias = biasCC.range(value);
		}
		if (modulationCC != null && modulationCC.cc == cc) {
			modulation = modulationCC.range(value);
		}
		if (mixFunctionCC != null && mixFunctionCC.cc == cc) {
			MixFunction newMixFunction = MixFunction.values()[(int) Math.round(mixFunctionCC.range(value))];
			if (!newMixFunction.equals(mixFunction)) {
				mixFunction = newMixFunction;
			}
		}
	}

}
