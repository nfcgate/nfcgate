package de.tu_darmstadt.seemoo.nfcgate.network;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManagerFix;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import de.tu_darmstadt.seemoo.nfcgate.gui.MainActivity;
import de.tu_darmstadt.seemoo.nfcgate.network.c2s.C2S;
import de.tu_darmstadt.seemoo.nfcgate.network.data.NetworkStatus;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

import static de.tu_darmstadt.seemoo.nfcgate.network.c2s.C2S.ServerData.Opcode;

public class NetworkManager implements ServerConnection.Callback {
    private static final String TAG = "NetworkManager";

    public interface Callback {
        void onReceive(NfcComm data);
        void onNetworkStatus(NetworkStatus status);
    }

    // references
    private MainActivity mActivity;
    private ServerConnection mConnection;
    private Callback mCallback;

    // preference data
    private String mHostname;
    private int mPort, mSessionNumber;

    public NetworkManager(MainActivity activity, Callback cb) {
        mActivity = activity;
        mCallback = cb;
    }

    public void connect() {
        // read fresh preference data
        loadPreferenceData();

        // disconnect old connection
        if (mConnection != null)
            disconnect();

        // establish connection
        mConnection = new ServerConnection(mHostname, mPort)
                .setCallback(this)
                .connect();

        // queue initial handshake message
        sendServer(Opcode.OP_SYN, null);
    }

    public void disconnect() {
        if (mConnection != null) {
            sendServer(Opcode.OP_FIN, null);
            mConnection.sync();
            mConnection.disconnect();
        }
    }

    public void send(NfcComm data) {
        // queue data message
        sendServer(Opcode.OP_PSH, data.toByteArray());
    }

    @Override
    public void onReceive(byte[] data) {
        C2S.ServerData serverData = null;
        try {
            serverData = C2S.ServerData.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            Log.e(TAG, "Message parsing failed", e);
            return;
        }

        Log.v(TAG, "Got message "+serverData.getOpcode().toString());
        switch (serverData.getOpcode()) {
            case OP_SYN:
                // empty syn message indicates our peer has just connected
                onNetworkStatus(NetworkStatus.PARTNER_CONNECT);
                // return ack
                sendServer(Opcode.OP_ACK, null);

                break;
            case OP_ACK:
                // empty ack message indicates our peer was already connected
                onNetworkStatus(NetworkStatus.PARTNER_CONNECT);

                break;
            case OP_FIN:
                // our peer has disconnected
                onNetworkStatus(NetworkStatus.PARTNER_LEFT);
                mConnection.disconnect();

                break;
            case OP_PSH:
                // pass data to callback
                mCallback.onReceive(new NfcComm(serverData.getData().toByteArray()));

                break;
        }
    }

    @Override
    public void onNetworkStatus(NetworkStatus status) {
        mCallback.onNetworkStatus(status);
    }

    private void loadPreferenceData() {
        // read data from shared prefs
        SharedPreferences prefs = PreferenceManagerFix.getDefaultSharedPreferences(mActivity);
        mHostname = prefs.getString("host", null);
        mPort = Integer.parseInt(prefs.getString("port", "0"));
        mSessionNumber = Integer.parseInt(prefs.getString("session", "0"));
    }

    private void sendServer(Opcode opcode, byte[] data) {
        mConnection.send(mSessionNumber,
                C2S.ServerData.newBuilder()
                    .setOpcode(opcode)
                    .setData(data == null ? ByteString.EMPTY : ByteString.copyFrom(data))
                    .build()
                    .toByteArray());
    }
}
