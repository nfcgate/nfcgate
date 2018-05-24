package tud.seemuh.nfcgate.network.threading;

import java.io.DataOutputStream;
import java.io.IOException;

import tud.seemuh.nfcgate.network.SendRecord;
import tud.seemuh.nfcgate.network.ServerConnection;

public class SendThread extends BaseThread {
    // references
    private ServerConnection mConnection;
    private DataOutputStream mWriteStream;

    /**
     * Waits on sendQueue and sends the data over the specified stream
     */
    public SendThread(ServerConnection connection) {
        super();
        mConnection = connection;
    }

    @Override
    void initThread() throws IOException {
        mWriteStream = new DataOutputStream(mConnection.getSocket().getOutputStream());
    }

    /**
     * Tries to send one item from the sendQueue.
     */
    @Override
    void runInternal() throws IOException {
        SendRecord record = mConnection.getSendQueue().poll();
        if (record != null) {
            // 4 byte data length
            mWriteStream.writeInt(record.getData().length);
            // 1 byte session number
            mWriteStream.writeByte(record.getSession());
            // send actual data
            mWriteStream.write(record.getData());
            // flush for good measure
            mWriteStream.flush();
        }
    }

    @Override
    void onError() {
        // TODO: error handling
    }
}
