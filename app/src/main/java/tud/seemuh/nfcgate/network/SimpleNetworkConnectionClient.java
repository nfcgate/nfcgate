package tud.seemuh.nfcgate.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.net.UnknownHostException;
import java.util.Arrays;

import android.util.Log;

import tud.seemuh.nfcgate.util.Utils;
import tud.seemuh.nfcgate.util.reader.NfcAReader;

/**
 * Created by daniel on 11/6/14.
 */
public class SimpleNetworkConnectionClient implements AbstractNetworkHandler {

    private int mServerPort = 15000;
    private InetAddress mServerAddress;

    private ClientThread mRunnableClientThread;
    private Thread mClientThread = null;

    private Socket mSocket;

    public SimpleNetworkConnectionClient(String serverAddress, int serverPort) {
        try {
            mServerAddress = InetAddress.getByName(serverAddress);

            if(mClientThread == null) {
                mRunnableClientThread = new ClientThread();
                mClientThread = new Thread(mRunnableClientThread);
                mClientThread.start();
            } else {
                Log.d(SimpleNetworkConnectionServer.class.getName(), "Client thread already started");
            }

        } catch (UnknownHostException e1){
            Log.e(SimpleNetworkConnectionClient.class.getName(), "Unknown Host: "+serverAddress);
        }
        mServerPort = serverPort;
    }

    public byte[] getBytes() {
        return mRunnableClientThread.getBytesFromNW();
    }

    public void sendBytes(byte[] msg) {
        try {
            //EditText et = (EditText) findViewById(R.id.EditText01);
            //String str = et.getText().toString();

            /*
            String str = "test string 123";
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(mSocket.getOutputStream())),
                    true);
            out.println(str);
            out.flush();*/
            mSocket.getOutputStream().write(msg);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {

        private CommunicationThread mRunnableComThread;
        private Thread commThread;

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

        public byte[] getBytesFromNW(){
            if(mRunnableComThread != null) {
                return mRunnableComThread.readBytes;
            } else {
                return null;
            }
        }

    }

    private class CommunicationThread implements Runnable {

        private Socket mClientSocket;

        private BufferedReader mBufReader;

        public byte[] readBytes = new byte[200];

        public CommunicationThread(Socket clientSocket) {

            mClientSocket = clientSocket;

            /*
            try {
                //create new buffer for reading
                mBufReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));

            } catch (IOException e) {
                //TODO
                e.printStackTrace();
            }*/
        }

        public void run() {

            Log.d(CommunicationThread.class.getName(), "started new CommunicationThread");

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    //read one line from socket

                    int read = mClientSocket.getInputStream().read(readBytes);
                    readBytes = Arrays.copyOf(readBytes, read);

                    if(read != -1) {
                        //mUpdateviewHandler.post(new updateUIThread(read));

                        Log.d(CommunicationThread.class.getName(), "Got Message: " + Utils.bytesToHex(readBytes));
                    } else {
                        Log.d(CommunicationThread.class.getName(), "Error!!!");
                        readBytes = null;
                    }

                } catch (IOException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }

    }
}
