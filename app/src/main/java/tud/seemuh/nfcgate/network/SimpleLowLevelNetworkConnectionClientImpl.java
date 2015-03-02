package tud.seemuh.nfcgate.network;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import tud.seemuh.nfcgate.util.Utils;


public class SimpleLowLevelNetworkConnectionClientImpl implements LowLevelNetworkHandler {

    private int mServerPort = 15000;
    private InetAddress mServerAddress;

    private ClientThread mRunnableClientThread;
    private Thread mClientThread = null;

    private Socket mSocket;
    private Callback mCallback;

    private static SimpleLowLevelNetworkConnectionClientImpl mInstance;

    public static SimpleLowLevelNetworkConnectionClientImpl getInstance() {
        if(mInstance == null) mInstance = new SimpleLowLevelNetworkConnectionClientImpl();
        return mInstance;
    }

    public SimpleLowLevelNetworkConnectionClientImpl connect(String serverAddress, int serverPort) {
        try {
            // TODO: Properly support Domain Names & IP Addresses as input --> I just included a catch for the exception
            // Resolve domain name to IP address as string
            InetAddress addr = null;
            addr = InetAddress.getByName(serverAddress);

            mServerAddress = (InetAddress.getByName(addr.getHostAddress()));

            if(mClientThread == null) {
                mRunnableClientThread = new ClientThread();
                mClientThread = new Thread(mRunnableClientThread);
                mClientThread.start();
            } else {
                Log.d(SimpleLowLevelNetworkConnectionClientImpl.class.getName(), "Client thread already started");
            }

        } catch (Exception e1){
            Log.e(SimpleLowLevelNetworkConnectionClientImpl.class.getName(), "Unknown Host: "+serverAddress);
        }
        mServerPort = serverPort;
        return this;
    }

    public SimpleLowLevelNetworkConnectionClientImpl setCallback(Callback callback) {
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

                //read answer from SOCKET
                mRunnableComThread = new CommunicationThread(mSocket);
                synchronized (mSendQueueSync) {
                    commThread = new Thread(mRunnableComThread);
                    commThread.start();
                    for(byte[] msg : mSendQueue) {
                        reallySendBytes(msg);
                    }
                    mSendQueue.clear();
                }

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
                if(commThread == null) {
                    mSendQueue.add(msg);
                    return;
                }
            }
            reallySendBytes(msg);
        }

        private void reallySendBytes(byte[] msg) {
            try {
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

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    BufferedInputStream dis = new BufferedInputStream(mClientSocket.getInputStream());
                    //read length of data from socket (should be 4 bytes long)
                    byte[] lenbytes = new byte[4];
                    dis.read(lenbytes);
                    int len = ByteBuffer.wrap(lenbytes).getInt();;

                    Log.i(TAG, "Reading bytes of length:" + len);

                    // read the message data
                    if (len > 0) {
                        readBytes = new byte[len];
                        dis.read(readBytes);
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
                    }

                } catch (IOException e) {
                    if (mCallback != null) {
                        mCallback.notifyBrokenPipe();
                        return;
                    }
                }
            }
            try {
                mClientSocket.close();
            } catch (IOException e) {
                // e.printStackTrace();
            }
            Log.i(TAG, "Shutting down");
            return;
        }

    }
}
