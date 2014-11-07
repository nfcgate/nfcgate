package tud.seemuh.nfcgate.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findField;

/**
 * Created by Max on 07.11.14.
 */
public class XPosedModAidRouting implements IXposedHookLoadPackage {
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.nfc.cardemulation")) {
            return;
        }
        findAndHookMethod("com.android.nfc.cardemulation", lpparam.classLoader, "findSelectAid",
                byte[].class, SelectAidHook);
    }

    private final static String MYAID = "F0010203040506";

    private final XC_MethodHook SelectAidHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            byte[] aid = (byte[]) param.args[0];

            if (aid != null && aid.length > 0) {
                try {
                    Object tObject = param.thisObject;
                    if (findField(tObject.getClass(), "mState").getInt(tObject) == 1) {
                        // If we are in a state that is waiting for an AID, do this.
                        if (aid[0] == 0x90 && aid[1] == 0x5a) {
                            // DESFire SELECT: 90 5A 00 00 AA AA AA AA 00, w/ AA AA AA AA as the AID
                            param.setResult(MYAID);
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    };
}
