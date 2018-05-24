package tud.seemuh.nfcgate.network.threading;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import tud.seemuh.nfcgate.network.ServerConnection;

public class ReceiveThread extends BaseThread {
    // references
    private ServerConnection mConnection;
    private DataInputStream mReadStream;

    /**
     * Waits on sendQueue and sends the data over the specified stream
     */
    public ReceiveThread(ServerConnection connection) {
        super();
        mConnection = connection;
    }

    @Override
    void initThread() throws IOException {
        mReadStream = new DataInputStream(mConnection.getSocket().getInputStream());
    }

    /**
     * Tries to send one item from the sendQueue.
     */
    @Override
    void runInternal() throws IOException {
        // block and wait for the 4 byte length prefix
        int length = mReadStream.readInt();

        // block and wait for actual data
        byte[] data = new byte[length];
        mReadStream.readFully(data);

        // deliver data
        mConnection.onReceive(data);
    }

    @Override
    void onError() {
        // TODO: error handling
    }
}
