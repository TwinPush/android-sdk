package com.twincoders.twinpush.sdk.communications.pinning;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.HurlStack;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/** The actual Volley transport; bootstrap cannot be requested through this stack. */
public final class PinningHurlStack extends BaseHttpStack {
    private final PinningController controller;
    private final PinningTls.ServerTrust trust;
    private final HurlStack legacy = new HurlStack();

    PinningHurlStack(PinningController controller, PinningTls.ServerTrust trust) {
        this.controller = controller; this.trust = trust;
    }
    @Override public HttpResponse executeRequest(Request<?> request, Map<String, String> headers) throws IOException, AuthFailureError {
        if (request.isCanceled()) throw new PinningException("request canceled");
        Object tag = request.getTag();
        PinningController.Lease lease = controller.beginTransport(request.getUrl(), tag instanceof Long ? (Long) tag : -1);
        if (lease == null) {
            try { return legacy.executeRequest(request, headers); }
            finally { controller.finishLegacyTransport(); }
        }
        request.setRetryPolicy(new DefaultRetryPolicy(request.getTimeoutMs(), 0, 1));
        SSLSocketFactory factory = PinningTls.factory(controller, lease, trust);
        return new HurlStack(null, factory) {
            @Override protected HttpURLConnection createConnection(URL url) throws IOException {
                if (request.isCanceled()) throw new PinningException("request canceled");
                HttpsURLConnection connection = (HttpsURLConnection) super.createConnection(url);
                connection.setInstanceFollowRedirects(false);
                connection.setUseCaches(false);
                connection.setRequestProperty("Connection", "close");
                return connection;
            }
            @Override protected OutputStream createOutputStream(Request<?> r, HttpURLConnection connection, int length) throws IOException {
                // Disables HttpURLConnection's authentication/redirect replay of request bodies.
                connection.setFixedLengthStreamingMode(length);
                return super.createOutputStream(r, connection, length);
            }
        }.executeRequest(request, headers);
    }
}
