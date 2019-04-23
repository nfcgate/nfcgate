package de.tu_darmstadt.seemoo.nfcgate.xposed;


public class Native {
    static {
        Instance = new Native();
    }
    static final Native Instance;

    public native boolean isHookEnabled();
    public native void setConfiguration(byte[] config);
    public native void setPolling(boolean enabled);
}
