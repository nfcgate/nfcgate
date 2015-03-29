package tud.seemuh.nfcgate.util.sink;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.util.NfcComm;

/**
 * Sink for Session logging
 */
public class SessionLoggingSink implements Sink {
    public String TAG = "SessionLoggingSink";

    private BlockingQueue<NfcComm> mQueue;

    public SessionLoggingSink() throws SinkInitException {

    }

    @Override
    public void setQueue(BlockingQueue<NfcComm> readQueue) {
        mQueue = readQueue;
    }

    @Override
    public void run() {
        // Initialization phase

        // Main loop
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Get a new message
                NfcComm msg = mQueue.take();

                if (msg.getType() == NfcComm.Type.AnticolBytes) {
                    // We are dealing with an Anticol message
                    if (msg.isChanged()) {
                        // TODO
                    } else {
                        // TODO
                    }


                } else if (msg.getType() == NfcComm.Type.NFCBytes) {
                    // We are dealing with regular NFC traffic.
                    if (msg.getSource() == NfcComm.Source.CARD) {
                        // Write out NFC data sent by card
                        if (msg.isChanged()) {
                            // TODO
                        } else {
                            // TODO
                        }
                    } else if (msg.getSource() == NfcComm.Source.HCE) {
                        // Write out NFC data sent by reader
                        if (msg.isChanged()) {
                            // TODO
                        } else {
                            // TODO
                        }
                    } else {
                        Log.e(TAG, "run: Unhandled message source, doing nothing");
                    }
                } else {
                    Log.e(TAG, "run: Unhandled message type, doing nothing");
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "run: Interrupted, exiting.");
                break;
            }
        }

        // Main loop terminated, clean up

    }
}