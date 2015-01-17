package tud.seemuh.nfcgate.network;

import android.nfc.Tag;
import android.util.Log;
import android.widget.TextView;

import com.google.protobuf.ByteString;

import tud.seemuh.nfcgate.reader.IsoDepReaderImpl;
import tud.seemuh.nfcgate.reader.NFCTagReader;
import tud.seemuh.nfcgate.reader.NfcAReaderImpl;
import tud.seemuh.nfcgate.util.Utils;
import tud.seemuh.nfcgate.network.c2c.C2C;


public class CallbackImpl implements SimpleNetworkConnectionClientImpl.Callback {

    private NFCTagReader mReader = null;
    private TextView debugView;

    public void setUpdateButton(TextView ldebugView) {
        debugView = ldebugView;
    }

    /**
     * Implementation of SimpleNetworkConnectionClientImpl.Callback
     * @param data: received bytes
     */
    @Override
    public void onDataReceived(byte[] data) {
        if(mReader.isConnected()) {
            byte[] answer;
            byte[] bytesFromCard = new byte[]{0};
            try {
                // Parse incoming NFCData Protobuf message
                C2C.NFCData NFCData = C2C.NFCData.parseFrom(data);

                // Extract NFC Bytes and send them to the card
                bytesFromCard = mReader.sendCmd(NFCData.getDataBytes().toByteArray());

                // Begin constructing reply
                C2C.NFCData.Builder reply = C2C.NFCData.newBuilder();
                ByteString replyBytes = ByteString.copyFrom(bytesFromCard);
                reply.setDataBytes(replyBytes);
                reply.setDataSource(C2C.NFCData.DataSource.CARD); // TODO This may be incorrect

                // Create byte[] from answer
                answer = reply.build().toByteArray();
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                // We have received an incorrect Protobuf message
                // Create a Status Message with an Error
                C2C.Status.Builder ErrorMsg = C2C.Status.newBuilder();
                ErrorMsg.setCode(C2C.Status.StatusCode.INVALID_MSG_FMT);

                // Create byte[] from message
                answer = ErrorMsg.build().toByteArray();
            }

            // Send the byte[] to the server
            SimpleNetworkConnectionClientImpl.getInstance().sendBytes(answer);

            //Ugly way to send data to the GUI from an external thread
            new UpdateUI(debugView).execute(Utils.bytesToHex(bytesFromCard)+"\n");
        } else {
            // There is no connected NFC device
            // Create an error message
            C2C.Status.Builder errorMessage = C2C.Status.newBuilder();
            errorMessage.setCode(C2C.Status.StatusCode.NFC_NO_CONN);

            // Send error message
            SimpleNetworkConnectionClientImpl.getInstance().sendBytes(
                    errorMessage.build().toByteArray()
            );

            // Update UI
            new UpdateUI(debugView).execute("Received NFC bytes, but we are not connected to any device.\n");
        }
    }

    /**
     * Called on nfc tag intend
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
            SimpleNetworkConnectionClientImpl.getInstance().setCallback(this);
        }

        return found_supported_tag;
    }
}
