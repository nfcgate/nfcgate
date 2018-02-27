package tud.seemuh.nfcgate.network;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.network.c2c.C2C;
import tud.seemuh.nfcgate.network.c2s.C2S;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper.MessageCase;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.UpdateUI;

/**
 * The HighLevelProtobufHandler is an implementation of the HighLevelNetworkHandler interface.
 * It is used to control all network communication and uses a LowLevelNetworkHandler for the actual
 * network communication. In this handler and the respective Callback implementation (ProtobufCallback
 * in our case), the protocol itself is implemented.
 *
 * The HighLevelProtobufHandler holds the state of the network connection and is responsible for taking down all
 * relevant threads once the connection is disconnected, be it by request of the user or by a
 * general connection loss.
 */
public class HighLevelProtobufHandler implements HighLevelNetworkHandler {
    private final static String TAG = "ProtobufHandler";

    private final static String CONN_CONNECTED = "Connected";
    private final static String CONN_DISCONNECTED = "Disconnected";
    private final static String CONN_SESSION = "In Session - Token ";
    private final static String CONN_WAITING_SESSION = "Waiting for Session";
    private final static String CONN_LEAVING_SESSION = "Leaving Session";
    private final static String CONN_SESSION_JOIN_FAILED = "Session join failed";
    private final static String CONN_SESSION_CREATION_FAILED = "Session creation failed";
    private final static String CONN_DIED = "Connection died";

    private final static String PEER_NO_SESSION = "No session";
    private final static String PEER_NOT_CONNECTED = "Not connected to server";
    private final static String PEER_WAITING_FOR_PARTNER = "Waiting for partner";
    private final static String PEER_CONNECTED = "Connected";
    private final static String PEER_CONNECTED_CARD = "Connected to Card";
    private final static String PEER_CONNECTED_APDU = "Connected to Reader";
    private final static String PEER_UNKNOWN = "Unknown";
    private final static String PEER_CONN_DIED = "Connection broke down";

    private LowLevelNetworkHandler handler;
    private static HighLevelProtobufHandler mInstance = null;
    private String secret;

    private enum Status {
        NOT_CONNECTED,
        CONNECTED_NO_SESSION,
        SESSION_CREATE_SENT,
        SESSION_JOIN_SENT,
        WAITING_FOR_PARTNER,
        SESSION_READY,
        SESSION_LEAVE_SENT,
        PARTNER_APDU_MODE,
        PARTNER_READER_MODE
    }

    private Status status;
    private boolean leaving = false;

    private Callback mCallbackInstance;

    private TextView debugView;
    private TextView connectionStatusView;
    private TextView peerStatusView;

    private Button resetButton;
    private Button connectButton;
    private Button joinButton;
    private Button abortButton;

    private NfcManager mNfcManager;


    public HighLevelProtobufHandler() {
        status = Status.NOT_CONNECTED;
    }


    // Helper functions
    public static HighLevelProtobufHandler getInstance() {
        if(mInstance == null) mInstance = new HighLevelProtobufHandler();
        return mInstance;
    }

    public Callback getCallback() {
        return mCallbackInstance;
    }

    public void setDebugView(TextView ldebugView) {
        debugView = ldebugView;
    }

    public void setConnectionStatusView(TextView connStatusView) {
        connectionStatusView = connStatusView;
    }

    public void setPeerStatusView(TextView view) {
        peerStatusView = view;
    }

    public void setButtons(Button Reset, Button ConnectTo, Button Abort, Button Join) {
        resetButton = Reset;
        connectButton = ConnectTo;
        abortButton = Abort;
        joinButton = Join;
    }

    public void setNfcManager(NfcManager nfcManager) {
        mNfcManager = nfcManager;
    }

    @Override
    public void setCallback(Callback mCallback) {
        mCallbackInstance = mCallback;
        if (mNfcManager != null) {
            mCallbackInstance.setNfcManager(mNfcManager);
        } else {
            Log.e(TAG, "setCallback: mNfcManager is not set (yet). Not passing reference to callback");
        }
    }

