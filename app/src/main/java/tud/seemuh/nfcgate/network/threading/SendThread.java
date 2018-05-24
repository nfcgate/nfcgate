package tud.seemuh.nfcgate.network.threading;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;

public class SendThread extends BaseThread {
    // references
    private Queue<byte[]> mSendQueue;
    private DataOutputStream mWriteStream;

    /**
     * Waits on sendQueue and sends the data over the specified stream
     */
    public SendThread(Queue<byte[]> sendQueue, Socket socket) {
        super();

        try {
            mSendQueue = sendQueue;
            mWriteStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            onError();
        }
    }

    /**
     * Tries to send one item from the sendQueue.
     */
    @Override
    void runInternal() throws IOException {
        byte[] data = mSendQueue.poll();
        if (data != null) {
            // prefix data with 4 byte length
            mWriteStream.writeInt(data.length);
            // send actual data
            mWriteStream.write(data);
            // flush for good measure
            mWriteStream.flush();
        }
    }

    @Override
    void onError() {
        // TODO: error handling
    }
}
