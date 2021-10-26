package com.gallantrealm.modsynth;

import java.io.Serializable;

// Note:  This class is needed to resolve deserialization issues on Android KitKat.
public class Scope implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        Oscilloscope, Flower // , Waterfall
    };
}
