package tud.seemuh.nfcgate.util.sink;

import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SinkManager implements Runnable {
    private final String TAG = "SinkManager";
    
    // Enum enumerating all installed Sinks. Used to identify a sink to perform operations on.
    // Devs: Add new sink types here
    public enum SinkType {
        FILE
    }

    // BlockingQueue linked to the NetHandler
    private BlockingQueue<byte[]> mInputQueue;
    
    // Storage for Sink instances and Threads
    private HashMap<Sink, BlockingQueue<byte[]>> mQueueMap = new HashMap<Sink, BlockingQueue<byte[]>>();
    private HashMap<Sink, Thread> mThreadMap = new HashMap<Sink, Thread>();
    private HashMap<SinkType, Sink> mSinkInstanceMap = new HashMap<SinkType, Sink>();

    public SinkManager(BlockingQueue<byte[]> que) {
        mInputQueue = que;
    }

    /**
     * Add a new Sink to the SinkManager
     * @param sinkIdentifier Identifier for the type of Sink.
     */
    public void addSink(SinkType sinkIdentifier) {
        // Initialize the Sink object
        // Devs: Add new sink types here
        Sink newSink;
        if (sinkIdentifier == SinkType.FILE) {
            newSink = new FileSink();
        } else {
            Log.e(TAG, "addSink: passed Enum not handled.");
            return;
        }
        
        // Set up queue
        BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue<byte[]>();
        
        // Pass linked pipe to the Sink
        newSink.setQueue(sharedQueue);

        // Store information in HashMaps
        mQueueMap.put(newSink, sharedQueue);
        mSinkInstanceMap.put(sinkIdentifier, newSink);
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
                byte[] msg = mInputQueue.take();
                // Distribute the byte[] to all Sinks
                for (BlockingQueue<byte[]> cQueue : mQueueMap.values()) {
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
