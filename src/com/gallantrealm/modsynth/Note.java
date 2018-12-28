package com.gallantrealm.modsynth;

import java.io.Serializable;

public class Note implements Serializable {
	private static final long serialVersionUID = 1L;
	public int start; // in "beats"
	public int pitch;
	public float velocity;
	public int duration; // in "beats"
	public boolean continuous;
}
