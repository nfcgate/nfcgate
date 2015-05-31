package tud.seemuh.nfcgate.nfc.hce;

/**
 * Interface to the nfc daemon patches
 * The native part of this class connects to the unix domain socket ipc created the patch
 */
public class DaemonConfiguration {
    static {
        System.loadLibrary("nfcgate-ipc");
        mInstance = new DaemonConfiguration();
    }
    static DaemonConfiguration mInstance;
    public static DaemonConfiguration getInstance() { return mInstance; }
    public native void enablePatch();
    public native void disablePatch();
    public native boolean isPatchEnabled();
    public native void uploadConfiguration(byte atqa, byte sak, byte[] hist, byte[] uid);
}
