package tud.seemuh.nfcgate.util.sink;

import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.UpdateUI;
import tud.seemuh.nfcgate.util.Utils;

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

    private void UpdateTextView(String text) {
        new UpdateUI(mDebugView, UpdateUI.UpdateMethod.appendTextView).execute(text + "\n");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Retrieve a message from the queue (blocking until one is available)
                NfcComm msg = mQueue.take();
                // Handle message
                if (msg.getType() == NfcComm.Type.AnticolBytes) {
                    // We are dealing with an Anticol message
                    if (msg.isChanged()) {
                        String output = "Card data (Pre-filter in Brackets):"
                                + "\n  UID: " + Utils.bytesToHex(msg.getUid())
                                + " (" + Utils.bytesToHex(msg.getOldUid()) + ")"
                                + "\n  ATQA: " + Utils.bytesToHex(msg.getAtqa())
                                + " (" + Utils.bytesToHex(msg.getOldAtqa()) + ")"
                                + "\n  SAK: " + Utils.bytesToHex(msg.getSak())
                                + " (" + Utils.bytesToHex(msg.getOldSak()) + ")"
                                + "\n  Hist: " + Utils.bytesToHex(msg.getHist())
                                + " (" + Utils.bytesToHex(msg.getOldHist()) + ")";
                        UpdateTextView(output);
                    } else {
                        String output = "Card data:"
                                + "\n  UID: "  + Utils.bytesToHex(msg.getUid())
                                + "\n  ATQA: " + Utils.bytesToHex(msg.getAtqa())
                                + "\n  SAK: "  + Utils.bytesToHex(msg.getSak())
                                + "\n  Hist: " + Utils.bytesToHex(msg.getHist());
                        UpdateTextView(output);
                    }
                } else if (msg.getType() == NfcComm.Type.NFCBytes) {
                    // We are dealing with regular NFC traffic.
                    if (msg.getSource() == NfcComm.Source.CARD) {
                        // Write out NFC data sent by card
                        if (msg.isChanged()) {
                            UpdateTextView("C: " + Utils.bytesToHex(msg.getData())
                                    + "(" + Utils.bytesToHex(msg.getOldData()) + ")");
                        } else {
                            UpdateTextView("C: " + Utils.bytesToHex(msg.getData()));
                        }
                    } else if (msg.getSource() == NfcComm.Source.HCE) {
                        // Write out NFC data sent by reader
                        if (msg.isChanged()) {
                            UpdateTextView("R: " + Utils.bytesToHex(msg.getData())
                                    + "(" + Utils.bytesToHex(msg.getOldData()) + ")");
                        } else {
                            UpdateTextView("R: " + Utils.bytesToHex(msg.getData()));
                        }
                    } else {
                        Log.e(TAG, "run: Unhandled message source, doing nothing");
                    }
                } else {
                    Log.e(TAG, "run: Unhandled message type, doing nothing");
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "run: Interrupted. Shutting down.");
                break;
            }
        }
    }
}
