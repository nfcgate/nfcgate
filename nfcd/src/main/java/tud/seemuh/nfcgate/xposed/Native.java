package tud.seemuh.nfcgate.xposed;


public class Native {
    static {
        Instance = new Native();
    }
    public static Native Instance;
    public native void setEnabled(boolean enabled);
    public native boolean isEnabled();
    public native void uploadConfiguration(byte atqa, byte sak, byte[] hist, byte[] uid);
}
