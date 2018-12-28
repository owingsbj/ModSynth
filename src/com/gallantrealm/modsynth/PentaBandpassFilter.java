package com.gallantrealm.modsynth;

/**
 * Five biquadratic bandpass filters, combined for performance
 */
public final class PentaBandpassFilter {

	double f1, f2, f3, f4, f5;
	double a1, a2, a3, a4, a5;
	double aa1, aa2, aa3, aa4, aa5;
	double aaa1, aaa2, aaa3, aaa4, aaa5;
	double b1, b2, b3, b4, b5;
//	double bb1, bb2, bb3, bb4, bb5;
	double bbb1, bbb2, bbb3, bbb4, bbb5;
	double xx, xxx;
	double y1, y2, y3, y4, y5;
	double yy1, yy2, yy3, yy4, yy5;
	double yyy1, yyy2, yyy3, yyy4, yyy5;
	double sample_rate, Q;

	public PentaBandpassFilter(double center_freq, double sample_rate, double Q) {
		configure(sample_rate, Q);
		f1 = center_freq;
		f2 = center_freq;
		f3 = center_freq;
		f4 = center_freq;
		f5 = center_freq;
	}

	public final void reset() {
		xx = xxx = 0;
		yy1 = yyy1 = 0;
		yy2 = yyy2 = 0;
		yy3 = yyy3 = 0;
		yy4 = yyy4 = 0;
		yy5 = yyy5 = 0;
	}

	public final void configure(double sample_rate, double Q) {
		reset();
		Q = (Q == 0) ? 1e-9 : Q;
		this.sample_rate = sample_rate;
		this.Q = Q;
		setFrequencies(f1, f2, f3, f4, f5);
	}

	// allow parameter change while running
	public final void setFrequencies(double cf1, double cf2, double cf3, double cf4, double cf5) {
		f1 = cf1;
		f2 = cf2;
		f3 = cf3;
		f4 = cf4;
		f5 = cf5;
		double omega1 = 2 * Math.PI * cf1 / sample_rate;
		double omega2 = 2 * Math.PI * cf2 / sample_rate;
		double omega3 = 2 * Math.PI * cf3 / sample_rate;
		double omega4 = 2 * Math.PI * cf4 / sample_rate;
		double omega5 = 2 * Math.PI * cf5 / sample_rate;
		double sn1 = Math.sin(omega1);
		double sn2 = Math.sin(omega2);
		double sn3 = Math.sin(omega3);
		double sn4 = Math.sin(omega4);
		double sn5 = Math.sin(omega5);
		double cs1 = Math.cos(omega1);
		double cs2 = Math.cos(omega2);
		double cs3 = Math.cos(omega3);
		double cs4 = Math.cos(omega4);
		double cs5 = Math.cos(omega5);
		double alpha1 = sn1 / (2 * Q);
		double alpha2 = sn2 / (2 * Q);
		double alpha3 = sn3 / (2 * Q);
		double alpha4 = sn4 / (2 * Q);
		double alpha5 = sn5 / (2 * Q);

		// Bandpass
		b1 = alpha1;
		b2 = alpha2;
		b3 = alpha3;
		b4 = alpha4;
		b5 = alpha5;
//		bb1 = 0;
//		bb2 = 0;
//		bb3 = 0;
//		bb4 = 0;
//		bb5 = 0;
		bbb1 = -alpha1;
		bbb2 = -alpha2;
		bbb3 = -alpha3;
		bbb4 = -alpha4;
		bbb5 = -alpha5;
		a1 = 1 + alpha1;
		a2 = 1 + alpha2;
		a3 = 1 + alpha3;
		a4 = 1 + alpha4;
		a5 = 1 + alpha5;
		aa1 = -2 * cs1;
		aa2 = -2 * cs2;
		aa3 = -2 * cs3;
		aa4 = -2 * cs4;
		aa5 = -2 * cs5;
		aaa1 = 1 - alpha1;
		aaa2 = 1 - alpha2;
		aaa3 = 1 - alpha3;
		aaa4 = 1 - alpha4;
		aaa5 = 1 - alpha5;

		// prescale filter constants
		b1 /= a1;
		b2 /= a2;
		b3 /= a3;
		b4 /= a4;
		b5 /= a5;
//		bb1 /= a1;
//		bb2 /= a2;
//		bb3 /= a3;
//		bb4 /= a4;
//		bb5 /= a5;
		bbb1 /= a1;
		bbb2 /= a2;
		bbb3 /= a3;
		bbb4 /= a4;
		bbb5 /= a5;
		aa1 /= a1;
		aa2 /= a2;
		aa3 /= a3;
		aa4 /= a4;
		aa5 /= a5;
		aaa1 /= a1;
		aaa2 /= a2;
		aaa3 /= a3;
		aaa4 /= a4;
		aaa5 /= a5;
	}

	// perform one filtering step
	public final double filter(double x, double l1, double l2, double l3, double l4, double l5) {
//		y1 = b1 * x + bb1 * xx + bbb1 * xxx - aa1 * yy1 - aaa1 * yyy1;
//		y2 = b2 * x + bb2 * xx + bbb2 * xxx - aa2 * yy2 - aaa2 * yyy2;
//		y3 = b3 * x + bb3 * xx + bbb3 * xxx - aa3 * yy3 - aaa3 * yyy3;
//		y4 = b4 * x + bb4 * xx + bbb4 * xxx - aa4 * yy4 - aaa4 * yyy4;
//		y5 = b5 * x + bb5 * xx + bbb5 * xxx - aa5 * yy5 - aaa5 * yyy5;
		y1 = b1 * x + bbb1 * xxx - aa1 * yy1 - aaa1 * yyy1;
		y2 = b2 * x + bbb2 * xxx - aa2 * yy2 - aaa2 * yyy2;
		y3 = b3 * x + bbb3 * xxx - aa3 * yy3 - aaa3 * yyy3;
		y4 = b4 * x + bbb4 * xxx - aa4 * yy4 - aaa4 * yyy4;
		y5 = b5 * x + bbb5 * xxx - aa5 * yy5 - aaa5 * yyy5;
		xxx = xx;
		xx = x;
		yyy1 = yy1;
		yyy2 = yy2;
		yyy3 = yy3;
		yyy4 = yy4;
		yyy5 = yy5;
		yy1 = y1;
		yy2 = y2;
		yy3 = y3;
		yy4 = y4;
		yy5 = y5;
		return l1 * y1 + l2 * y2 + l3 * y3 + l4 * y4 + l5 * y5;
	}
}