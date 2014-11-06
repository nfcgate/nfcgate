package tud.seemuh.nfcgate.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by daniel on 11/6/14.
 */
public class SimpleNetworkConnectionServer {

    private int mServerPort = 15000;

    private ServerThread mRunnableServerThread;
    private Thread mServerThread = null;

    private Handler mUpdateviewHandler;

    /**
     * Constructor, create listen thread
     * @param serverport
     */
    //public SimpleNetworkConnectionServer(Handler handler, int serverport) {
    public SimpleNetworkConnectionServer(int serverport) {
        //mUpdateviewHandler = handler;
        mServerPort = serverport;

        Log.d(SimpleNetworkConnectionServer.class.getName(), "SimpleNetworkConnectionServer Constructor");
        if(mServerThread == null) {
            mRunnableServerThread = new ServerThread();
            mServerThread = new Thread(mRunnableServerThread);
            mServerThread.start();
        } else {
            Log.d(SimpleNetworkConnectionServer.class.getName(), "Server already started");
        }
    }

    public void tearDown() {
        mRunnableServerThread.tearDown();
        mServerThread.interrupt();
    }


    private class ServerThread implements Runnable {

        private ServerSocket serverSocket;
        private CommunicationThread mRunnableComThread;
        private Thread commThread;

        public void tearDown() {
            Log.d(ServerThread.class.getName(), "about to stop ServerThread");

            if(commThread != null) {
                commThread.interrupt();
            }

            try {
                serverSocket.close();
            } catch (IOException e) {
                //TODO
                e.printStackTrace();
            }
        }

        public void run() {

            Log.d(ServerThread.class.getName(), "started new ServerThread");

            Socket socket = null;
            try {
                serverSocket = new ServerSocket(mServerPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    socket = serverSocket.accept();

                    //we are connected, lets handle the incoming connection
                    mRunnableComThread = new CommunicationThread(socket);
                    commThread = new Thread(mRunnableComThread);
                    commThread.start();

                } catch (IOException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }
    }

    private class CommunicationThread implements Runnable {

        private Socket mClientSocket;

        private BufferedReader mBufReader;

        public CommunicationThread(Socket clientSocket) {

           mClientSocket = clientSocket;

            try {
                //create new buffer for reading
                mBufReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));

            } catch (IOException e) {
                //TODO
                e.printStackTrace();
            }
        }

        public void run() {

            Log.d(CommunicationThread.class.getName(), "started new CommunicationThread");

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    //read one line from socket
                    String read = mBufReader.readLine();

                    if(read != null) {
                        //mUpdateviewHandler.post(new updateUIThread(read));

                        Log.d(CommunicationThread.class.getName(), "Got Message: " + read);
                    }

                } catch (IOException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }

    }

    private class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            //TODO Update UI
            //TextView text = (TextView) findViewById(R.id.text2);
            //text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
        }
    }
}
