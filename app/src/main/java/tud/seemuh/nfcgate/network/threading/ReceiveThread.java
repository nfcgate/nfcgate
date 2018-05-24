package tud.seemuh.nfcgate.network.threading;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import tud.seemuh.nfcgate.network.ServerConnection;

public class ReceiveThread extends BaseThread {
    // references
    ServerConnection.Callback mCallback;
    private DataInputStream mReadStream;

    /**
     * Waits on sendQueue and sends the data over the specified stream
     */
    public ReceiveThread(ServerConnection.Callback callback, Socket socket) {
        super();

        try {
            mCallback = callback;
            mReadStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            onError();
        }
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
        mCallback.onReceive(data);
    }

    @Override
    void onError() {
        // TODO: error handling
    }
}
