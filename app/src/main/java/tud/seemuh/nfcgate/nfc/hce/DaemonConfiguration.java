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

    public void enablePolling() {
        sendSimple("ENABLE_POLLING");
    }

    public void disablePolling() {
        sendSimple("DISABLE_POLLING");
    }

    public void disablePatch() {
        sendSimple("DISABLE");
    }

    public boolean isPatchEnabled() {
        return false;
    }

    public void uploadConfiguration(byte[] config) {
        Intent intent = new Intent();
        intent.putExtra("action", "UPLOAD");
        intent.putExtra("config", config);
        send(intent);
    }

    private void sendSimple(String action) {
        Intent intent = new Intent();
        intent.putExtra("action", action);
        send(intent);
    }

    private void send(Intent intent) {
        intent.setAction("tud.seemuh.nfcgate.daemoncall");
        mContext.sendBroadcast(intent);
    }

    public void requestPatchState() {
        sendSimple("REQSTATE");
    }
}
