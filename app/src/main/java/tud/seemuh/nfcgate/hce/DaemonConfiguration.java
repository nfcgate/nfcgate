package tud.seemuh.nfcgate.hce;

public class DaemonConfiguration {
    static {
        System.loadLibrary("nfcipc");
    }

    public native void enablePatch();
    public native void disablePatch();
    public native void uploadConfiguration(byte atqa, byte sak, byte hist, byte[] uid);
}