    private void appendDebugOutput(String output) {
        new UpdateUI(debugView, UpdateUI.UpdateMethod.appendTextView).execute(output + "\n");
    }

    private void setConnectionStatusOutput(String output) {
        new UpdateUI(connectionStatusView, UpdateUI.UpdateMethod.setTextTextView).execute("Server Status: " + output);
    }

    private void setPeerStatusOutput(String output) {
        new UpdateUI(peerStatusView, UpdateUI.UpdateMethod.setTextTextView).execute("Partner Status: " + output);
    }

    private void reactivateButtons() {
        // We need to pass a parameter, even though it isn't used. Otherwise, the app will crash.
        new UpdateUI(connectButton, UpdateUI.UpdateMethod.enableButton).execute("Unfug");
        new UpdateUI(joinButton, UpdateUI.UpdateMethod.enableButton).execute("Unfug");
        new UpdateUI(abortButton, UpdateUI.UpdateMethod.disableButton).execute("Unfug");
    }

    private void setButtonTexts() {
        new UpdateUI(connectButton, UpdateUI.UpdateMethod.setTextButton).execute(MainActivity.createSessionMessage);
        new UpdateUI(joinButton, UpdateUI.UpdateMethod.setTextButton).execute(MainActivity.joinSessionMessage);
        new UpdateUI(resetButton, UpdateUI.UpdateMethod.setTextButton).execute(MainActivity.resetMessage);
    }

    // Network message building and sending
    private C2S.Data wrapAsDataMessage(byte[] msg) {
        // Prepare Data Message
        C2S.Data.Builder dataMsg = C2S.Data.newBuilder();

        // Set the byte field
        dataMsg.setBlob(ByteString.copyFrom(msg));

        // Set error code to "no error"
        dataMsg.setErrcode(C2S.Data.DataErrorCode.ERROR_NOERROR);

        return dataMsg.build();
    }

    private void sendMessage(Message msg, MessageCase mcase) {
        if (status == Status.NOT_CONNECTED) {
            Log.e(TAG, "sendMessage: Trying to send message while not connected. Failed, doing nothing");
            return;
        }

        // At first glance, the following strategy may seem inefficient. Why do we create a wrapper
        // message, pack it into a data message, and then pack that data message into another
        // wrapper message? The answer is easy: Originally, we intended to implement cryptographic
        // protection for all messages. This would only be possible if we can send arbitrary bytes
        // in a protobuf message. Hence, the data message was born.
        // Due to time constraints, the idea of encrypting all packets was shelved for version 1.0.
        // However, the protocol was already implemented serverside, so changing everything would
        // have implied a rewrite of the server and the associated test cases. As we still intend
        // to add cryptographic protection at some point in the future, we opted to use this
        // slightly inefficient way of handling this problem.
        // As protobuf invocations are quite cheap, computationally speaking, and the network
        // latency is quite large in comparison, the extra computational time used here should
        // not make a large difference overall.

        // Prepare a wrapper message
        Wrapper.Builder WrapperMsg = Wrapper.newBuilder();

        // Set the relevant message field
        if (mcase == MessageCase.NFCDATA) {
            // This is a C2C message, wrap as data message
            // First, pack in a Wrapper message to indicate what type the inner message is
            Wrapper.Builder innerWrapperMsg = Wrapper.newBuilder();
            innerWrapperMsg.setNFCData((C2C.NFCData) msg);

            // Now, pack the inner wrapper message in a data message
            C2S.Data data = wrapAsDataMessage(innerWrapperMsg.build().toByteArray());

            // Finally, pack the data message in an outer wrapper message
            WrapperMsg.setData(data);

        } else if (mcase == MessageCase.SESSION) {
            // This is a C2S data, no need to wrap as data message
            WrapperMsg.setSession((C2S.Session) msg);

        } else if (mcase == MessageCase.STATUS) {
            // This is a C2C message, wrap as data message as before
            Wrapper.Builder innerWrapperMsg = Wrapper.newBuilder();
            innerWrapperMsg.setStatus((C2C.Status) msg);
            C2S.Data data = wrapAsDataMessage(innerWrapperMsg.build().toByteArray());
            WrapperMsg.setData(data);

        } else if (mcase == MessageCase.ANTICOL) {
            // This is a C2C message, wrap as data message as before
            Wrapper.Builder innerWrapperMsg = Wrapper.newBuilder();
            innerWrapperMsg.setAnticol((C2C.Anticol) msg);
            C2S.Data data = wrapAsDataMessage(innerWrapperMsg.build().toByteArray());
            WrapperMsg.setData(data);

        } else {
            Log.e(TAG, "sendMessage: Unknown Message type: " + mcase);
            return;
            // This should never happen...
        }

        // Build and serialize the message
        byte[] msgbytes = WrapperMsg.build().toByteArray();

        // Send the message
        if (handler != null) {
            handler.sendBytes(msgbytes);
        } else {
            Log.e(TAG, "sendMessage: Trying to send message without connected handler.");
        }
    }

