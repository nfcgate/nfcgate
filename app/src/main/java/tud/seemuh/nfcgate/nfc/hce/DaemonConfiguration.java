package tud.seemuh.nfcgate.nfc.hce;

import android.content.Context;
import android.content.Intent;

/**
 * Interface to the nfc daemon patches
 */
public class DaemonConfiguration {
    private Context mCtx;

    public DaemonConfiguration(Context ctx) {
        mCtx = ctx;
    }

    public void upload(byte[] config) {
        send("UPLOAD", config);
    }

    public void enable() {
        send("ENABLE", null);
    }

    public void disable() {
        send("DISABLE", null);
    }

    public void enablePolling() {
        send("ENABLE_POLLING", null);
    }

    public void disablePolling() {
        send("DISABLE_POLLING", null);
    }

    private void send(String action, byte[] config) {
        Intent intent = new Intent();
        intent.setAction("tud.seemuh.nfcgate.daemoncall");

        intent.putExtra("action", action);
        if (config != null)
            intent.putExtra("config", config);

        mCtx.sendBroadcast(intent);
    }
}
