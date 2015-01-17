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


public class CallbackImpl implements SimpleLowLevelNetworkConnectionClientImpl.Callback {
    private final static String TAG = "ApduService";

    private NFCTagReader mReader = null;
    private TextView debugView;
    private NetHandler Handler = new NetHandler();

    public void setUpdateButton(TextView ldebugView) {
        debugView = ldebugView;
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
                Log.i(TAG, "MessageCase.DATA: Sending to handler");
                handleData(Wrapper.getData());
            }
            else if (Wrapper.getMessageCase() == MessageCase.KEX) {
                Log.i(TAG, "MessageCase.KEX: Sending to handler");
                handleKex(Wrapper.getKex());
            }
            else if (Wrapper.getMessageCase() == MessageCase.NFCDATA) {
                Log.i(TAG, "MessageCase:NFCDATA: Sending to handler");
                handleNFCData(Wrapper.getNFCData());
            }
            else if (Wrapper.getMessageCase() == MessageCase.SESSION) {
                Log.i(TAG, "MessageCase.SESSION: Sending to handler");
                handleSession(Wrapper.getSession());
            }
            else if (Wrapper.getMessageCase() == MessageCase.STATUS) {
                Log.i(TAG, "MessageCase.STATUS: Sending to handler");
                handleStatus(Wrapper.getStatus());
            }
            else {
                Log.e(TAG, "Message fits no known case! This is fucked up");
                sendErrorMessage(C2C.Status.StatusCode.UNKNOWN_MESSAGE);
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            // We have received a message in an invalid format.
            // Send error message
            Log.e(TAG, "Message was malformed, discarding and sending error message");
            sendErrorMessage(C2C.Status.StatusCode.INVALID_MSG_FMT);
        }
    }


    private void sendErrorMessage(C2C.Status.StatusCode code) {
        // Create error message
        C2C.Status.Builder ErrorMsg = C2C.Status.newBuilder();
        ErrorMsg.setCode(code);

        // Send message
        Handler.sendMessage(ErrorMsg.build(), MessageCase.STATUS);
    }


    private void handleKex(C2C.Kex msg) {
        Log.e(TAG, "MessageCase.KEX: Not implemented");
        sendErrorMessage(C2C.Status.StatusCode.NOT_IMPLEMENTED);
    }


    private void handleNFCData(C2C.NFCData msg) {
        if(mReader.isConnected()) {
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
        } else {
            // There is no connected NFC device
            sendErrorMessage(C2C.Status.StatusCode.NFC_NO_CONN);

            // Update UI
            new UpdateUI(debugView).execute("Received NFC bytes, but we are not connected to any device.\n");
        }
    }


    private void handleStatus(C2C.Status msg) {
        Log.e(TAG, "MessageCase.STATUS: Not implemented");
        sendErrorMessage(C2C.Status.StatusCode.NOT_IMPLEMENTED);
    }


    private void handleData(C2S.Data msg) {
        Log.e(TAG, "MessageCase.DATA: Not implemented");
        sendErrorMessage(C2C.Status.StatusCode.NOT_IMPLEMENTED);
    }


    private void handleSession(C2S.Session msg) {
        Log.e(TAG, "MessageCase.SESSION: Not implemented");
        sendErrorMessage(C2C.Status.StatusCode.NOT_IMPLEMENTED);
    }


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
            Log.i("NFCGATE_DEBUG", "Tag TechList: " + type);
            if("android.nfc.tech.IsoDep".equals(type)) {
                found_supported_tag = true;

                mReader = new IsoDepReaderImpl(tag);
                Log.d("NFCGATE_DEBUG", "Chose IsoDep technology.");
                break;
            } else if("android.nfc.tech.NfcA".equals(type)) {
                found_supported_tag = true;

                mReader = new NfcAReaderImpl(tag);
                Log.d("NFCGATE_DEBUG", "Chose NfcA technology.");
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
