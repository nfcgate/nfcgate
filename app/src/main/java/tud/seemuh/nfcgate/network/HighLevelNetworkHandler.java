package tud.seemuh.nfcgate.network;

import com.google.protobuf.Message;
import tud.seemuh.nfcgate.network.meta.MetaMessage.Wrapper.MessageCase;

public interface HighLevelNetworkHandler {
    public void sendMessage(Message msg, MessageCase mcase);
}
