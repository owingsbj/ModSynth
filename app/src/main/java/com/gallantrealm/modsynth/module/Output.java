package com.gallantrealm.modsynth.module;

public class Output extends Module {
	private static final long serialVersionUID = 1L;

	// public Scope.Type scopeType;
	public int scopeStyle;

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
		return 0;
	}

	@Override
	public String getInputName(int n) {
		if (n == 0) {
			return "In L";
		} else {
			return "In R";
		}
	}

	@Override
	public String getModName(int n) {
		return "Scope";
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		quietCycles = 0;
		limiter = 1.0f;
	}

	@Override
	public void doEnvelope(int voice) {
		if (voice == 0 && limiter < 1.0f) {
			limiter += 0.001f;
		}
	}

	public transient float fleft;
	public transient float fright;

	public static transient float limiter;

	@Override
	public final void doSynthesis(int startVoice, int endVoice) {
		if (input1 != null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				fleft += input1.value[voice];
			}
		}
		if (input2 != null) {
			for (int voice = startVoice; voice <= endVoice; voice++) {
				fright += input2.value[voice];
			}
		}
		float newleft = fleft * limiter;
		float newright = fright * limiter;
		if (newleft >= 0.9f || newright >= 0.9f || newleft <= -0.9f || newright <= -0.9f) {
			limiter -= 0.01f;
			newleft = fleft * limiter;
			newright = fright * limiter;
		}
		while (newleft >= 1.0f || newright >= 1.0f || newleft <= -1.0f || newright <= -1.0f) {
			limiter -= 0.01f;
			newleft = fleft * limiter;
			newright = fright * limiter;
		}
		fleft = newleft;
		fright = newright;
	}

	transient int quietCycles;

	public boolean isSounding() {
		double output = Math.abs(fleft + fright);
		if (output < 0.0001) {
			quietCycles += 1;
		} else {
			quietCycles = 0;
		}
		if (quietCycles > 100) {
			return false;
		}
		return true;
	}

}
