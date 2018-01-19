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

    private Object mReceiver;

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if(!"com.android.nfc".equals(lpparam.packageName))
            return;

        // hook construtor to catch application context
        findAndHookConstructor("com.android.nfc.NfcService", lpparam.classLoader, Application.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.i("HOOKNFC", "constructor");
                Application app = (Application) param.args[0];

                // using context, inject our class into the nfc service class loader
                mReceiver = injectClass(app, "tud.seemuh.nfcgate", lpparam.classLoader, "tud.seemuh.nfcgate.xposed.InjectionBroadcastWrapper");
            }
        });

        // hook findSelectAid to route all APDUs to our app
        findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader, "findSelectAid", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                Log.i("HOOKNFC", "beforeHookedMethod");
                if (isNativeEnabled()) {
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

    private boolean isNativeEnabled() {
        try {
            return (boolean)mReceiver.getClass().getMethod("isEnabled").invoke(mReceiver);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to check native enabled", e);
        }

        return false;
    }

    private Object injectClass(Context ctx, String sourcePackage, ClassLoader target, String injectClass) {
        PackageManager pm = ctx.getPackageManager();

        // find our foreign source directory
        String sourceDir;
        try {
            sourceDir = pm.getPackageInfo(sourcePackage, 0).applicationInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("HOOKNFC", "Failed to find source package " + sourcePackage);
            return null;
        }

        // add our sources to the target's class loader and
        // create injected class using target loader and instance it with context
        try {
            Method adp = target.getClass().getMethod("addDexPath", String.class);
            adp.invoke(target, sourceDir);
            Class loaded = target.loadClass(injectClass);
            return loaded.getConstructor(Context.class).newInstance(ctx);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to construct injected class", e);
        }

        return null;
    }
}
