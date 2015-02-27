package tud.seemuh.nfcgate.network;

import android.nfc.Tag;
import android.util.Log;
import android.widget.TextView;

import com.google.protobuf.ByteString;

import tud.seemuh.nfcgate.network.meta.MetaMessage;
import tud.seemuh.nfcgate.reader.IsoDepReaderImpl;
import tud.seemuh.nfcgate.reader.NFCTagReader;
import tud.seemuh.nfcgate.reader.NfcAReaderImpl;
import tud.seemuh.nfcgate.util.Utils;
import tud.seemuh.nfcgate.network.c2c.C2C;
import tud.seemuh.nfcgate.network.c2s.C2S;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper.MessageCase;
import tud.seemuh.nfcgate.network.c2c.C2C.Status.StatusCode;
import tud.seemuh.nfcgate.network.c2s.C2S.Session.SessionOpcode;
import tud.seemuh.nfcgate.network.c2s.C2S.Session.SessionErrorCode;
import tud.seemuh.nfcgate.hce.ApduService;


public class CallbackImpl implements SimpleLowLevelNetworkConnectionClientImpl.Callback {
    private final static String TAG = "CallbackImpl";

    private ApduService apdu;
    private NFCTagReader mReader = null;
    private TextView debugView;
    private NetHandler Handler = new NetHandler();

    private enum Status {
        NOT_CONNECTED,
        SESSION_CREATE_SENT,
        SESSION_JOIN_SENT,
        WAITING_FOR_PARTNER,
        SESSION_READY,
        SESSION_LEAVE_SENT,
    }

    Status status;

    private String SessionToken;

    public void setUpdateButton(TextView ldebugView) {
        debugView = ldebugView;
    }


    public CallbackImpl(ApduService as) {
        apdu = as;
        status = Status.NOT_CONNECTED;
    }

    public CallbackImpl() {
        status = Status.NOT_CONNECTED;
    }

