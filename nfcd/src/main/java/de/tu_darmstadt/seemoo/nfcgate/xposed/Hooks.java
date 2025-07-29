package de.tu_darmstadt.seemoo.nfcgate.xposed;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.tu_darmstadt.seemoo.nfcgate.util.Utils;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hooks implements IXposedHookLoadPackage {
    private interface NfcServiceConstructorHook {
        void afterHookedMethod(XC_MethodHook.MethodHookParam param);
    }

    private Object mReceiver;
    private Object mNfcServiceInstance;

    public void handleLoadPackage(final LoadPackageParam lpparam) {
        // hook our own NfcManager to indicate that the hook is loaded and active
        if ("de.tu_darmstadt.seemoo.nfcgate".equals(lpparam.packageName)) {
            // indicate that the hook worked and the xposed module is active
            findAndHookMethod("de.tu_darmstadt.seemoo.nfcgate.nfc.NfcManager", lpparam.classLoader,
                    "isModuleLoaded", XC_MethodReplacement.returnConstant(true));
        } else if ("com.android.nfc".equals(lpparam.packageName) || "com.google.android.nfc".equals(lpparam.packageName)) {
            // hook constructor to catch application context
            hookNfcServiceConstructor(lpparam.classLoader, new NfcServiceConstructorHook() {
                @Override
                public void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                    // only execute once
                    if (mNfcServiceInstance != null)
                        return;

                    Log.i("HOOKNFC", "Launching InjectionBroadcastWrapper");

                    // save nfc service instance for later
                    mNfcServiceInstance = param.thisObject;

                    // using context, inject our class into the nfc service class loader
                    mReceiver = loadOrInjectClass((Application) param.args[0],
                            "de.tu_darmstadt.seemoo.nfcgate", getClass().getClassLoader(),
                            lpparam.classLoader, "de.tu_darmstadt.seemoo.nfcgate.xposed.InjectionBroadcastWrapper");
                }
            });

            // hook findSelectAid to route all HCE APDUs to our app
            findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader,
                    "findSelectAid",
                    byte[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.i("HOOKNFC", "HostEmulationManager::findSelectAid; data: " + Utils.bytesToHex((byte[]) param.args[0]));
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Log.i("HOOKNFC", "HostEmulationManager::findSelectAid; old result: " + param.getResult());

                    if (isPatchEnabled()) {
                        // setting a result will overwrite the original result
                        // F0010203040506 is an aid registered by the nfcgate hce service
                        param.setResult("F0010203040506");
                        Log.i("HOOKNFC", "HostEmulationManager::findSelectAid; changing result to F0010203040506");
                    } else
                        Log.i("HOOKNFC", "HostEmulationManager::findSelectAid; patch not enabled, not changing AID");
                }
            });

            // support extended length apdus
            // see http://stackoverflow.com/questions/25913480/what-are-the-requirements-for-support-of-extended-length-apdus-and-which-smartph
            findAndHookMethod("com.android.nfc.dhimpl.NativeNfcManager", lpparam.classLoader,
                    "getMaxTransceiveLength",
                    int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {

                    int technology = (int) param.args[0];
                    if (technology == 3 /* 3=TagTechnology.ISO_DEP */) {
                        param.setResult(2462);
                    }
                }
            });

            // hook 'transceive' method for on-device capture of request/response data
            findAndHookMethod("com.android.nfc.NfcService$TagService", lpparam.classLoader,
                    "transceive",
                    int.class, byte[].class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (isCaptureEnabled()) {
                        byte[] commandData = (byte[]) param.args[1];
                        addCaptureData(false, commandData);

                        byte[] responseData = (byte[]) param.getResult().getClass().getMethod("getResponseOrThrow").invoke(param.getResult());
                        addCaptureData(true, responseData);

                        Log.i("HOOKNFC", "Captured tag read");
                    }

                }
            });

            // hook tag dispatch for on-device capture of initial data
            findAndHookMethod("com.android.nfc.NfcDispatcher", lpparam.classLoader,
                    "dispatchTag",
                    Tag.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    dumpAIDRegistrations();

                    if (isCaptureEnabled()) {
                        Tag tag = (Tag) param.args[0];
                        addCaptureInitial(tag);

                        Log.i("HOOKNFC", "Captured initial data");
                    }
                }
            });

            // hook onHostEmulationData method for on-device HCE request capture
            findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader,
                    preLollipop("notifyHostEmulationData", "onHostEmulationData"),
                    byte[].class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {

                    if (isCaptureEnabled()) {
                        byte[] commandData = (byte[]) param.args[0];
                        addCaptureData(false, commandData);

                        Log.i("HOOKNFC", "Captured HCE request");
                    }
                }
            });

            // hook notifyHostEmulationActivated method for on-device HCE initial capture
            findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader,
                    preLollipop("notifyHostEmulationActivated", "onHostEmulationActivated"),
                    new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    dumpAIDRegistrations();

                    if (isCaptureEnabled()) {
                        addCaptureInitial(null);

                        Log.i("HOOKNFC", "Captured HCE initial data");
                    }
                }
            });

            // hook sendData method for on-device HCE response capture
            findAndHookMethod("com.android.nfc.NfcService", lpparam.classLoader,
                    "sendData",
                    byte[].class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {

                    if (isCaptureEnabled()) {
                        byte[] responseData = (byte[]) param.args[0];
                        addCaptureData(true, responseData);

                        Log.i("HOOKNFC", "Captured HCE response");
                    }
                }
            });
        }
    }

    private void addCaptureInitial(Parcelable initial) {
        Bundle capture = new Bundle();
        capture.putString("type", "INITIAL");
        capture.putParcelable("data", initial);
        capture.putLong("timestamp", System.currentTimeMillis());

        addCapture(capture);
    }

    private void addCaptureData(boolean tag, byte[] data) {
        Bundle capture = new Bundle();
        capture.putString("type", tag ? "TAG" : "READER");
        capture.putByteArray("data", data);
        capture.putLong("timestamp", System.currentTimeMillis());

        addCapture(capture);
    }

    private boolean isPatchEnabled() {
        try {
            return (boolean)mReceiver.getClass().getMethod("isPatchEnabled").invoke(mReceiver);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to get isPatchEnabled", e);
        }

        return false;
    }

    private boolean isCaptureEnabled() {
        try {
            return (boolean)mReceiver.getClass().getMethod("isCaptureEnabled").invoke(mReceiver);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to get isCaptureEnabled", e);
        }

        return false;
    }

    private void addCapture(Bundle capture) {
        try {
            mReceiver.getClass().getMethod("addCapture", Bundle.class).invoke(mReceiver, capture);
        } catch (Exception e) {
            Log.e("HOOKNFC", "Failed to get addCaptureData", e);
        }
    }

    private void dumpAIDRegistrations() {
        try {
            Object nfcService = mNfcServiceInstance;

            // Get mCardEmulationManager field of NfcService instance
            Field cardEmulationManager = XposedHelpers.findField(nfcService.getClass(), "mCardEmulationManager");
            Object cardEmulationManagerObj = cardEmulationManager.get(nfcService);

            // Get mAidCache field of CardEmulationManager instance
            Field aidCache = XposedHelpers.findField(cardEmulationManagerObj.getClass(), "mAidCache");
            Object aidCacheObj = aidCache.get(cardEmulationManagerObj);

            // Get mAidCache field of RegisteredAidCache instance. This field maps AIDs to AidResolveInfo.
            Field cacheMap = XposedHelpers.findField(aidCacheObj.getClass(), "mAidCache");
            TreeMap<String, ? extends Object> cacheMapObj = ((TreeMap<String, ? extends Object>) cacheMap.get(aidCacheObj));

            // AidResolveInfo implements toString, so we just dump the whole TreeMap
            Log.i("HOOKNFC", "AID reg dump:" + cacheMapObj.toString());
        } catch (Exception e) {
            Log.e("HOOKNFC", "AID reg dump failed", e);
        }
    }

    private String preLollipop(String oldName, String newName) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? oldName : newName;
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
            Class<?> loaded = target.loadClass(loadClass);
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

    /// Finds the NFCService constructor, hooks it and calls the hook only if the application context is a parameter
    private void hookNfcServiceConstructor(ClassLoader classLoader, NfcServiceConstructorHook hook) {
        Class<?> nfcServiceClass = findSpecificNfcService(classLoader);
        if (nfcServiceClass == null)
            return;

        Log.i("HOOKNFC", "Specific NfcService selected: " + nfcServiceClass.getSimpleName());
        XposedBridge.hookAllConstructors(nfcServiceClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Log.i("HOOKNFC", "NfcService constructor called: " + toGenericString(param.method));

                if (param.args.length > 0 && isOfClass(param.args[0], Context.class))
                    hook.afterHookedMethod(param);
                else
                    Log.e("HOOKNFC", "NfcService constructor called without Application context");
            }
        });
    }

    /// Finds the most specific NFCService class that exists
    private Class<?> findSpecificNfcService(ClassLoader classLoader) {
        List<String> candidates = List.of(
                "com.android.nfc.LNfcService",
                "com.android.nfc.NfcService"
        );

        for (String candidate : candidates) {
            Class<?> clazz = XposedHelpers.findClassIfExists(candidate, classLoader);
            if (clazz != null)
                return clazz;
        }

        Log.e("HOOKNFC", "Failed to find NfcService class");
        return null;
    }

    private String toGenericString(Member member) {
        if (member instanceof Constructor)
            return ((Constructor<?>) member).toGenericString();
        else if (member instanceof Method)
            return ((Method) member).toGenericString();
        else
            return "Unknown member " + member.getName() + " with type " + member.getClass().getSimpleName();
    }

    private boolean isOfClass(Object a, Class<?> clazz) {
        return a != null && clazz.isInstance(a);
    }
}