    /**
     * Send a status message with the provided status code.
     * @param code The Status code as defined in the Enum C2C.Status.StatusCode
     */
    private void sendStatusMessage(C2C.Status.StatusCode code) {
        // Create error message
        C2C.Status.Builder ErrorMsg = C2C.Status.newBuilder();
        ErrorMsg.setCode(code);

        // Send message
        sendMessage(ErrorMsg.build(), MessageCase.STATUS);
    }

    // Connection management

    /**
     * Connect to the provided address and port
     * @param addr String containing the IP we want to connect to
     * @param port integer containing the target port (usually 5566)
     * @return The connected HighLevelProtobufHandler instance
     */
    @Override
    public HighLevelNetworkHandler connect(String addr, int port) {
        mNfcManager.start();
        handler = LowLevelTCPHandler.getInstance().connect(addr, port);
        handler.setCallback(mCallbackInstance);
        status = Status.CONNECTED_NO_SESSION;
        setConnectionStatusOutput(CONN_CONNECTED);
        setPeerStatusOutput(PEER_NO_SESSION);
        leaving = false;
        return this;
    }

    /**
     * Disconnect from an existing network connection due to user request
     * (or do nothing if we are not connected)
     */
    @Override
    public void disconnect() {
        leaveSession(); // Ensure we are no longer in a session
        leaving = true;
        setConnectionStatusOutput(CONN_DISCONNECTED);
        setPeerStatusOutput(PEER_NOT_CONNECTED);
        disconnectCommon();
    }

    /**
     * Disconnect due to a broken down network connection
     */
    @Override
    public void disconnectBrokenPipe() {
        setConnectionStatusOutput(CONN_DIED);
        setPeerStatusOutput(PEER_CONN_DIED);
        disconnectCommon();
    }

    /**
     * Common code for disconnect() and disconnectBrokenPipe()
     */
    private void disconnectCommon() {
        // Disconnect network
        if (handler != null) handler.disconnect();
        status = Status.NOT_CONNECTED;
        mCallbackInstance.shutdown();
        // Set default UI state
        setButtonTexts();
        reactivateButtons();
        // Stop NfcManager
        mNfcManager.shutdown();
    }

    /**
     * Terminate the workaround for a specific Broadcom card, if it is running.
     */
    @Override
    public void disconnectCardWorkaround() {
        mNfcManager.stopWorkaround();
        new UpdateUI(resetButton, UpdateUI.UpdateMethod.setTextButton).execute(MainActivity.resetMessage);
    }

    /**
     * Notify the HighLevelProtobufHandler that the card workaround has been started.
     */
    @Override
    public void notifyCardWorkaroundConnected() {
        new UpdateUI(resetButton, UpdateUI.UpdateMethod.setTextButton).execute(MainActivity.resetCardMessage);
    }

