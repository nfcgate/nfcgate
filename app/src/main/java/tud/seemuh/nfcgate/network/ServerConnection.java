package tud.seemuh.nfcgate.network;

import android.util.Log;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.network.threading.ReceiveThread;
import tud.seemuh.nfcgate.network.threading.SendThread;
import tud.seemuh.nfcgate.util.NfcComm;

public class ServerConnection {
    private static final String TAG = "ServerConnection";

    public interface Callback {
        void onReceive(byte[] data);
        void onNetworkStatus(NetworkStatus status);
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

    ServerConnection(String hostname, int port) {
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
        Log.v(TAG, "Enqueuing message of " + data.length + " bytes");
        mSendQueue.add(new SendRecord(session, data));
    }

    /**
     * Called by threads to get socket
     */
    public Socket getSocket() {
        synchronized (mSocketLock) {
            if (mSocket == null)
                createSocket();

            return mSocket;
        }
    }

    /**
     * Called by threads to create socket
     */
    private void createSocket() {
        // double-check to prevent race conditions
        Log.v(TAG, "Creating socket. mSocket = " + mSocket);
        if (mSocket == null) {
            try {
                mSocket = new Socket();
                mSocket.connect(new InetSocketAddress(mHostname, mPort), 10000);
                mSocket.setTcpNoDelay(true);

                reportStatus(NetworkStatus.CONNECTED);
            } catch (Exception e) {
                Log.e(TAG, "Socket cannot connect", e);
                mSocket = null;
            }
        }
    }

    /**
     * ReceiveThread delivers data
     */
    public void onReceive(byte[] data) {
        mCallback.onReceive(data);
    }

    /**
     * SendThread accesses sendQueue
     */
    public Queue<SendRecord> getSendQueue() {
        return mSendQueue;
    }

    /**
     * Reports a status to the callback if set
     */
    public void reportStatus(NetworkStatus status) {
        mCallback.onNetworkStatus(status);
    }
}
