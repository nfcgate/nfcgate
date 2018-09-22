package tud.seemuh.nfcgate.network.threading;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;

import tud.seemuh.nfcgate.network.NetworkStatus;
import tud.seemuh.nfcgate.network.ServerConnection;

public class ReceiveThread extends BaseThread {
    private static final String TAG = "ReceiveThread";

    // references
    private DataInputStream mReadStream;

    /**
     * Waits on sendQueue and sends the data over the specified stream
     */
    public ReceiveThread(ServerConnection connection) {
        super(connection);
    }

    @Override
    void initThread() throws IOException {
        mReadStream = new DataInputStream(mSocket.getInputStream());
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

        Log.v(TAG, "Got message of " + length + " bytes");

        // deliver data
        mConnection.onReceive(data);
    }

    @Override
    void onError(Exception e) {
        Log.e(TAG, "Receive onError", e);
        mConnection.reportStatus(NetworkStatus.ERROR);
    }
}
