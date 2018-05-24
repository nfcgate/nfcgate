package tud.seemuh.nfcgate.network;

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
    private final Object mSocketLock = new Object();

    // threading
    private SendThread mSendThread;
    private ReceiveThread mReceiveThread;
    private Queue<SendRecord> mSendQueue = new LinkedList<>();

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
        // I/O threads
        mSendThread = new SendThread(this);
        mReceiveThread = new ReceiveThread(this);
        mSendThread.start();
        mReceiveThread.start();
        return this;
    }

    /**
     * Closes the connection and releases all resources
     */
    public void disconnect() {
        if (mSendThread != null)
            mSendThread.interrupt();

        if (mReceiveThread != null)
            mReceiveThread.interrupt();
    }

    /**
     * Schedules the data to be sent
     */
    public void send(int session, byte[] data) {
        mSendQueue.add(new SendRecord(session, data));
    }

    public Socket getSocket() {
        if (mSocket == null)
            createSocket();

        return mSocket;
    }

    private void createSocket() {
        synchronized (mSocketLock) {
            if (mSocket == null) {
                try {
                    mSocket = new Socket(mHostname, mPort);
                    mSocket.setTcpNoDelay(true);
                } catch (Exception e) {
                    // print error to log and inform callback
                    e.printStackTrace();
                    reportStatus();
                }
            }
        }
    }

    public void onReceive(byte[] data) {
        mCallback.onReceive(data);
    }

    public Queue<SendRecord> getSendQueue() {
        return mSendQueue;
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
