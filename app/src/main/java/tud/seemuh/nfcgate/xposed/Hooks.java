package tud.seemuh.nfcgate.xposed;


import android.nfc.tech.TagTechnology;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hooks implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private final String IPC_SOCK_DIR = "/data/data/tud.seemuh.nfcgate/ipc";

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if(!"com.android.nfc".equals(lpparam.packageName))
            return;

        //System.loadLibrary("nfcgate-native");
        System.load("/data/data/tud.seemuh.nfcgate/lib/libnfcgate-native.so");

        findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager", lpparam.classLoader, "findSelectAid", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                Log.i("HOOKNFC", "beforeHookedMethod");
                if (isPatchEnabled()) {
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

                int technology = (int)param.args[0];
                if(technology == 3 /* 3=TagTechnology.ISO_DEP */) {
                    param.setResult(2462);
                }
            }
        });
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        try {
            File f = new File(IPC_SOCK_DIR);
            if (!f.exists()) {
                f.mkdir();
                f.setExecutable(true, false);
                f.setReadable(true, false);
                f.setWritable(true, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public native boolean isPatchEnabled();
}
