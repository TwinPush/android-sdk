package com.twincoders.twinpush.sdk.communications.security;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;

public class TwinPushSSLSocketFactory extends SSLSocketFactory {
	
	String publicKey = null;
	Map<String, String> issuerChecks = null;
	Map<String, String> subjectChecks = null;
	
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public TwinPushSSLSocketFactory(KeyStore truststore, TwinPushTrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        sslContext.init(null, new TrustManager[] { trustManager }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}