package tud.seemuh.nfcgate.network;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import tud.seemuh.nfcgate.util.Utils;

/**
 * Created by daniel on 11/6/14.
 */
public class SimpleNetworkConnectionClientImpl implements NetworkHandler {

    private int mServerPort = 15000;
    private InetAddress mServerAddress;

    private ClientThread mRunnableClientThread;
    private Thread mClientThread = null;

    private Socket mSocket;

    public SimpleNetworkConnectionClientImpl(String serverAddress, int serverPort) {
        try {
            mServerAddress = InetAddress.getByName(serverAddress);

            if(mClientThread == null) {
                mRunnableClientThread = new ClientThread();
                mClientThread = new Thread(mRunnableClientThread);
                mClientThread.start();
            } else {
                Log.d(SimpleNetworkConnectionClientImpl.class.getName(), "Client thread already started");
            }

        } catch (UnknownHostException e1){
            Log.e(SimpleNetworkConnectionClientImpl.class.getName(), "Unknown Host: "+serverAddress);
        }
        mServerPort = serverPort;
    }

    public synchronized byte[] getBytes() {
        return mRunnableClientThread.getBytesFromNW();
    }

    public void sendBytes(byte[] msg) {
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

    private class ClientThread implements Runnable {

        private CommunicationThread mRunnableComThread;
        private Thread commThread = null;

        @Override
        public void run() {

            try {
                mSocket = new Socket(mServerAddress, mServerPort);

                //read answer from SOCKET
                mRunnableComThread = new CommunicationThread(mSocket);
                commThread = new Thread(mRunnableComThread);
                commThread.start();

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        public synchronized byte[] getBytesFromNW() {
            byte[] ret;
            if(commThread != null && mRunnableComThread.getSome == true) {
                ret = mRunnableComThread.readBytes;
                mRunnableComThread.getSome = false;
                return ret;
            } else {
                return null;
            }
        }

    }

    private class CommunicationThread implements Runnable {

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
                        getSome = true;
                    } else {
                        Log.e(CommunicationThread.class.getName(), "Error no postive number of bytes: " + len);
                    }

                } catch (IOException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }

    }
}
