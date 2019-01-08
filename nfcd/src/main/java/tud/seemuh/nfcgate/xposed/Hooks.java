package tud.seemuh.nfcgate.xposed;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.*;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hooks implements IXposedHookLoadPackage {

    private Object mReceiver;

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // hook our own NfcManager to indicate that the hook is loaded and active
        if ("tud.seemuh.nfcgate".equals(lpparam.packageName)) {
            findAndHookMethod("tud.seemuh.nfcgate.nfc.NfcManager", lpparam.classLoader,
                    "isHookLoaded", XC_MethodReplacement.returnConstant(true));
        }
        else if ("com.android.nfc".equals(lpparam.packageName)) {
            // hook construtor to catch application context
            findAndHookConstructor("com.android.nfc.NfcService", lpparam.classLoader, Application.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    // using context, inject our class into the nfc service class loader
                    mReceiver = loadOrInjectClass((Application) param.args[0],
                            "tud.seemuh.nfcgate", getClass().getClassLoader(),
                            lpparam.classLoader, "tud.seemuh.nfcgate.xposed.InjectionBroadcastWrapper");
                }
            });

            // hook findSelectAid to route all APDUs to our app
            findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader, "findSelectAid", byte[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

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

            // hook transceive method for on-device capture
            findAndHookMethod("com.android.nfc.NfcService.TagService", lpparam.classLoader, "transceive", int.class, byte[].class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // TODO: figure out where to store this and how to transfer it to the UI

                    // nfc command
                    byte[] cmd = (byte[]) param.args[1];
                    // nfc response
                    byte[] response = (byte[]) param.getResult().getClass().getMethod("getResponseOrThrow").invoke(param.getResult());
                    // timestamp
                    long timestamp = System.currentTimeMillis();

                    Log.d("HOOKNFC", "Captured data: at " + timestamp + " got " + cmd.length + "/" + response.length);

                }
            });

        }
    }

    private boolean isNativeEnabled() {
        try {
            return (boolean)mReceiver.getClass().getMethod("isEnabled").invoke(mReceiver);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to check native enabled", e);
        }

        return false;
    }

    private Object loadOrInjectClass(Context ctx, String sourcePackage,
                                     ClassLoader current, ClassLoader target,
                                     String className) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return injectClass(ctx, sourcePackage, target, className);
        else
            return loadClass(ctx, current, className);
    }

    private Object loadClass(Context ctx, ClassLoader target, String loadClass) {
        // instantiate class in given class loader
        try {
            Class loaded = target.loadClass(loadClass);
            return loaded.getConstructor(Context.class).newInstance(ctx);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to construct loaded class", e);
        }
        return null;
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

            return loadClass(ctx, target, injectClass);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to construct injected class", e);
        }

        return null;
    }
}
