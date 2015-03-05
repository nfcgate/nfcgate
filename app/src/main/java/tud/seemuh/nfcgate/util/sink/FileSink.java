package tud.seemuh.nfcgate.util.sink;

import java.io.PipedReader;

/**
 * The FileSink class provides a simple file dump interface. Data is written to a regular text file
 * and can be read using any text editor.
 */
public class FileSink implements Sink {
    public String TAG = "FileSink";

    private PipedReader mReadPipe;

    @Override
    public void setPipe(PipedReader readPipe) {
        mReadPipe = readPipe;
    }

    @Override
    public void run() {
        // TODO Implement
    }
}
