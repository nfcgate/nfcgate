package tud.seemuh.nfcgate.util.sink;

import android.widget.TextView;

import java.util.concurrent.BlockingQueue;

/**
 * The DebugOutputSink class writes all bytes to a TextView.
 */
public class TextViewSink implements Sink {

    private String TAG = "DebugOutputSink";

    private TextView mDebugView;
    private BlockingQueue<NfcComm> mQueue;

    /**
     * Constructor. Expects a TextView to which he can append debug output
     * @param dView The output TextView
     */
    public TextViewSink(TextView dView) {
        mDebugView = dView;
    }

    /**
     * Set the BlockingQueue the Sink should use
     * @param readQueue The BlockingQueue object
     */
    @Override
    public void setQueue(BlockingQueue<NfcComm> readQueue) {
        mQueue = readQueue;
    }

    @Override
    public void run() {
        // TODO Implement
    }
}
