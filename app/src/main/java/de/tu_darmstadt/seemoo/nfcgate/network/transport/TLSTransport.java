package de.tu_darmstadt.seemoo.nfcgate.network.transport;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.tu_darmstadt.seemoo.nfcgate.network.UserTrustManager;

public class TLSTransport extends Transport {
    private static final String TAG = "TLSTransport";
    protected SSLContext mSslContext;

    public TLSTransport(String hostname, int port) {
        super(hostname, port);

        createSslContext();
    }

    protected void createSslContext() {
        try {
            mSslContext = SSLContext.getInstance("TLS");
            mSslContext.init(null, buildTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.wtf(TAG, "Cannot instantiate SSLContext");
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("CustomX509TrustManager")
    protected TrustManager[] buildTrustManagers() {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            // we want to use the default TrustManager for verification purposes later
            X509TrustManager defaultManager = ((X509TrustManager) factory.getTrustManagers()[0]);

            // create our own TrustManager
            return new X509TrustManager[] { new UserX509TrustManager(defaultManager) };
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Socket createSocket() {
        SSLSocketFactory sslSocketFactory = mSslContext.getSocketFactory();
        try {
            return sslSocketFactory.createSocket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void connectSocket() throws IOException {
        mSocket.connect(mAddress, 10000);
        ((SSLSocket) mSocket).startHandshake();

        // verify the hostname, even though we do not use HTTPS, we can borrow the hostname verifier
        if (!HttpsURLConnection.getDefaultHostnameVerifier().verify(mAddress.getHostString(), ((SSLSocket) mSocket).getSession()))
            throw new SSLHandshakeException("Hostname in certificate does not match");
    }

    protected static class UserX509TrustManager implements X509TrustManager {
        protected X509TrustManager mDefaultManager;

        public UserX509TrustManager(X509TrustManager defaultManager) {
            this.mDefaultManager = defaultManager;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            mDefaultManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                mDefaultManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                // If this is server authentication and the certificate path verification
                // fails, we check if the user explicitly trusted the certificate.
                // If verification fails due to other errors, e.g. expiry, fail immediately
                if (!(e.getCause() instanceof CertPathValidatorException))
                    throw e;

                UserTrustManager.Trust trust = UserTrustManager.getInstance().checkCertificate(chain);
                switch (trust) {
                    case TRUSTED:
                        // consider the certificate trusted
                        return;
                    case UNKNOWN:
                        UserTrustManager.getInstance().setCachedCertificateChain(chain);
                        throw new UserTrustManager.UnknownTrustException();
                    case UNTRUSTED:
                    default:
                        throw new UserTrustManager.UntrustedException();
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return mDefaultManager.getAcceptedIssuers();
        }
    }
}
