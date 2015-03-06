package tud.seemuh.nfcgate.util.sink;

import java.util.concurrent.BlockingQueue;

/**
 * The FileSink class provides a simple file dump interface. Data is written to a regular text file
 * and can be read using any text editor.
 */
public class FileSink implements Sink {
    public String TAG = "FileSink";

    private BlockingQueue<byte[]> mQueue;
    private String mOutputFileName;

    public FileSink(String outfile) {
        mOutputFileName = outfile;
    }

    @Override
    public void setQueue(BlockingQueue<byte[]> readQueue) {
        mQueue = readQueue;
    }

    @Override
    public void run() {
        // TODO Implement
    }
}
