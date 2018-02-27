package tud.seemuh.nfcgate.network;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import tud.seemuh.nfcgate.util.Utils;

/**
 * This class contains the logic for a low-level network connection with the server.
 * The class only sends and receives raw bytes, all the protocol logic and parsing happen in a
 * HighLevelNetworkHandler or Callback instance, respectively.
 *
 * This class contains two nested classes which implement the actual threads used for the network
 * communication.
 */
public class LowLevelTCPHandler implements LowLevelNetworkHandler {

    private int mServerPort = 15000;
    private InetAddress mServerAddress;

    private ClientThread mRunnableClientThread;
    private Thread mClientThread = null;

    private Socket mSocket;
    private Callback mCallback;

    private static LowLevelTCPHandler mInstance;

    public static LowLevelTCPHandler getInstance() {
        if(mInstance == null) mInstance = new LowLevelTCPHandler();
        return mInstance;
    }

    public LowLevelTCPHandler connect(String serverAddress, int serverPort) {
        try {
            // TODO: Properly support Domain Names & IP Addresses as input --> I just included a catch for the exception
            // Resolve domain name to IP address as string
            InetAddress addr;
            addr = InetAddress.getByName(serverAddress);

            mServerAddress = (InetAddress.getByName(addr.getHostAddress()));

            if(mClientThread == null) {
                mRunnableClientThread = new ClientThread();
                mClientThread = new Thread(mRunnableClientThread);
                mClientThread.start();
            } else {
                Log.d(LowLevelTCPHandler.class.getName(), "Client thread already started");
            }

        } catch (Exception e1){
            Log.e(LowLevelTCPHandler.class.getName(), "Unknown Host: "+serverAddress);
        }
        mServerPort = serverPort;
        return this;
    }

    public LowLevelTCPHandler setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    public synchronized byte[] getBytes() {
        if (mRunnableClientThread != null)
            return mRunnableClientThread.getBytesFromNW();
        else return null;
    }

    public void sendBytes(byte[] msg) {
        if (mRunnableClientThread != null) mRunnableClientThread.sendBytes(msg);
    }

    public void disconnect() {
        if (mRunnableClientThread != null) mRunnableClientThread.exitThread();
        mRunnableClientThread = null;
        mClientThread = null;
        mSocket = null;
    }

    private class ClientThread implements Runnable {

        private CommunicationThread mRunnableComThread;
        private Thread commThread = null;
        private List<byte[]> mSendQueue = new LinkedList<byte[]>();
        private final Object mSendQueueSync = new Object();

        @Override
        public void run() {

            try {
                mSocket = new Socket(mServerAddress, mServerPort);
                try {
                    mSocket.setTcpNoDelay(true);
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                //read answer from SOCKET
                mRunnableComThread = new CommunicationThread(mSocket);
                commThread = new Thread(mRunnableComThread);
                commThread.start();

                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    synchronized (mSendQueueSync) {
                        for (byte[] msg : mSendQueue) {
                            reallySendBytes(msg);
                        }
                        mSendQueue.clear();
                    }
                }

            } catch (ConnectException e1) {
                mCallback.notifyBrokenPipe();
                // TODO This is not very elegant but will do for now
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        public synchronized byte[] getBytesFromNW() {
            byte[] ret;
            if(commThread != null && mRunnableComThread.getSome) {
                ret = mRunnableComThread.readBytes;
                mRunnableComThread.getSome = false;
                return ret;
            } else {
                return null;
            }
        }

        public void sendBytes(byte[] msg) {
            synchronized (mSendQueueSync) {
                mSendQueue.add(msg);
            }
        }

        private void reallySendBytes(byte[] msg) {
            try {
                // The raw bytes sent are a 4-byte representation of the length of the following
                // data, followed by the actual data. Hence, we determine the length of the message,
                // append the message itself, and then send everything through the network socket.
                OutputStream out = mSocket.getOutputStream();
                byte[] len = ByteBuffer.allocate(4).putInt(msg.length).array();
                byte[] full = new byte[4 + msg.length];
                System.arraycopy(len, 0, full, 0, len.length);
                System.arraycopy(msg, 0, full, len.length, msg.length);
                out.write(full);
                out.flush();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void exitThread() {
            if (commThread != null) commThread.interrupt();
        }


    }

    private class CommunicationThread extends Observable implements Runnable {

        private Socket mClientSocket;
        protected volatile byte[] readBytes = null;
        protected volatile boolean getSome = false;

        public CommunicationThread(Socket clientSocket) {
            mClientSocket = clientSocket;
        }

        public void run() {
            String TAG = CommunicationThread.class.getName();

            Log.d(TAG, "started new CommunicationThread");

            try {
                BufferedInputStream dis = new BufferedInputStream(mClientSocket.getInputStream());
                while (!Thread.currentThread().isInterrupted()) {
                    // As noted before, all messages are preceded by four bytes containing an
                    // integer representation of the length of the following data, to make sure
                    // that we can read the correct number of bytes. Hence, we read four bytes
                    // in order to determine the length integer
                    byte[] lenbytes = new byte[4];
                    int rcvlen = dis.read(lenbytes);
                    Log.d(TAG, "Got " + rcvlen + " bytes");
                    int len = ByteBuffer.wrap(lenbytes).getInt();

                    // read the message data
                    if (len > 0) {
                        Log.i(TAG, "Reading bytes of length:" + len);
                        readBytes = new byte[len];
                        int read = 0;
                        do {
                            read += dis.read(readBytes, read, len-read);
                        } while(read < len);

                        Log.d(TAG, "Read data: " + Utils.bytesToHex(readBytes));
                        if(mCallback != null) {
                            Log.d(TAG, "Delegating to Callback.");
                            mCallback.onDataReceived(readBytes);
                            Log.d(TAG, "Callback finished execution.");
                        }
                        else {
                            Log.i(TAG, "No callback set, saving for later");
                            getSome = true;
                        }
                    } else {
                        Log.e(TAG, "Error no postive number of bytes: " + len);
                        throw new IOException("Protocol error: Length information was negative or null");
                    }
                }
            } catch (IOException e) {
                Log.i(TAG, "IOERROR ", e);
                if (mCallback != null) {
                    mCallback.notifyBrokenPipe();
                    return;
                }
            }
            try {
                mClientSocket.close();
            } catch (IOException e) {
                // e.printStackTrace();
            }
            Log.i(TAG, "Shutting down");
        }

    }
}
