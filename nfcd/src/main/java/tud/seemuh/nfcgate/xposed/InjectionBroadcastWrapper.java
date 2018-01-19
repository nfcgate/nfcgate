package tud.seemuh.nfcgate.xposed;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

public class InjectionBroadcastWrapper extends BroadcastReceiver {

    public InjectionBroadcastWrapper(Context ctx) {
        // load our native library
        loadForeignLibrary(ctx, "tud.seemuh.nfcgate", "nfcgate");

        HandlerThread handlerThread = new HandlerThread("ht");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        ctx.registerReceiver(this, new IntentFilter("tud.seemuh.nfcgate.daemoncall"), null, handler);
    }

    public boolean isEnabled() {
        return Native.Instance.isEnabled();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        Log.d("HOOKNFC", "Command: " + action);

        if("ENABLE".equals(action)) {
            Native.Instance.setEnabled(true);
        }
        else if("DISABLE".equals(action)) {
            Native.Instance.setEnabled(false);
        }
        else if("ENABLE_POLLING".equals(action)) {
            Native.Instance.enablePolling();
        }
        else if("DISABLE_POLLING".equals(action)) {
            Native.Instance.disablePolling();
        }
        else if("UPLOAD".equals(action)) {
            Native.Instance.uploadConfiguration(
                    intent.getByteExtra("bitf", (byte)0),
                    intent.getByteExtra("plat", (byte)0),
                    intent.getByteExtra("sak", (byte)0),
                    intent.getByteArrayExtra("hist"),
                    intent.getByteArrayExtra("uid")
            );
        }
        else if("REQSTATE".equals(action)) {
            Intent toaster = new Intent("tud.seemuh.nfcgate.toaster");
            toaster.putExtra("text", "Patch state: " + (Native.Instance.isEnabled() ? "Active" : "Inactive"));
            context.sendBroadcast(toaster);
        }
    }

    private void loadForeignLibrary(Context ctx, String foreignPkg, String name) {
        PackageManager pm = ctx.getPackageManager();

        // find foreign package library path and assemble libPath
        String libPath;
        try {
            String dir = pm.getPackageInfo(foreignPkg, 0).applicationInfo.nativeLibraryDir;
            libPath = combinePath(dir, "lib" + name + ".so");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("HOOKNFC", "Failed to find package " + foreignPkg);
            return;
        }

        // try to load the library
        System.load(libPath);
        Log.d("HOOKNFC", "Loaded library successfully");
    }

    private String combinePath(String p1, String p2) {
        return p1 + (p1.endsWith("/") ? "" : "/") + p2;
    }
}
