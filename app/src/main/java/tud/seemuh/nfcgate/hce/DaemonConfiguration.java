package tud.seemuh.nfcgate.hce;

public class DaemonConfiguration {
    static {
        System.loadLibrary("nfcipc");
        mInstance = new DaemonConfiguration();
    }
    static DaemonConfiguration mInstance;
    public static DaemonConfiguration getInstance() { return mInstance; }
    public native void enablePatch();
    public native void disablePatch();
    public native void uploadConfiguration(byte atqa, byte sak, byte hist, byte[] uid);
}
