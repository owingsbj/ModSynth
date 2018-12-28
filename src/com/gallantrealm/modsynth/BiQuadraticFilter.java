package com.gallantrealm.modsynth;

import com.gallantrealm.android.Translator;

// http://en.wikipedia.org/wiki/Digital_biquad_filter

public final class BiQuadraticFilter {

	public enum Type {
		LOWPASS, HIGHPASS, BANDPASS, NOTCH;
		public String toString() {
			String string;
			if (this == HIGHPASS) {
				string = "High Pass";
			} else if (this == BANDPASS) {
				string = "Band Pass";
			} else if (this == NOTCH) {
				string = "Notch";
			} else {
				string = "Low Pass";
			}
			return Translator.getTranslator().translate(string);
		}
	};

	double a0, a1, a2, b0, b1, b2;
	double x1, x2, y, y1, y2;
	double gain_abs;
	Type type;
	double center_freq, sample_rate, Q, gainDB;

	public BiQuadraticFilter() {
	}

	public BiQuadraticFilter(Type type, double center_freq, double sample_rate, double Q, double gainDB) {
		configure(type, center_freq, sample_rate, Q, gainDB);
	}

	// constructor without gain setting
	public BiQuadraticFilter(Type type, double center_freq, double sample_rate, double Q) {
		configure(type, center_freq, sample_rate, Q, 0);
	}

	public final void reset() {
		x1 = x2 = y1 = y2 = 0;
	}

	public final double getFrequency() {
		return center_freq;
	}

	public final double getQ() {
		return Q;
	}
	
	public final void setQ(double Q) {
		this.Q = (Q == 0) ? 1e-9 : Q;
	}

	public final void configure(Type type, double center_freq, double sample_rate, double Q, double gainDB) {
		reset();
		Q = (Q == 0) ? 1e-9 : Q;
		this.type = type;
		this.sample_rate = sample_rate;
		this.Q = Q;
		this.gainDB = gainDB;
		setFrequency(center_freq);
	}

	public final void configure(Type type, double center_freq, double sample_rate, double Q) {
		configure(type, center_freq, sample_rate, Q, 0);
	}

	// allow parameter change while running
	public final void setFrequency(double cf) {
		center_freq = cf;
//		double gain_abs = Math.pow(10, gainDB / 40);   // only used for peaking and shelving filters
		double omega = 2 * Math.PI * cf / sample_rate;
		double sn = Math.sin(omega);
		double cs = Math.cos(omega);
		double alpha = sn / (2 * Q);
//		double beta = Math.sqrt(gain_abs + gain_abs);
		switch (type) {
		case BANDPASS:
			b0 = alpha;
			b1 = 0;
			b2 = -alpha;
			a0 = 1 + alpha;
			a1 = -2 * cs;
			a2 = 1 - alpha;
			break;
		case LOWPASS:
			b0 = (1 - cs) / 2;
			b1 = 1 - cs;
			b2 = (1 - cs) / 2;
			a0 = 1 + alpha;
			a1 = -2 * cs;
			a2 = 1 - alpha;
			break;
		case HIGHPASS:
			b0 = (1 + cs) / 2;
			b1 = -(1 + cs);
			b2 = (1 + cs) / 2;
			a0 = 1 + alpha;
			a1 = -2 * cs;
			a2 = 1 - alpha;
			break;
		case NOTCH:
			b0 = 1;
			b1 = -2 * cs;
			b2 = 1;
			a0 = 1 + alpha;
			a1 = -2 * cs;
			a2 = 1 - alpha;
			break;
//		case PEAK:
//			b0 = 1 + (alpha * gain_abs);
//			b1 = -2 * cs;
//			b2 = 1 - (alpha * gain_abs);
//			a0 = 1 + (alpha / gain_abs);
//			a1 = -2 * cs;
//			a2 = 1 - (alpha / gain_abs);
//			break;
//		case LOWSHELF:
//			b0 = gain_abs * ((gain_abs + 1) - (gain_abs - 1) * cs + beta * sn);
//			b1 = 2 * gain_abs * ((gain_abs - 1) - (gain_abs + 1) * cs);
//			b2 = gain_abs * ((gain_abs + 1) - (gain_abs - 1) * cs - beta * sn);
//			a0 = (gain_abs + 1) + (gain_abs - 1) * cs + beta * sn;
//			a1 = -2 * ((gain_abs - 1) + (gain_abs + 1) * cs);
//			a2 = (gain_abs + 1) + (gain_abs - 1) * cs - beta * sn;
//			break;
//		case HIGHSHELF:
//			b0 = gain_abs * ((gain_abs + 1) + (gain_abs - 1) * cs + beta * sn);
//			b1 = -2 * gain_abs * ((gain_abs - 1) + (gain_abs + 1) * cs);
//			b2 = gain_abs * ((gain_abs + 1) + (gain_abs - 1) * cs - beta * sn);
//			a0 = (gain_abs + 1) - (gain_abs - 1) * cs + beta * sn;
//			a1 = 2 * ((gain_abs - 1) - (gain_abs + 1) * cs);
//			a2 = (gain_abs + 1) - (gain_abs - 1) * cs - beta * sn;
//			break;
		}
		// prescale filter constants
		b0 /= a0;
		b1 /= a0;
		b2 /= a0;
		a1 /= a0;
		a2 /= a0;
		
		double p1h = b0 + b1 + b2;
		double p2h = 1.0 + a1 + a2;
		p1 = p1h * p1h;
		p2 = p2h * p2h;
		bb = (b0 * b1 + 4.0 * b0 * b2 + b1 * b2);
		aa = (a1 + 4.0 * a2 + a1 * a2);
	}
	
	double p1, p2, bb, aa;

	// provide a static amplitude result for testing
	public final double result(double f) {
		double ph = Math.sin(Math.PI * f / sample_rate);
		double phi = ph * ph;
		double phisquared = phi * phi;
		return (p1 - 4.0 * bb * phi + 16.0 * b0 * b2 * phisquared) / (p2 - 4.0 * aa * phi + 16.0 * a2 * phisquared);
	}

	// provide a static decibel result for testing
	public final double log_result(double f) {
		double r;
		try {
			r = 10 * Math.log10(result(f));
		} catch (Exception e) {
			r = -100;
		}
		if (Double.isInfinite(r) || Double.isNaN(r)) {
			r = -100;
		}
		return r;
	}

	// return the constant set for this filter
	public final double[] constants() {
		return new double[] { b0, b1, b2, a1, a2 };
	}

	// perform one filtering step
	public final double filter(double x) {
		y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
		x2 = x1;
		x1 = x;
		y2 = y1;
		y1 = y;
		return (y);
	}
}