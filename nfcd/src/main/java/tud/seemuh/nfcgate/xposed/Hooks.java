package tud.seemuh.nfcgate.xposed;


import android.app.Application;
import android.util.Log;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hooks implements IXposedHookLoadPackage {

    private final String IPC_SOCK_DIR = "/data/data/tud.seemuh.nfcgate/ipc";
    private IPCBroadcastReceiver mReceiver;

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if(!"com.android.nfc".equals(lpparam.packageName))
            return;

        //System.loadLibrary("nfcgate-native");
        System.load("/data/data/tud.seemuh.nfcgate/lib/libnfcgate-native.so");


        // hook construtor to catch application context
        XposedHelpers.findAndHookConstructor("com.android.nfc.NfcService", lpparam.classLoader, Application.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.i("HOOKNFC", "constructor");
                Application app = (Application) param.args[0];
                mReceiver = new IPCBroadcastReceiver(app);
            }
        });

        // hook findSelectAid to route all APDUs to our app
        XposedHelpers.findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader, "findSelectAid", byte[].class, new XC_MethodHook() {
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
        XposedHelpers.findAndHookMethod("com.android.nfc.dhimpl.NativeNfcManager", lpparam.classLoader, "getMaxTransceiveLength", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                int technology = (int) param.args[0];
                if (technology == 3 /* 3=TagTechnology.ISO_DEP */) {
                    param.setResult(2462);
                }
            }
        });
    }
}
