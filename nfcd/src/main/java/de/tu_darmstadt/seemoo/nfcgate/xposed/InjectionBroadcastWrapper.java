package de.tu_darmstadt.seemoo.nfcgate.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;

public class InjectionBroadcastWrapper extends BroadcastReceiver {
    private final Context mCtx;
    private boolean mCaptureEnabled = false;
    private final ArrayList<Bundle> mCaptured = new ArrayList<>();

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public InjectionBroadcastWrapper(Context ctx) {
        mCtx = ctx;

        // load our native library
        loadForeignLibrary(ctx, "de.tu_darmstadt.seemoo.nfcgate", "nfcgate");

        // start broadcast receiver on handler thread
        HandlerThread ht = new HandlerThread("ht");
        ht.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(this, new IntentFilter("de.tu_darmstadt.seemoo.nfcgate.daemoncall"), null, new Handler(ht.getLooper()), Context.RECEIVER_EXPORTED);
        } else {
            ctx.registerReceiver(this, new IntentFilter("de.tu_darmstadt.seemoo.nfcgate.daemoncall"), null, new Handler(ht.getLooper()));
        }

        // try to install our hooks, schedule retry if needed
        if (installHooks() == HookResult.ERROR_RETRY)
            new Handler(ht.getLooper()).postDelayed(this::installHooks, 3000);
    }

    public HookResult installHooks() {
        HookResult result = HookResult.fromValue(Native.Instance.installHooks());
        if (result == HookResult.ERROR_FATAL)
            Log.e("HOOKNFC", "Native hook failed (fatal)");
        else if (result == HookResult.ERROR_RETRY)
            Log.i("HOOKNFC", "Native hook failed (for now)");
        else
            Log.i("HOOKNFC", "Native hook success");

        return result;
    }

    /** @noinspection unused*/
    // used by Hooks
    public boolean isPatchEnabled() {
        return Native.Instance.isPatchEnabled();
    }

    /** @noinspection unused*/
    // used by Hooks
    public boolean isCaptureEnabled() {
        return mCaptureEnabled;
    }

    /** @noinspection unused*/
    // used by Hooks
    public void addCapture(Bundle capture) {
        mCaptured.add(capture);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String op = intent.getStringExtra("op");
        Log.i("NATIVENFC", "Command: " + op);

        if ("SET_CONFIG".equals(op)) {
            Native.Instance.setConfig(intent.getByteArrayExtra("config"));
        }
        else if ("RESET_CONFIG".equals(op)) {
            Native.Instance.resetConfig();
        }
        else if ("SET_CAPTURE".equals(op)) {
            mCaptureEnabled = intent.getBooleanExtra("enabled", false);

            if (!mCaptureEnabled) {
                // deliver capture
                mCtx.startActivity(makeResponseIntent()
                        .putExtra("type", "CAPTURE")
                        .putParcelableArrayListExtra("capture", mCaptured));

                // delete capture
                mCaptured.clear();
            }
        }
        else if ("INSTALL_HOOKS".equals(op)) {
            // deliver hook status
            mCtx.startActivity(makeResponseIntent()
                    .putExtra("type", "HOOK_STATUS")
                    .putExtra("hookEnabled", installHooks() == HookResult.SUCCESS));
        }
    }

    private Intent makeResponseIntent() {
        return new Intent()
                .setPackage("de.tu_darmstadt.seemoo.nfcgate")
                .setAction("de.tu_darmstadt.seemoo.nfcgate.daemoncall")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
    }

    private String combinePath(String p1, String p2) {
        return p1 + (p1.endsWith("/") ? "" : "/") + p2;
    }
}