    // Session management
    @Override
    public void createSession() {
        if (status != Status.CONNECTED_NO_SESSION) {
            Log.e(TAG, "createSession: Trying to create session while not in CONNECTED_NO_SESSION state. Doing nothing");
            return;
        }
        Log.d(TAG, "createSession: Trying to create session");
        // Create a message builder and fill in the relevant data
        C2S.Session.Builder sessionMessage = C2S.Session.newBuilder();
        sessionMessage.setOpcode(C2S.Session.SessionOpcode.SESSION_CREATE);
        sessionMessage.setErrcode(C2S.Session.SessionErrorCode.ERROR_NOERROR);

        // Send the message
        sendMessage(sessionMessage.build(), MessageCase.SESSION);
        status = Status.SESSION_CREATE_SENT;
        setConnectionStatusOutput(CONN_WAITING_SESSION);
    }

    @Override
    public void joinSession(String secretToken) {
        if (status != Status.CONNECTED_NO_SESSION) {
            Log.e(TAG, "joinSession: Trying to join session while not in CONNECTED_NO_SESSION state. Doing nothing");
            return;
        }
        secret = secretToken;
        Log.d(TAG, "joinSession: Trying to join session with secret " + secret);
        // Create a message builder and fill in the relevant data
        C2S.Session.Builder sessionMessage = C2S.Session.newBuilder();
        sessionMessage.setOpcode(C2S.Session.SessionOpcode.SESSION_JOIN);
        sessionMessage.setSessionSecret(secret);
        sessionMessage.setErrcode(C2S.Session.SessionErrorCode.ERROR_NOERROR);

        // Send the message
        sendMessage(sessionMessage.build(), MessageCase.SESSION);
        secret = secretToken;
        status = Status.SESSION_JOIN_SENT;
        setConnectionStatusOutput(CONN_WAITING_SESSION);
    }

    @Override
    public void leaveSession() {
        if (status != Status.PARTNER_APDU_MODE
                && status != Status.PARTNER_READER_MODE
                && status != Status.SESSION_READY
                && status != Status.WAITING_FOR_PARTNER) {
            Log.w(TAG, "leaveSession: Trying to leave session while not in a session. Doing nothing");
            return;
        }
        Log.d(TAG, "leaveSession: Trying to leave session with secret " + secret);
        // Create a message builder and fill in the relevant data
        C2S.Session.Builder sessionMessage = C2S.Session.newBuilder();
        sessionMessage.setOpcode(C2S.Session.SessionOpcode.SESSION_LEAVE);
        sessionMessage.setSessionSecret(secret);
        sessionMessage.setErrcode(C2S.Session.SessionErrorCode.ERROR_NOERROR);

        // Send the message
        sendMessage(sessionMessage.build(), MessageCase.SESSION);
        status = Status.SESSION_LEAVE_SENT;
        setConnectionStatusOutput(CONN_LEAVING_SESSION);
    }

    // NFC Message passing
    @Override
    public void sendAPDUMessage(NfcComm nfcdata) {
        if (nfcdata.getType() != NfcComm.Type.NFCBytes) {
            Log.e(TAG, "sendApduMessage: NfcComm object does not contain NFC bytes. Doing nothing.");
            return;
        }
        if (status != Status.PARTNER_READER_MODE) {
            Log.e(TAG, "sendAPDUMessage: Trying to send APDU message to partner who is not in reader mode. Doing nothing.");
            return;
        }
        byte[] apdu = nfcdata.getData();
        // Prepare message
        C2C.NFCData.Builder apduMessage = C2C.NFCData.newBuilder();
        apduMessage.setDataSource(C2C.NFCData.DataSource.READER);
        apduMessage.setDataBytes(ByteString.copyFrom(apdu));

        // Send prepared message
        sendMessage(apduMessage.build(), MessageCase.NFCDATA);
    }

    @Override
    public void sendAPDUReply(NfcComm nfcdata) {
        if (nfcdata.getType() != NfcComm.Type.NFCBytes) {
            Log.e(TAG, "sendApduReply: NfcComm object does not contain NFC bytes. Doing nothing.");
            return;
        }
        if (status != Status.PARTNER_APDU_MODE) {
            Log.e(TAG, "sendAPDUReply: Trying to send APDU reply to partner who is not in APDU mode. Doing nothing.");
            return;
        }
        byte[] nfcbytes = nfcdata.getData();

        // Build reply Protobuf
        C2C.NFCData.Builder reply = C2C.NFCData.newBuilder();
        reply.setDataBytes(ByteString.copyFrom(nfcbytes));
        reply.setDataSource(C2C.NFCData.DataSource.CARD);

        // Send reply
        sendMessage(reply.build(), MessageCase.NFCDATA);
    }

