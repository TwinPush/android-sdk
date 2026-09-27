package com.twincoders.twinpush.sdk.communications.pinning;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

final class PinningTls {
    interface ServerTrust { void check(X509Certificate[] chain, String authType, String host) throws CertificateException; }
    // Delegates to the platform's host-aware CA validation before applying the additional leaf pin.
    // Real TLS tests cover untrusted CAs, wrong hostnames and wrong pins independently.
    @android.annotation.SuppressLint("CustomX509TrustManager")
    static SSLSocketFactory factory(PinningController controller, PinningController.Lease lease, ServerTrust platform) throws IOException {
        try {
            // A new context AND factory for every operation prevents pooling and session resumption
            // across requests/pin revisions. HttpsURLConnection retains its hostname verification.
            SSLContext tls = SSLContext.getInstance("TLS");
            tls.init(null, new TrustManager[] { new X509TrustManager() {
                @Override public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    if (chain == null || chain.length == 0) throw new CertificateException("Empty certificate chain");
                    platform.check(chain, authType, lease.config.host);
                    controller.checkLeaf(lease, chain[0]);
                }
                @Override public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    throw new CertificateException("Client authentication is not supported by this trust manager");
                }
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            } }, null);
            return tls.getSocketFactory();
        } catch (GeneralSecurityException e) { throw new PinningException("cannot initialize TLS", e); }
    }
}
