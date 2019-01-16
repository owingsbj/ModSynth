package com.gallantrealm.modsynth.module;

import java.io.Serializable;

/**
 * Used to maintain a MIDI control's id and range.
 */
public class CC implements Serializable {
	private static final long serialVersionUID = 1L;

	public int cc = 0;
	public int minRange = 0; // 0-100
	public int maxRange = 100; // 0-100

	// new range limit function fields
	public boolean rangeLimited;
	public int minLimit;
	public int maxLimit;

	public void setRangeLimits(int minLimit, int maxLimit) {
		this.minLimit = minLimit;
		this.maxLimit = maxLimit;
		if (!rangeLimited) {
			// migrate old range over
			minRange = minRange * (maxLimit - minLimit) / 100 + minLimit;
			maxRange = maxRange * (maxLimit - minLimit) / 100 + minLimit;
			rangeLimited = true;
		} else {
			// adjust range to fit new limits
			if (minRange < minLimit) {
				minRange = minLimit;
			}
			if (maxRange > maxLimit) {
				maxRange = maxLimit;
			}
		}
	}

	/**
	 * Takes a value and scales it for the range defined on this CC. min and max range are 0-100
	 * 
	 * @param value
	 *            a value from 0.0 to 1.0.
	 * @return
	 */
	public double range(double value) {
		if (rangeLimited) {
			int scale = maxRange - minRange;
			return value * scale + minRange;
		} else {
			double scale = (maxRange - minRange) / 100.0;
			return value * scale + minRange / 100.0;
		}
	}
}
