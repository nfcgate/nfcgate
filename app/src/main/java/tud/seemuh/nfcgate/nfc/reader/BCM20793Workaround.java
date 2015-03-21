package tud.seemuh.nfcgate.nfc.reader;

import android.nfc.Tag;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The Broadcom BCM20793 has a problem with DESFire cards: It sets them into a specific mode with its
 * keepalive messages, which prevents us from sending messages using another mode. (See #33)
 * This class is a workaround: We will try to "fake" a keepalive loop in order to prevent Android
 * from sending the destructive keepalive messages, as per
 * http://www.dematte.org/2014/08/15/AndroidNFCServiceAndThinClientOneProblemAndOneHack.aspx
 *
 * We are basically reproducing the code from this function call:
 * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.4.4_r1/android/nfc/tech/BasicTagTechnology.java#73
 *
 * We are using reflection to gain access to the methods and classes we need.
 * The way we use them, the only effect should be that the Keepalive-Timer is reset every time we
 * call the connect()-Function in the loop below.
 */
public class BCM20793Workaround implements Runnable {
    public static boolean workaroundNeeded() {
        // File bcmdevice = new File("/dev/bcm2079x-i2c");
        // return bcmdevice.exists();
        return true;
        // TODO Appearently, the workaround is needed for more devices than previously thought
        // Enabling by default for now.
    }

    private String TAG = "BCM20793Workaround";
    private Tag mTag;

    public BCM20793Workaround(Tag tag) {
        mTag = tag;
    }

    @Override
    public void run() {
        try {
            // Get get getTagService Method
            Method getTagServiceMethod = mTag.getClass().getDeclaredMethod("getTagService");
            getTagServiceMethod.setAccessible(true);
            // Retrieve tagService object (INfcTag)
            Object tagService = getTagServiceMethod.invoke(mTag);
            Log.d(TAG, "run: Got tagService object");

            // get the getServiceHandle Method
            Method getTagServiceHandleMethod = mTag.getClass().getDeclaredMethod("getServiceHandle");
            getTagServiceHandleMethod.setAccessible(true);
            // Retrieve ServiceHandle (int)
            Object serviceHandle = getTagServiceHandleMethod.invoke(mTag);
            Log.d(TAG, "run: got ServiceHandle object");


            // get the getConnectedTechnology Method
            Method getConnectedTechnologyMethod = mTag.getClass().getDeclaredMethod("getConnectedTechnology");
            getConnectedTechnologyMethod.setAccessible(true);
            // Retrieve ServiceHandle (int)
            Object connectedTechnology = getConnectedTechnologyMethod.invoke(mTag);
            Log.d(TAG, "run: got connectedTechnology object");

            // Get connect method
            Method connectMethod = tagService.getClass().getDeclaredMethod("connect", new Class[] {int.class, int.class});
            connectMethod.setAccessible(true);
            Log.d(TAG, "run: Got connect method object");

            while (!Thread.currentThread().isInterrupted()) {
                connectMethod.invoke(tagService, serviceHandle, connectedTechnology);
                Thread.sleep(100);
            }

        } catch (NoSuchMethodException e) {
            Log.e(TAG, "run: Method not found", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "run: InvocationTargetException: ", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "run: IllegalAccessException: ", e);
        } catch (InterruptedException e) {
            Log.i(TAG, "run: Rudely awoken from sleep. Exiting out of spite.");
        } catch (Exception e) {
            Log.e(TAG, "run: Encountered unspecified Exception: ", e);
        }
        Log.i(TAG, "run: Thread stopping");
    }
}
