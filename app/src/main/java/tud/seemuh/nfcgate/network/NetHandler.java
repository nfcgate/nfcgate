package tud.seemuh.nfcgate.network;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import tud.seemuh.nfcgate.network.c2c.C2C;
import tud.seemuh.nfcgate.network.c2s.C2S;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper.MessageCase;

public class NetHandler implements HighLevelNetworkHandler {
    private final static String TAG = "HighLevelNetworkHandler";

    private LowLevelNetworkHandler handler;
    private static NetHandler mInstance = null;


    // Helper functions
    public static NetHandler getInstance() {
        if(mInstance == null) mInstance = new NetHandler();
        return mInstance;
    }

    private void sendStatusMessage(C2C.Status.StatusCode code) {
        // Create error message
        C2C.Status.Builder ErrorMsg = C2C.Status.newBuilder();
        ErrorMsg.setCode(code);

        // Send message
        sendMessage(ErrorMsg.build(), MessageCase.STATUS);
    }

    public void sendMessage(Message msg, MessageCase mcase) {
        // Prepare a wrapper message
        Wrapper.Builder WrapperMsg = Wrapper.newBuilder();

        // Set the relevant message field
        if (mcase == MessageCase.DATA) {
            WrapperMsg.setData((C2S.Data) msg);
        } else if (mcase == MessageCase.KEX) {
            WrapperMsg.setKex((C2C.Kex) msg);
        } else if (mcase == MessageCase.NFCDATA) {
            WrapperMsg.setNFCData((C2C.NFCData) msg);
        } else if (mcase == MessageCase.SESSION) {
            WrapperMsg.setSession((C2S.Session) msg);
        } else if (mcase == MessageCase.STATUS) {
            WrapperMsg.setStatus((C2C.Status) msg);
        } else if (mcase == MessageCase.ANTICOL) {
            WrapperMsg.setAnticol((C2C.Anticol) msg);
        } else {
            Log.e(TAG, "Unknown Message type: " + mcase);
            // TODO This should never happen...
        }

        // Build and serialize the message
        byte[] msgbytes = WrapperMsg.build().toByteArray();

        // Send the message
        if (handler != null) {
            handler.sendBytes(msgbytes);
        } else {
            Log.e(TAG, "Trying to send message without connected handler.");
            // TODO Give indication to caller?
        }
    }

    @Override
    public HighLevelNetworkHandler connect(String addr, int port) {
        handler = SimpleLowLevelNetworkConnectionClientImpl.getInstance().connect(addr, port);
        return this;
    }

    @Override
    public void sendAPDUMessage(byte[] apdu) {
        // Prepare message
        C2C.NFCData.Builder apduMessage = C2C.NFCData.newBuilder();
        apduMessage.setDataSource(C2C.NFCData.DataSource.READER);
        apduMessage.setDataBytes(ByteString.copyFrom(apdu));

        // Send prepared message
        sendMessage(apduMessage.build(), MessageCase.NFCDATA);

        // Log
        Log.d(TAG, "sent APDU message");
    }

    @Override
    public void sendAPDUReply(byte[] nfcdata) {
        C2C.NFCData.Builder reply = C2C.NFCData.newBuilder();
        ByteString replyBytes = ByteString.copyFrom(nfcdata);
        reply.setDataBytes(replyBytes);
        reply.setDataSource(C2C.NFCData.DataSource.CARD);

        // Send reply
        sendMessage(reply.build(), MessageCase.NFCDATA);
    }

    @Override
    public void createSession() {
        Log.d(TAG, "createSession: Trying to create session");
        // Create a message builder and fill in the relevant data
        C2S.Session.Builder sessionMessage = C2S.Session.newBuilder();
        sessionMessage.setOpcode(C2S.Session.SessionOpcode.SESSION_CREATE);
        sessionMessage.setErrcode(C2S.Session.SessionErrorCode.ERROR_NOERROR);

        // Send the message
        sendMessage(sessionMessage.build(), MessageCase.SESSION);
    }

    @Override
    public void joinSession(String secret) {
        Log.d(TAG, "joinSession: Trying to join session with secret " + secret);
        // Create a message builder and fill in the relevant data
        C2S.Session.Builder sessionMessage = C2S.Session.newBuilder();
        sessionMessage.setOpcode(C2S.Session.SessionOpcode.SESSION_JOIN);
        sessionMessage.setSessionSecret(secret);
        sessionMessage.setErrcode(C2S.Session.SessionErrorCode.ERROR_NOERROR);

        // Send the message
        sendMessage(sessionMessage.build(), MessageCase.SESSION);
    }

    @Override
    public void leaveSession(String secret) {
        Log.d(TAG, "leaveSession: Trying to leave session with secret " + secret);
        // Create a message builder and fill in the relevant data
        C2S.Session.Builder sessionMessage = C2S.Session.newBuilder();
        sessionMessage.setOpcode(C2S.Session.SessionOpcode.SESSION_LEAVE);
        sessionMessage.setSessionSecret(secret);
        sessionMessage.setErrcode(C2S.Session.SessionErrorCode.ERROR_NOERROR);

        // Send the message
        sendMessage(sessionMessage.build(), MessageCase.SESSION);
    }

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
    public void sendKeepaliveMessage() {
        sendStatusMessage(C2C.Status.StatusCode.KEEPALIVE_REQ);
    }

    @Override
    public void sendKeepaliveReply() {
        sendStatusMessage(C2C.Status.StatusCode.KEEPALIVE_REP);
    }

    @Override
    public void notifyNFCNotConnected() {
        sendStatusMessage(C2C.Status.StatusCode.NFC_NO_CONN);
    }
}
