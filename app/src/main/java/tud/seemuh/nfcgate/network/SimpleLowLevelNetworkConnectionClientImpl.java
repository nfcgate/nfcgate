package tud.seemuh.nfcgate.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
        return mRunnableClientThread.getBytesFromNW();
    }

    public void sendBytes(byte[] msg) {
        mRunnableClientThread.sendBytes(msg);
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
            DataOutputStream out;
            try {
                out = new DataOutputStream(mSocket.getOutputStream());
                out.writeInt(msg.length);
                out.write(msg);
                out.flush();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

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

            Log.d(CommunicationThread.class.getName(), "started new CommunicationThread");

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    DataInputStream dis = new DataInputStream(mClientSocket.getInputStream());
                    //read length of data from socket (should be 4 bytes long)
                    int len = dis.readInt();

                    Log.i(CommunicationThread.class.getName(), "Reading bytes of length:" + len);

                    // read the message data
                    if (len > 0) {
                        readBytes = new byte[len];
                        dis.readFully(readBytes);
                        Log.d(CommunicationThread.class.getName(), "Read data: " + Utils.bytesToHex(readBytes));
                        if(mCallback != null) {
                            Log.d(CommunicationThread.class.getName(), "Delegating to Callback.");
                            mCallback.onDataReceived(readBytes);
                            Log.d(CommunicationThread.class.getName(), "Callback finished execution.");
                        }
                        else {
                            Log.i(CommunicationThread.class.getName(), "No callback set, saving for later");
                            getSome = true;
                        }
                    } else {
                        Log.e(CommunicationThread.class.getName(), "Error no postive number of bytes: " + len);
                    }

                } catch (IOException e) {
                    //TODO
                    // e.printStackTrace();
                }
            }
        }

    }
}
