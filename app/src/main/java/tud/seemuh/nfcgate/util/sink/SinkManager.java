package tud.seemuh.nfcgate.util.sink;

import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SinkManager implements Runnable {
    private final String TAG = "SinkManager";
    
    // Enum enumerating all installed Sinks. Used to identify a sink to perform operations on.
    // Devs: Add new sink types here
    public enum SinkType {
        FILE,
        DISPLAY_TEXTVIEW,
    }

    // BlockingQueue linked to the NetHandler
    private BlockingQueue<NfcComm> mInputQueue;
    
    // Storage for Sink instances and Threads
    private HashMap<Sink, BlockingQueue<NfcComm>> mQueueMap = new HashMap<Sink, BlockingQueue<NfcComm>>();
    private HashMap<Sink, Thread> mThreadMap = new HashMap<Sink, Thread>();
    private HashMap<SinkType, Sink> mSinkInstanceMap = new HashMap<SinkType, Sink>();

    public SinkManager(BlockingQueue<NfcComm> que) {
        mInputQueue = que;
    }

    /**
     * Common functionality for all addSink operations.
     * @param cSink The new Sink object, already instantiated
     * @param cSinkType The SinkType (from the enum above)
     */
    private void addSinkCommon(Sink cSink, SinkType cSinkType) {
        // Set up queue
        BlockingQueue<NfcComm> sharedQueue = new LinkedBlockingQueue<NfcComm>();

        // Pass linked pipe to the Sink
        cSink.setQueue(sharedQueue);

        // Store information in HashMaps
        mQueueMap.put(cSink, sharedQueue);
        mSinkInstanceMap.put(cSinkType, cSink);
    }

    /**
     * Add a new Sink to the SinkManager
     * @param sinkIdentifier Identifier for the type of Sink.
     */
    public void addSink(SinkType sinkIdentifier) {
        // Currently, there are no sinks that can be added without any parameters
        // If there ever are any, put them here
        Log.e(TAG, "addSink: passed Enum not handled with these parameters.");
    }

    /**
     * Add a new Sink using one TextView to the SinkManager
     * @param sinkIdentifier Identifier for the type of Sink.
     * @param tView TextView the Sink requires
     */
    public void addSink(SinkType sinkIdentifier, TextView tView) {
        Sink newSink;
        if (sinkIdentifier == SinkType.DISPLAY_TEXTVIEW) {
            newSink = new TextViewSink(tView);
        } else {
            Log.e(TAG, "addSink: passed Enum not handled with these parameters.");
            return;
        }
        addSinkCommon(newSink, sinkIdentifier);
    }

    /**
     * Add a new Sink using one String to the SinkManager
     * @param sinkIdentifier Identifier for the type of Sink.
     * @param cfgString String the Sink requires
     */
    public void addSink(SinkType sinkIdentifier, String cfgString) {
        // Initialize the Sink object
        // Devs: Add new sink types here
        Sink newSink;
        if (sinkIdentifier == SinkType.FILE) {
            newSink = new FileSink(cfgString);
        } else {
            Log.e(TAG, "addSink: passed Enum not handled with these parameters.");
            return;
        }
        addSinkCommon(newSink, sinkIdentifier);
    }

    @Override
    public void run() {
        // Start all Sink threads
        for (Sink cSink : mQueueMap.keySet()) {
            Thread sinkThread = new Thread(cSink);
            sinkThread.start();
            mThreadMap.put(cSink, sinkThread);
        }

        // Start normal operation
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Retrieve one byte[] from the queue (blocking until available or interrupted)
                NfcComm msg = mInputQueue.take();
                // Distribute the byte[] to all Sinks
                for (BlockingQueue<NfcComm> cQueue : mQueueMap.values()) {
                    try {
                        // Distribute to current Queue, raising exception if Queue is full
                        cQueue.add(msg);
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "run: Normal Operation: Queue full, skipping.");
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "run: Normal Operation: Interrupted. Shutting down.");
                break;
            }
        }

        // Shutdown phase
        // Shutdown all Threads
        for (Thread cThread : mThreadMap.values()) {
            cThread.interrupt();
        }

        // TODO Clean up pointers (set to null)?
    }
}
