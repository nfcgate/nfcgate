package tud.seemuh.nfcgate.reader;

import android.nfc.Tag;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Broadcom chips have a problem with DESFire cards: They set them into a specific mode with their
 * keepalive messages, which prevents us from sending messages using another mode. (See #33)
 * This class is an attempt at a workaround: We will try to "fake" a keepalive loop in order to
 * prevent Android from sending the destructive keepalive messages, as per
 * http://www.dematte.org/2014/08/15/AndroidNFCServiceAndThinClientOneProblemAndOneHack.aspx
 * This may or may not work, but it is at least worth a try.
 */
public class BroadcomWorkaround implements Runnable {
    private String TAG = "BroadcomWorkaround";
    private Tag mTag;

    public BroadcomWorkaround(Tag tag) {
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
            Log.e(TAG, "run: Rudely awoken from sleep. Exiting out of spite.");
        } catch (Exception e) {
            Log.e(TAG, "run: Encountered unspecified Exception: ", e);
        }
        Log.i(TAG, "run: Thread stopping");
    }
}