    @Override
    public void sendAnticol(NfcComm nfcdata) {
        if (nfcdata.getType() != NfcComm.Type.AnticolBytes) {
            Log.e(TAG, "sendAnticol: NfcComm object does not contain Anticol bytes. Doing nothing.");
            return;
        }

        // Retrieve values
        byte[] config = nfcdata.getConfig().build();

        // Build reply protobuf
        C2C.Anticol.Builder b = C2C.Anticol.newBuilder();
        b.setCONFIG(ByteString.copyFrom(config));

        // TODO If we aren't in a session, cache this and send it as soon as a session is established?
        // (And delete it if the card is removed in the meantime)
        sendMessage(b.build(), MessageCase.ANTICOL);
        Log.d(TAG, "sendAnticol: Sent Anticol message");
    }

    // Session status management
    @Override
    public void confirmSessionCreation(String secretToken) {
        Log.d(TAG, "confirmSessionCreation: Session created with token " + secretToken);
        secret = secretToken;
        status = Status.WAITING_FOR_PARTNER;
        setConnectionStatusOutput(CONN_SESSION + secretToken);
        setPeerStatusOutput(PEER_WAITING_FOR_PARTNER);
    }

    @Override
    public void confirmSessionJoin() {
        Log.d(TAG, "confirmSessionJoin: Session joined.");
        status = Status.SESSION_READY;
        setConnectionStatusOutput(CONN_SESSION + secret);
        setPeerStatusOutput(PEER_CONNECTED);
    }

    @Override
    public void confirmSessionLeave() {
        if (leaving) {
            leaving = false;
            return; // Do nothing if we are closing the connection
        }
        status = Status.CONNECTED_NO_SESSION;
        setConnectionStatusOutput(CONN_CONNECTED);
        setPeerStatusOutput(PEER_NO_SESSION);
    }

    @Override
    public void sessionPartnerJoined() {
        status = Status.SESSION_READY;
        setPeerStatusOutput(PEER_CONNECTED);
    }

    @Override
    public void sessionPartnerLeft() {
        status = Status.WAITING_FOR_PARTNER;
        setPeerStatusOutput(PEER_WAITING_FOR_PARTNER);
    }

    @Override
    public void sessionPartnerReaderModeOn() {
        status = Status.PARTNER_READER_MODE;
        setPeerStatusOutput(PEER_CONNECTED_CARD);
    }

    @Override
    public void sessionPartnerAPDUModeOn() {
        status = Status.PARTNER_APDU_MODE;
        setPeerStatusOutput(PEER_CONNECTED_APDU);
    }

    @Override
    public void sessionPartnerReaderModeOff() {
        status = Status.SESSION_READY;
        setPeerStatusOutput(PEER_CONNECTED);
    }

    @Override
    public void sessionPartnerAPDUModeOff() {
        status = Status.SESSION_READY;
        setPeerStatusOutput(PEER_CONNECTED);
    }

    @Override
    public void sessionPartnerNFCLost() {
        status = Status.SESSION_READY;
        setPeerStatusOutput(PEER_CONNECTED);
        appendDebugOutput("Partner lost NFC connection");
    }

    @Override
    public void sessionCreateFailed(C2S.Session.SessionErrorCode errcode) {
        status = Status.CONNECTED_NO_SESSION;
        setConnectionStatusOutput(CONN_SESSION_CREATION_FAILED);
        if (errcode == C2S.Session.SessionErrorCode.ERROR_CREATE_ALREADY_HAS_SESSION) {
            appendDebugOutput("Session creation failed: Already in a session");
        } else if (errcode == C2S.Session.SessionErrorCode.ERROR_CREATE_UNKOWN) {
            appendDebugOutput("Session creation failed: Unknown server error");
        } else {
            appendDebugOutput("Session creation failed, unknown error code sent");
        }
        setButtonTexts();
        reactivateButtons();
    }

