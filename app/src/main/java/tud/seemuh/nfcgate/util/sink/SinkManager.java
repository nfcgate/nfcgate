package tud.seemuh.nfcgate.util.sink;

import java.io.PipedReader;

public class SinkManager implements Runnable {
    private PipedReader mReader;

    public SinkManager(PipedReader reader) {
        mReader = reader;
    }

    public void addSink(String sinkName) {
        // TODO Implement
    }

    @Override
    public void run() {
        // TODO Implement
    }
}
