package tud.seemuh.nfcgate.network;

import com.google.protobuf.Message;

import tud.seemuh.nfcgate.network.c2c.C2C;
import tud.seemuh.nfcgate.network.c2s.C2S;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper.MessageCase;

public class NetHandler implements HighLevelNetworkHandler {

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
        } else {
            // TODO This should never happen...
        }

        // Build and serialize the message
        byte[] msgbytes = WrapperMsg.build().toByteArray();

        // Send the message
        SimpleLowLevelNetworkConnectionClientImpl.getInstance().sendBytes(msgbytes);
    }
}
