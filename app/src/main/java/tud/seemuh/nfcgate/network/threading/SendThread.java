package tud.seemuh.nfcgate.network.threading;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import tud.seemuh.nfcgate.network.NetworkStatus;
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
        Socket socket = mConnection.getSocket();

        if (socket == null)
            throw new IOException("Socket error");
        else
            mWriteStream = new DataOutputStream(socket.getOutputStream());
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
        mConnection.reportStatus(NetworkStatus.SEND_ERROR);
    }
}
