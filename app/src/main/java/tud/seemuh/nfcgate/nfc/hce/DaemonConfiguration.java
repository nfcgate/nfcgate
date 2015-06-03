package tud.seemuh.nfcgate.nfc.hce;

import android.content.Context;
import android.content.Intent;

/**
 * Interface to the nfc daemon patches
 */
public class DaemonConfiguration {

    static DaemonConfiguration mInstance;
    private Context mContext;
    public static DaemonConfiguration getInstance() { return mInstance; }

    public static void Init(Context ctx) {
        mInstance = new DaemonConfiguration(ctx);
    }

    public DaemonConfiguration(Context ctx) {
        mContext = ctx;
    }

    public void enablePatch() {
        sendSimple("ENABLE");
    }

    public void disablePatch() {
        sendSimple("DISABLE");
    }

    public boolean isPatchEnabled() {
        return false;
    }

    public void uploadConfiguration(byte atqa, byte sak, byte[] hist, byte[] uid) {
        Intent intent = new Intent();
        intent.putExtra("action", "UPLOAD");
        intent.putExtra("atqa", atqa);
        intent.putExtra("sak", sak);
        intent.putExtra("hist", hist);
        intent.putExtra("uid", uid);
        send(intent);
    }

    private void sendSimple(String action) {
        Intent intent = new Intent();
        intent.putExtra("action", action);
        send(intent);
    }

    private void send(Intent intent) {
        intent.setAction("tud.seemuh.nfcgate.daemoncall");
        intent.putExtra("test", "test");
        mContext.sendBroadcast(intent);
    }

    public void requestPatchState() {
        sendSimple("REQSTATE");
    }
}
