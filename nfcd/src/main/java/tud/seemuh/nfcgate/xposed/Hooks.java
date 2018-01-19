package tud.seemuh.nfcgate.xposed;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.*;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hooks implements IXposedHookLoadPackage {

    private IPCBroadcastReceiver mReceiver;

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if(!"com.android.nfc".equals(lpparam.packageName))
            return;

        // hook construtor to catch application context
        findAndHookConstructor("com.android.nfc.NfcService", lpparam.classLoader, Application.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.i("HOOKNFC", "constructor");
                Application app = (Application) param.args[0];
                mReceiver = new IPCBroadcastReceiver(app);

                // using context, load our foreign native library
                loadForeignLibrary(app, lpparam.classLoader, "tud.seemuh.nfcgate", "nfcgate");
            }
        });

        // hook findSelectAid to route all APDUs to our app
        findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader, "findSelectAid", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                Log.i("HOOKNFC", "beforeHookedMethod");
                if (Native.Instance.isEnabled()) {
                    Log.i("HOOKNFC", "enabled");
                    // setting a result will prevent the original method to run.
                    // F0010203040506 is a aid registered by the nfcgate hce service
                    param.setResult("F0010203040506");
                }
            }
        });

        // support extended length apdus
        // see http://stackoverflow.com/questions/25913480/what-are-the-requirements-for-support-of-extended-length-apdus-and-which-smartph
        findAndHookMethod("com.android.nfc.dhimpl.NativeNfcManager", lpparam.classLoader, "getMaxTransceiveLength", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                int technology = (int) param.args[0];
                if (technology == 3 /* 3=TagTechnology.ISO_DEP */) {
                    param.setResult(2462);
                }
            }
        });
    }

    private void loadForeignLibrary(Context ctx, ClassLoader cl, String foreignPkg, String name) {
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
        try {
            Method m = Runtime.class.getDeclaredMethod("doLoad", String.class, ClassLoader.class);
            m.setAccessible(true);
            Object res = m.invoke(Runtime.getRuntime(), libPath, cl);

            if (res != null)
                Log.e("HOOKNFC", res.toString());
        } catch (Exception e) {
            Log.e("HOOKNFC", "Could not load nfcgate-native library at " + libPath, e);
            return;
        }

        Log.d("HOOKNFC", "Loaded library successfully");
    }

    private String combinePath(String p1, String p2) {
        return p1 + (p1.endsWith("/") ? "" : "/") + p2;
    }
}
