package tud.seemuh.nfcgate.network;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import tud.seemuh.nfcgate.network.c2c.C2C;
import tud.seemuh.nfcgate.network.c2s.C2S;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper.MessageCase;

public class NetHandler implements HighLevelNetworkHandler {
    private final static String TAG = "NetHandler";

    private LowLevelNetworkHandler handler;
    private static NetHandler mInstance = null;
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

    public NetHandler() {
        status = Status.NOT_CONNECTED;
    }


    // Helper functions
    public static NetHandler getInstance() {
        if(mInstance == null) mInstance = new NetHandler();
        return mInstance;
    }

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
            // TODO This should never happen...
        }

        // Build and serialize the message
        byte[] msgbytes = WrapperMsg.build().toByteArray();

        // Send the message
        if (handler != null) {
            handler.sendBytes(msgbytes);
        } else {
            Log.e(TAG, "sendMessage: Trying to send message without connected handler.");
            // TODO Give indication to caller?
        }
    }

    private void sendStatusMessage(C2C.Status.StatusCode code) {
        // Create error message
        C2C.Status.Builder ErrorMsg = C2C.Status.newBuilder();
        ErrorMsg.setCode(code);

        // Send message
        sendMessage(ErrorMsg.build(), MessageCase.STATUS);
    }

    // Connection management
    @Override
    public HighLevelNetworkHandler connect(String addr, int port) {
        handler = SimpleLowLevelNetworkConnectionClientImpl.getInstance().connect(addr, port);
        status = Status.CONNECTED_NO_SESSION;
        return this;
    }

    @Override
    public void disconnect() {
        // TODO Implement
        status = Status.NOT_CONNECTED;
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
    }

    @Override
    public void leaveSession() {
        if (status != Status.PARTNER_APDU_MODE
                && status != Status.PARTNER_READER_MODE
                && status != Status.SESSION_READY
                && status != Status.WAITING_FOR_PARTNER) {
            Log.e(TAG, "leaveSession: Trying to leave session while not in a session. Doing nothing");
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
    }

    // NFC Message passing
    @Override
    public void sendAPDUMessage(byte[] apdu) {
        if (status != Status.PARTNER_READER_MODE) {
            Log.e(TAG, "sendAPDUMessage: Trying to send APDU message to partner who is not in reader mode. Doing nothing.");
            return;
        }
        // Prepare message
        C2C.NFCData.Builder apduMessage = C2C.NFCData.newBuilder();
        apduMessage.setDataSource(C2C.NFCData.DataSource.READER);
        apduMessage.setDataBytes(ByteString.copyFrom(apdu));

        // Send prepared message
        sendMessage(apduMessage.build(), MessageCase.NFCDATA);

        // Log
        Log.d(TAG, "sendAPDUMessage: sent APDU message");
    }

    @Override
    public void sendAPDUReply(byte[] nfcdata) {
        if (status != Status.PARTNER_APDU_MODE) {
            Log.e(TAG, "sendAPDUReply: Trying to send APDU reply to partner who is not in APDU mode. Doing nothing.");
            return;
        }
        C2C.NFCData.Builder reply = C2C.NFCData.newBuilder();
        ByteString replyBytes = ByteString.copyFrom(nfcdata);
        reply.setDataBytes(replyBytes);
        reply.setDataSource(C2C.NFCData.DataSource.CARD);

        // Send reply
        sendMessage(reply.build(), MessageCase.NFCDATA);
    }

    // Session status management
    @Override
    public void confirmSessionCreation(String secretToken) {
        Log.d(TAG, "confirmSessionCreation: Session created with token " + secretToken);
        secret = secretToken;
        status = Status.WAITING_FOR_PARTNER;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void confirmSessionJoin() {
        status = Status.SESSION_READY;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void confirmSessionLeave() {
        status = Status.CONNECTED_NO_SESSION;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionPartnerJoined() {
        status = Status.SESSION_READY;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionPartnerLeft() {
        status = Status.WAITING_FOR_PARTNER;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionPartnerReaderModeOn() {
        status = Status.PARTNER_READER_MODE;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionPartnerAPDUModeOn() {
        status = Status.PARTNER_APDU_MODE;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionPartnerReaderModeOff() {
        status = Status.SESSION_READY;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionPartnerAPDUModeOff() {
        status = Status.SESSION_READY;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionPartnerNFCLost() {
        status = Status.SESSION_READY;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionCreateFailed(C2S.Session.SessionErrorCode errcode) {
        status = Status.CONNECTED_NO_SESSION;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionJoinFailed(C2S.Session.SessionErrorCode errcode) {
        status = Status.CONNECTED_NO_SESSION;
        notifyNotImplemented(); // TODO Implement
    }

    @Override
    public void sessionLeaveFailed(C2S.Session.SessionErrorCode errcode) {
        status = Status.WAITING_FOR_PARTNER; // TODO This may result in an inconsistent state. Handle that better
        notifyNotImplemented(); // TODO Implement
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