package de.tu_darmstadt.seemoo.nfcgate.xposed;

public class Native {
    static {
        Instance = new Native();
    }
    static final Native Instance;

    public native int installHooks();
    public native boolean isPatchEnabled();
    public native void setConfig(byte[] config);
    public native void resetConfig();
}
