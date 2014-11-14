package tud.seemuh.nfcgate.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findField;

/**
 * Created by Max on 07.11.14.
 *
 * This is a Module for the XPosed Framework that aims to provide support for DESFire Application
 * Select messages to the ADPU / HCE functions of Android. Android only allows "normal" AID select
 * Messages right now (00 A4 04), but DESFire readers use a different set of bytes (90 5A).
 *
 * For now, all DESFire selects are redirected to the AID "F0010203040506".
 */
public class XPosedModAidRouting implements IXposedHookLoadPackage {
    /**
     * Called when a package is loaded. Used to place our hook on the findSelectAid function.
     */
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.nfc")) {
            return;
        }
        // Hook the findSelectAid function from com.android.nfc.cardemulation
        findAndHookMethod("com.android.nfc.cardemulation.HostEmulationManager",
                lpparam.classLoader, "findSelectAid", byte[].class, SelectAidHook);
    }

    private final static String MYAID = "F0010203040506";

    /**
     * Our Hook function to actually do the check for the DESFire select command
     */
    private final XC_MethodHook SelectAidHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            // The received command is saved in the cmd byte[].
            byte[] cmd = (byte[]) param.args[0];
            XposedBridge.log("Entering hooked findSelectAid.");

            // Check if cmd can actually be a DESFire select.
            if (cmd != null && cmd.length > 0) {
                try {
                    // Check if we are in a state where we are waiting for a select command
                    // Object tObject = param.thisObject;
                    // if (findField(tObject.getClass(), "mState").getInt(tObject) == 1) {
                        // If we are in a state that is waiting for an AID, check if cmd is a
                        // DESFire select.
                    if (cmd[0] == 0x90 && cmd[1] == 0x5a) {
                        XposedBridge.log("Found DESFire SELECT, substituting AID.");
                        // Select detected, substituting AID and returning
                        // The original function will not be called after this.
                        param.setResult(MYAID);
                    } else {
                        XposedBridge.log("This is no DESFire SELECT, ignoring.");
                    }
                    // }
                    // Commented out this if block, since I am not sure we even need it. Let's try
                    // without the block and see what happens before we put it back in.
                } catch (Exception e) {
                    XposedBridge.log(e);
                    // Log, then ignore any exception, just carry on.
                }
            }
        }
    };
}