    @Override
    public void sessionJoinFailed(C2S.Session.SessionErrorCode errcode) {
        status = Status.CONNECTED_NO_SESSION;
        setConnectionStatusOutput(CONN_SESSION_JOIN_FAILED);
        if (errcode == C2S.Session.SessionErrorCode.ERROR_JOIN_ALREADY_HAS_SESSION) {
            appendDebugOutput("Session join failed: Already in a session");
        } else if (errcode == C2S.Session.SessionErrorCode.ERROR_JOIN_UNKNOWN) {
            appendDebugOutput("Session join failed: Unknown server error");
        } else if (errcode == C2S.Session.SessionErrorCode.ERROR_JOIN_UNKNOWN_SECRET) {
            appendDebugOutput("Session join failed: Unknown secret (Session does not exist)");
        } else {
            appendDebugOutput("Session join failed, unknown error code sent");
        }
        setButtonTexts();
        reactivateButtons();
    }

    @Override
    public void sessionLeaveFailed(C2S.Session.SessionErrorCode errcode) {
        status = Status.WAITING_FOR_PARTNER;
        // TODO This may result in an inconsistent state
        // But as "Session leave failed" is a very rare message, that's probably fine for now.
        if (errcode == C2S.Session.SessionErrorCode.ERROR_LEAVE_NOT_JOINED) {
            appendDebugOutput("Session leave failed: Not in a session");
            setConnectionStatusOutput(CONN_CONNECTED);
            setPeerStatusOutput(PEER_NO_SESSION);
        } else if (errcode == C2S.Session.SessionErrorCode.ERROR_LEAVE_UNKNOWN_SECRET) {
            appendDebugOutput("Session leave failed: Unknown secret");
            setConnectionStatusOutput(CONN_CONNECTED);
            setPeerStatusOutput(PEER_NO_SESSION);
        } else if (errcode == C2S.Session.SessionErrorCode.ERROR_LEAVE_UNKNOWN) {
            appendDebugOutput("Session leave failed: Unknown server error");
            setConnectionStatusOutput(CONN_CONNECTED);
            setPeerStatusOutput(PEER_UNKNOWN);
        } else {
            appendDebugOutput("Session leave failed, unknown error code sent");
        }
    }

    // Notification Messages
    @Override
    public void notifyReaderFound() {
        sendStatusMessage(C2C.Status.StatusCode.READER_FOUND);
    }

    @Override
    public void notifyCardFound() {
        sendStatusMessage(C2C.Status.StatusCode.CARD_FOUND);
    }
    // TODO If we aren't in a session, cache this and send it as soon as a session is established?
    // (And delete it if the card is removed in the meantime)

    @Override
    public void notifyReaderRemoved() {
        sendStatusMessage(C2C.Status.StatusCode.READER_REMOVED);
    }

    @Override
    public void notifyCardRemoved() {
        sendStatusMessage(C2C.Status.StatusCode.CARD_REMOVED);
    }

    @Override
    public void notifyInvalidMsgFormat() {
        sendStatusMessage(C2C.Status.StatusCode.INVALID_MSG_FMT);
    }

    @Override
    public void notifyNotImplemented() {
        sendStatusMessage(C2C.Status.StatusCode.NOT_IMPLEMENTED);
    }

    @Override
    public void notifyUnknownMessageType() {
        sendStatusMessage(C2C.Status.StatusCode.UNKNOWN_MESSAGE);
    }

    @Override
    public void notifyUnknownError() {
        sendStatusMessage(C2C.Status.StatusCode.UNKNOWN_ERROR);
    }

    @Override
    public void notifyNFCNotConnected() {
        sendStatusMessage(C2C.Status.StatusCode.NFC_NO_CONN);
    }

    @Override
    public void sendKeepaliveMessage() {
        sendStatusMessage(C2C.Status.StatusCode.KEEPALIVE_REQ);
    }

    @Override
    public void sendKeepaliveReply() {
        sendStatusMessage(C2C.Status.StatusCode.KEEPALIVE_REP);
    }
}