package tud.seemuh.nfcgate.network;

import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.BlockingQueue;

import tud.seemuh.nfcgate.network.c2s.C2S;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterManager;
import tud.seemuh.nfcgate.util.sink.SinkManager;

public interface HighLevelNetworkHandler {
    // Setup
    public HighLevelNetworkHandler connect(String addr, int port);

    public void disconnect();

    public void disconnectBrokenPipe();

    public void disconnectCardWorkaround();

    public void notifyCardWorkaroundConnected();

    public void setDebugView(TextView ldebugView);

    public void setConnectionStatusView(TextView connStatusView);

    public void setPeerStatusView(TextView view);

    public void setButtons(Button mReset, Button mConnecttoSession, Button mAbort, Button mJoinSession);

    public void setCallback(Callback mCallback);

    // Session messages
    public void createSession();

    public void joinSession(String secret);

    public void leaveSession();

    public void confirmSessionCreation(String secret);

    public void confirmSessionJoin();

    public void confirmSessionLeave();

    public void sessionPartnerJoined();

    public void sessionPartnerLeft();

    public void sessionPartnerAPDUModeOn();

    public void sessionPartnerReaderModeOn();

    public void sessionPartnerAPDUModeOff();

    public void sessionPartnerReaderModeOff();

    public void sessionPartnerNFCLost();

    public void sessionCreateFailed(C2S.Session.SessionErrorCode errcode);

    public void sessionJoinFailed(C2S.Session.SessionErrorCode errcode);

    public void sessionLeaveFailed(C2S.Session.SessionErrorCode errcode);

    // NFC messages
    public void sendAPDUMessage(byte[] apdu);

    public void sendAPDUReply(byte[] reply);

    public void sendAnticol(byte[] atqa, byte sak, byte[] hist, byte[] uid);

    // Notification messages
    public void notifyReaderFound();

    public void notifyCardFound();

    public void notifyReaderRemoved();

    public void notifyCardRemoved();

    // Error messages
    public void notifyInvalidMsgFormat();

    public void notifyNotImplemented();

    public void notifyUnknownMessageType();

    public void notifyUnknownError();

    public void notifyNFCNotConnected();

    // Misc Messages
    public void sendKeepaliveMessage();

    public void sendKeepaliveReply();

    // Sink Manager
    public void setSinkManager(SinkManager mSinkManager, BlockingQueue<NfcComm> mSinkManagerQueue);

    public void notifySinkManager(NfcComm nfc);

    // NFC Manager
    public void setNfcManager(NfcManager nfcManager);
}
