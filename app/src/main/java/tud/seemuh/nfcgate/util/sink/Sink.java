package tud.seemuh.nfcgate.util.sink;

import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.util.NfcComm;

/**
 * The Sink interface defines an API for different types of data sinks.
 * A data sink is a class which receives all NFC traffic and does something with it.
 * For example, a FileSink could log all raw bytes to file, a NetworkSink could send it to another
 * Server, ...
 *
 * Sinks are communication with a SinkManager using a java.io.PipedReader. They receive raw bytes
 * and it is up to them to decide what to do with them. They SHOULD NOT block for too long to avoid
 * blocking the SinkManager (writes to PipedWriters block if the buffer is full).
 */
public interface Sink extends Runnable {
    // Set the BlockingQueue which should be used to receive data from the SinkManager
    public void setQueue(BlockingQueue<NfcComm> readQueue);

    // The main loop of the Sink thread. This is where all the work takes place
    public void run();
}
