package com.gallantrealm.modsynth;

public final class FastMath {

	public static final float PI = 3.1415927f;

	public static final float max(float a, float b) {
		if (a > b) {
			return a;
		}
		return b;
	}

	public static final float min(float a, float b) {
		if (a < b) {
			return a;
		}
		return b;
	}

	public static final float range(float x, float min, float max) {
		if (x < min) {
			return min;
		}
		if (x > max) {
			return max;
		}
		return x;
	}

	public static final float abs(float a) {
		if (a < 0) {
			return -a;
		}
		return a;
	}

	public static final float sign(float a) {
		if (a > 0) {
			return 1;
		} else if (a < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	public static final float avg(float a, float b) {
		return (a + b) / 2.0f;
	}

	public static final float avg(float a, float b, float c) {
		return (a + b + c) / 3.0f;
	}

	public static final float avg(float a, float b, float c, float d) {
		return (a + b + c + d) / 4.0f;
	}

	public static final float toRadians(float a) {
		return a * TORADIAN;
	}

	public static final float toDegrees(float a) {
		return a * TODEGREES;
	}

	public static final float TORADIAN = (float) Math.PI / 180.0F;
	public static final float TODEGREES = (float) (180.0f / Math.PI);

	public static final int SIN_BITS = 14;
	public static final int SIN_MASK = ~(-1 << SIN_BITS);
	public static final int SIN_COUNT = SIN_MASK + 1;

	public static final float radFull = (float) (Math.PI * 2.0);
	public static final float degFull = (float) (360.0);
	public static final float radToIndex = SIN_COUNT / radFull;
	public static final float degToIndex = SIN_COUNT / degFull;

	public static final float[] sin = new float[SIN_COUNT];
	public static final float[] cos = new float[SIN_COUNT];

	static {
		for (int i = 0; i < SIN_COUNT; i++) {
			sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
			cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
		}
	}

	/**
	 * SIN / COS (RAD)
	 */

	public static final float sin(float rad) {
		return sin[(int) (rad * radToIndex) & SIN_MASK];
	}

	public static final float cos(float rad) {
		return cos[(int) (rad * radToIndex) & SIN_MASK];
	}

	/**
	 * SIN / COS (DEG)
	 */

	public static final float sinDeg(float deg) {
		return sin[(int) (deg * degToIndex) & SIN_MASK];
	}

	public static final float cosDeg(float deg) {
		return cos[(int) (deg * degToIndex) & SIN_MASK];
	}

	/**
	 * SIN / COS (DEG - STRICT)
	 */

	public static final float sinDegStrict(float deg) {
		return (float) Math.sin(deg * TORADIAN);
	}

	public static final float cosDegStrict(float deg) {
		return (float) Math.cos(deg * TORADIAN);
	}

	public static final int SIZE = 1024;
	public static final float STRETCH = (float) Math.PI;
	// Output will swing from -STRETCH to STRETCH (default: Math.PI)
	// Useful to change to 1 if you would normally do "atan2(y, x) / Math.PI"

	// Inverse of SIZE
	public static final int EZIS = -SIZE;
	public static final float[] ATAN2_TABLE_PPY = new float[SIZE + 1];
	public static final float[] ATAN2_TABLE_PPX = new float[SIZE + 1];
	public static final float[] ATAN2_TABLE_PNY = new float[SIZE + 1];
	public static final float[] ATAN2_TABLE_PNX = new float[SIZE + 1];
	public static final float[] ATAN2_TABLE_NPY = new float[SIZE + 1];
	public static final float[] ATAN2_TABLE_NPX = new float[SIZE + 1];
	public static final float[] ATAN2_TABLE_NNY = new float[SIZE + 1];
	public static final float[] ATAN2_TABLE_NNX = new float[SIZE + 1];

	static {
		for (int i = 0; i <= SIZE; i++) {
			float f = (float) i / SIZE;
			ATAN2_TABLE_PPY[i] = (float) (StrictMath.atan(f) * STRETCH / StrictMath.PI);
			ATAN2_TABLE_PPX[i] = STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
			ATAN2_TABLE_PNY[i] = -ATAN2_TABLE_PPY[i];
			ATAN2_TABLE_PNX[i] = ATAN2_TABLE_PPY[i] - STRETCH * 0.5f;
			ATAN2_TABLE_NPY[i] = STRETCH - ATAN2_TABLE_PPY[i];
			ATAN2_TABLE_NPX[i] = ATAN2_TABLE_PPY[i] + STRETCH * 0.5f;
			ATAN2_TABLE_NNY[i] = ATAN2_TABLE_PPY[i] - STRETCH;
			ATAN2_TABLE_NNX[i] = -STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
		}
	}

	/**
	 * ATAN2
	 */

	public static final float atan2(float y, float x) {
		if (x >= 0) {
			if (y >= 0) {
				if (x >= y)
					return ATAN2_TABLE_PPY[(int) (SIZE * y / x + 0.5)];
				else
					return ATAN2_TABLE_PPX[(int) (SIZE * x / y + 0.5)];
			} else {
				if (x >= -y)
					return ATAN2_TABLE_PNY[(int) (EZIS * y / x + 0.5)];
				else
					return ATAN2_TABLE_PNX[(int) (EZIS * x / y + 0.5)];
			}
		} else {
			if (y >= 0) {
				if (-x >= y)
					return ATAN2_TABLE_NPY[(int) (EZIS * y / x + 0.5)];
				else
					return ATAN2_TABLE_NPX[(int) (EZIS * x / y + 0.5)];
			} else {
				if (x <= y) // (-x >= -y)
					return ATAN2_TABLE_NNY[(int) (SIZE * y / x + 0.5)];
				else
					return ATAN2_TABLE_NNX[(int) (SIZE * x / y + 0.5)];
			}
		}
	}

	public static final float random() {
		return (float) Math.random();
	}

	public static final float random(float start, float end) {
		return ((float) Math.random() * (end - start) + start);
	}

	public static final float random(float start, float end, float increment) {
		return (int) ((((float) Math.random()) * (end - start) + start) / increment) * increment;
	}

// Note: Math.sqrt is faster
//	public static final float sqrt(float a) {
//		final long x = Double.doubleToLongBits(a) >> 32;
//		double y = Double.longBitsToDouble((x + 1072632448) << 31);
//
//		// repeat the following line for more precision
//		y = (y + a / y) * 0.5;
//		return (float) y;
//	}

}
