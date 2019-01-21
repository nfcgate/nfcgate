package tud.seemuh.nfcgate.xposed;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;

public class InjectionBroadcastWrapper extends BroadcastReceiver {
    private Context mCtx;
    private boolean mCaptureEnabled = false;
    private ArrayList<Bundle> mCaptured = new ArrayList<>();


    public InjectionBroadcastWrapper(Context ctx) {
        mCtx = ctx;

        // load our native library
        loadForeignLibrary(ctx, "tud.seemuh.nfcgate", "nfcgate");

        // start broadcast receiver on handler thread
        HandlerThread ht = new HandlerThread("ht");
        ht.start();
        ctx.registerReceiver(this, new IntentFilter("tud.seemuh.nfcgate.daemoncall"), null, new Handler(ht.getLooper()));
    }

    public boolean isHookEnabled() {
        return Native.Instance.isHookEnabled();
    }

    public boolean isCaptureEnabled() {
        return mCaptureEnabled;
    }

    public void addCapture(Bundle capture) {
        mCaptured.add(capture);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String op = intent.getStringExtra("op");
        Log.d("NATIVENFC", "Command: " + op);

        if ("SET_CONFIG".equals(op)) {
            Native.Instance.setConfiguration(intent.getByteArrayExtra("config"));
        }
        else if ("SET_POLLING".equals(op)) {
            Native.Instance.setPolling(intent.getBooleanExtra("enabled", false));
        }
        else if ("SET_CAPTURE".equals(op)) {
            mCaptureEnabled = intent.getBooleanExtra("enabled", false);

            if (!mCaptureEnabled) {
                // deliver capture
                mCtx.startActivity(new Intent()
                        .setPackage("tud.seemuh.nfcgate")
                        .setAction("tud.seemuh.nfcgate.capture")
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putParcelableArrayListExtra("capture", mCaptured));

                // delete capture
                mCaptured.clear();
            }
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
