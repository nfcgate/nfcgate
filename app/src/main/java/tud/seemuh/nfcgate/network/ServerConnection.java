package tud.seemuh.nfcgate.network;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import tud.seemuh.nfcgate.network.threading.ReceiveThread;
import tud.seemuh.nfcgate.network.threading.SendThread;

public class ServerConnection {
    public interface Callback {
        void onReceive(byte[] data);
        void onConnectionStatus();
    }

    // connection objects
    private Socket mSocket;
    private SendThread mSendThread;
    private ReceiveThread mReceiveThread;
    private Queue<byte[]> mSendQueue = new LinkedList<>();

    // metadata
    private Callback mCallback;
    private String mHostname;
    private int mPort;

    public ServerConnection(String hostname, int port) {
        mHostname = hostname;
        mPort = port;
    }

    public ServerConnection setCallback(Callback cb) {
        mCallback = cb;

        return this;
    }

    /**
     * Connects to the socket, enables async I/O
     */
    public ServerConnection connect() {
        try {
            mSocket = new Socket(mHostname, mPort);
            mSocket.setTcpNoDelay(true);
        }
        catch (Exception e) {
            // print error to log and inform callback
            e.printStackTrace();
            reportStatus();
        }

        // I/O threads
        if (mSocket != null) {
            mSendThread = new SendThread(mSendQueue, mSocket);
            mReceiveThread = new ReceiveThread(mCallback, mSocket);
            mSendThread.run();
            mReceiveThread.run();
        }

        return this;
    }

    /**
     * Closes the connection and releases all resources
     */
    public void disconnect() {
        try {
            if (mSendThread != null)
                mSendThread.interrupt();

            if (mReceiveThread != null)
                mReceiveThread.interrupt();

            if (mSocket != null)
                mSocket.close();
        }
        catch (IOException e) {
            // TODO: error handling
            reportStatus();
        }
    }

    /**
     * Schedules the data to be sent
     */
    public void send(byte[] data) {
        mSendQueue.add(data);
    }

    /**
     * Reports a status to the callback if set
     * TODO: status enum
     */
    private void reportStatus() {
        if (mCallback != null)
            mCallback.onConnectionStatus();
    }
}
