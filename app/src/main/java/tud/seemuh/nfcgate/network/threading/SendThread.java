package tud.seemuh.nfcgate.network.threading;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import tud.seemuh.nfcgate.network.data.NetworkStatus;
import tud.seemuh.nfcgate.network.data.SendRecord;
import tud.seemuh.nfcgate.network.ServerConnection;

public class SendThread extends BaseThread {
    private static final String TAG = "SendThread";

    // references
    private DataOutputStream mWriteStream;

    /**
     * Waits on sendQueue and sends the data over the specified stream
     */
    public SendThread(ServerConnection connection) {
        super(connection);
    }

    @Override
    void initThread() throws IOException {
        mWriteStream = new DataOutputStream(mSocket.getOutputStream());
    }

    /**
     * Tries to send one item from the sendQueue.
     */
    @Override
    void runInternal() throws IOException, InterruptedException {
        SendRecord record = mConnection.getSendQueue().take();
        Log.v(TAG, "Sending message of " + record.getData().length + " bytes");

        // 4 byte data length
        mWriteStream.writeInt(record.getData().length);
        // 1 byte session number
        mWriteStream.writeByte(record.getSession());
        // send actual data
        mWriteStream.write(record.getData());
        // flush for good measure
        mWriteStream.flush();
    }

    @Override
    void onError(Exception e) {
        Log.e(TAG, "Send onError", e);
        mConnection.reportStatus(NetworkStatus.ERROR);
    }
}