    /**
     * Implementation of SimpleNetworkConnectionClientImpl.Callback
     * @param data: received bytes
     */
    @Override
    public void onDataReceived(byte[] data) {
        try {
            // Parse incoming data as a MetaMessage
            MetaMessage.Wrapper Wrapper = MetaMessage.Wrapper.parseFrom(data);

            // Determine which type of Message the MetaMessage contains
            if (Wrapper.getMessageCase() == MessageCase.DATA) {
                Log.i(TAG, "onDataReceived: MessageCase.DATA: Sending to handler");
                handleData(Wrapper.getData());
            }
            else if (Wrapper.getMessageCase() == MessageCase.KEX) {
                Log.i(TAG, "onDataReceived: MessageCase.KEX: Sending to handler");
                handleKex(Wrapper.getKex());
            }
            else if (Wrapper.getMessageCase() == MessageCase.NFCDATA) {
                Log.i(TAG, "onDataReceived: MessageCase:NFCDATA: Sending to handler");
                handleNFCData(Wrapper.getNFCData());
            }
            else if (Wrapper.getMessageCase() == MessageCase.SESSION) {
                Log.i(TAG, "onDataReceived: MessageCase.SESSION: Sending to handler");
                handleSession(Wrapper.getSession());
            }
            else if (Wrapper.getMessageCase() == MessageCase.STATUS) {
                Log.i(TAG, "onDataReceived: MessageCase.STATUS: Sending to handler");
                handleStatus(Wrapper.getStatus());
            }
            else if (Wrapper.getMessageCase() == MessageCase.ANTICOL) {
                Log.i(TAG, "onDataReceived: MessageCase.STATUS: Sending to handler");
                handleAnticol(Wrapper.getAnticol());
            }
            else {
                Log.e(TAG, "onDataReceived: Message fits no known case! This is fucked up");
                sendStatusMessage(StatusCode.UNKNOWN_MESSAGE);
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            // We have received a message in an invalid format.
            // Send error message
            Log.e(TAG, "onDataReceived: Message was malformed, discarding and sending error message");
            sendStatusMessage(StatusCode.INVALID_MSG_FMT);
        }
    }


    /**
     * Private helper function to send Status messages.
     */
    private void sendStatusMessage(StatusCode code) {
        // Create error message
        C2C.Status.Builder ErrorMsg = C2C.Status.newBuilder();
        ErrorMsg.setCode(code);

        // Send message
        Handler.sendMessage(ErrorMsg.build(), MessageCase.STATUS);
    }


    /**
     * Notify the other party that a reader has been detected in the proximity of the device
     */
    public void notifyReaderDetected() {
        sendStatusMessage(StatusCode.READER_FOUND);
    }


    /**
     * Notify the other party that a reader has left the proximity of the device
     */
    public void notifyReaderRemoved() {
        sendStatusMessage(StatusCode.READER_REMOVED);
    }


    /**
     * Notify the other party that a card has been detected in the proximity of the device
     */
    public void notifyCardDetected() {
        sendStatusMessage(StatusCode.CARD_FOUND);
    }


    /**
    Notify the other party that the card has left the proximity of the device
     */
    public void notifyCardRemoved() {
        sendStatusMessage(StatusCode.CARD_REMOVED);
    }


    /**
     * Join an existing session at the server.
     * @param secret String with the session secret
     */
    public void joinSession(String secret) {
        Log.d(TAG, "joinSession: Trying to join session with secret " + secret);
        // Create a message builder and fill in the relevant data
        C2S.Session.Builder sessionMessage = C2S.Session.newBuilder();
        sessionMessage.setOpcode(SessionOpcode.SESSION_CREATE);
        sessionMessage.setSessionSecret(secret);
        sessionMessage.setErrcode(SessionErrorCode.ERROR_NOERROR);

        // Send the message
        Handler.sendMessage(sessionMessage.build(), MessageCase.SESSION);
    }

    /**
     * Send a keepalive packet to the peer, who will respond with a keepalive response message.
     */
    public void sendKeepaliveMessage() {
        Log.i(TAG, "sendKeepaliveMessage: Keepalive msg sent.");
        sendStatusMessage(StatusCode.KEEPALIVE_REQ);
    }


    private void handleKex(C2C.Kex msg) {
        Log.e(TAG, "handleKex: Not implemented");
        sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
    }


    private void handleAnticol(C2C.Anticol msg) {
        Log.e(TAG, "handleAnticol: Not implemented");
        sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
    }


    private void handleNFCData(C2C.NFCData msg) {
        if (msg.getDataSource() == C2C.NFCData.DataSource.READER) {
            // We received a signal FROM a reader device and are required to talk TO a card.
            if (mReader.isConnected()) {
                Log.i(TAG, "HandleNFCData: Received message for a card, forwarding...");
                // Extract NFC Bytes and send them to the card
                byte[] bytesFromCard = mReader.sendCmd(msg.getDataBytes().toByteArray());

                // Begin constructing reply
                C2C.NFCData.Builder reply = C2C.NFCData.newBuilder();
                ByteString replyBytes = ByteString.copyFrom(bytesFromCard);
                reply.setDataBytes(replyBytes);
                reply.setDataSource(C2C.NFCData.DataSource.CARD);

                // Send reply
                Handler.sendMessage(reply.build(), MessageCase.NFCDATA);

                //Ugly way to send data to the GUI from an external thread
                new UpdateUI(debugView).execute(Utils.bytesToHex(bytesFromCard) + "\n");
                Log.i(TAG, "HandleNFCData: Received and forwarded reply from card");
                Log.i(TAG, "HandleNFCData: BytesFromCard: " + Utils.bytesToHex(bytesFromCard));
            } else {
                Log.e(TAG, "HandleNFCData: No NFC connection active");
                // There is no connected NFC device
                sendStatusMessage(StatusCode.NFC_NO_CONN);

                // Update UI
                new UpdateUI(debugView).execute("HandleNFCData: Received NFC bytes, but we are not connected to any device.\n");
            }
        } else {
            if (apdu != null) {
                Log.i(TAG, "HandleNFCData: Received a message for a reader, forwarding...");
                // We received a signal FROM a card and are required to talk TO a reader.
                apdu.sendResponseApdu(msg.getDataBytes().toByteArray());
            } else {
                Log.e(TAG, "HandleNFCData: Received a message for a reader, but no APDU instance active.");
                sendStatusMessage(StatusCode.NFC_NO_CONN);
            }
        }
    }


    private void handleStatus(C2C.Status msg) {
        if (msg.getCode() == StatusCode.KEEPALIVE_REQ) {
            // Received keepalive request, reply with response
            Log.i(TAG, "handleStatus: Got Keepalive request, replying");
            sendStatusMessage(StatusCode.KEEPALIVE_REP);
        }
        else if (msg.getCode() == StatusCode.KEEPALIVE_REP) {
            // Got keepalive response, do nothing for now
            Log.i(TAG, "handleStatus: Got Keepalive response. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.NOT_IMPLEMENTED) {
            Log.e(TAG, "handleStatus: Other party sent NOT_IMPLEMENTED. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.UNKNOWN_ERROR) {
            Log.e(TAG, "handleStatus: Other party sent UNKNOWN_ERROR. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.UNKNOWN_MESSAGE) {
            Log.e(TAG, "handleStatus: Other party sent UNKNOWN_MESSAGE. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.READER_FOUND) {
            Log.e(TAG, "handleStatus: Other party sent READER_FOUND. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.READER_REMOVED) {
            Log.e(TAG, "handleStatus: Other party sent READER_REMOVED. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.CARD_FOUND) {
            Log.e(TAG, "handleStatus: Other party sent CARD_FOUND. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.CARD_REMOVED) {
            Log.e(TAG, "handleStatus: Other party sent CARD_REMOVED. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.NFC_NO_CONN) {
            Log.e(TAG, "handleStatus: Other party sent NFC_NO_CONN. Doing nothing");
        }
        else if (msg.getCode() == StatusCode.INVALID_MSG_FMT) {
            Log.e(TAG, "handleStatus: Other party sent INVALID_MSG_FMT. Doing nothing");
        }
        else {
            // Not implemented
            Log.e(TAG, "handleStatus: Message case not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
    }


    private void handleData(C2S.Data msg) {
        Log.e(TAG, "handleData: Not implemented");
        sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
    }


    private void handleSession(C2S.Session msg) {
        if (msg.getOpcode() == SessionOpcode.SESSION_CREATE_FAIL) {
            // TODO is it possible to display a popup notification to the user in this case?
            Log.e(TAG, "handleSession: SESSION_CREATE_FAIL: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else if (msg.getOpcode() == SessionOpcode.SESSION_CREATE_SUCCESS) {
            Log.e(TAG, "handleSession: SESSION_CREATE_SUCCESS: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else if (msg.getOpcode() == SessionOpcode.SESSION_JOIN_FAIL) {
            Log.e(TAG, "handleSession: SESSION_JOIN_FAIL: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else if (msg.getOpcode() == SessionOpcode.SESSION_JOIN_SUCCESS) {
            Log.e(TAG, "handleSession: SESSION_JOIN_SUCCESS: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else if (msg.getOpcode() == SessionOpcode.SESSION_LEAVE_FAIL) {
            Log.e(TAG, "handleSession: SESSION_LEAVE_FAIL: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else if (msg.getOpcode() == SessionOpcode.SESSION_LEAVE_SUCCESS) {
            Log.e(TAG, "handleSession: SESSION_LEAVE_SUCCESS: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else if (msg.getOpcode() == SessionOpcode.SESSION_PEER_JOINED) {
            Log.e(TAG, "handleSession: SESSION_PEER_JOINED: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else if (msg.getOpcode() == SessionOpcode.SESSION_PEER_LEFT) {
            Log.e(TAG, "handleSession: SESSION_PEER_LEFT: Not implemented");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
        else {
            Log.e(TAG, "handleSession: Unknown Opcode!");
            sendStatusMessage(StatusCode.NOT_IMPLEMENTED);
        }
    }


    // TODO Refactor this part into another class
    /**
     * Called on nfc tag intent
     * @param tag nfc tag
     * @return true if a supported tag is found
     */
    public boolean setTag(Tag tag) {

        boolean found_supported_tag = false;

        //identify tag type
        for(String type: tag.getTechList()) {
            // TODO: Refactor this into something much nicer to avoid redundant work betw.
            //       this code and the worker thread, which also does this check.
            Log.i(TAG, "setTag: Tag TechList: " + type);
            if("android.nfc.tech.IsoDep".equals(type)) {
                found_supported_tag = true;

                mReader = new IsoDepReaderImpl(tag);
                Log.d(TAG, "setTag: Chose IsoDep technology.");
                break;
            } else if("android.nfc.tech.NfcA".equals(type)) {
                found_supported_tag = true;

                mReader = new NfcAReaderImpl(tag);
                Log.d(TAG, "setTag: Chose NfcA technology.");
                break;
            }
        }

        //set callback when data is received
        if(found_supported_tag){
            SimpleLowLevelNetworkConnectionClientImpl.getInstance().setCallback(this);
        }

        return found_supported_tag;
    }
}
