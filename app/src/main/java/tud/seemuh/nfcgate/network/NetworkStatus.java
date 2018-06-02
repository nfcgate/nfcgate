package tud.seemuh.nfcgate.network;

public enum NetworkStatus {
    CONNECTING,
    SEND_ERROR,
    RECEIVE_ERROR,

    PARTNER_WAIT,
    PARTNER_CONNECT,
    PARTNER_LEFT,
}
