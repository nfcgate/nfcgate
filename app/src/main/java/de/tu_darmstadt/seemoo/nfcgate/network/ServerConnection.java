package de.tu_darmstadt.seemoo.nfcgate.network;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;
import de.tu_darmstadt.seemoo.nfcgate.network.data.SendRecord;
import de.tu_darmstadt.seemoo.nfcgate.network.threading.ReceiveThread;
import de.tu_darmstadt.seemoo.nfcgate.network.threading.SendThread;

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
    private final BlockingQueue<SendRecord> mSendQueue = new LinkedBlockingQueue<>();

    // metadata
    private Callback mCallback;
    private final String mHostname;
    private final int mPort;

    ServerConnection(String hostname, int port) {
        mHostname = hostname;
        mPort = port;
    }

    ServerConnection setCallback(Callback cb) {
        mCallback = cb;
        return this;
    }

    /**
     * Connects to the socket, enables async I/O
     */
    ServerConnection connect() {
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
    void disconnect() {
        if (mSendThread != null)
            mSendThread.interrupt();

        if (mReceiveThread != null)
            mReceiveThread.interrupt();
    }

    /**
     * Wait some time to allow sendQueue to be processed
     */
    void sync() {
        if (mSendQueue.peek() != null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) { }
        }
    }

    /**
     * Schedules the data to be sent
     */
    public void send(int session, byte[] data) {
        Log.v(TAG, "Enqueuing message of " + data.length + " bytes");
        mSendQueue.add(new SendRecord(session, data));
    }

    /**
     * Called by threads to open socket
     */
    public Socket openSocket() {
        synchronized (mSocketLock) {
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

            return mSocket;
        }
    }

    /**
     * Called by threads to close socket
     */
    public void closeSocket() {
        synchronized (mSocketLock) {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mSocket = null;
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
    public BlockingQueue<SendRecord> getSendQueue() {
        return mSendQueue;
    }

    /**
     * Reports a status to the callback if set
     */
    public void reportStatus(NetworkStatus status) {
        mCallback.onNetworkStatus(status);
    }
}
