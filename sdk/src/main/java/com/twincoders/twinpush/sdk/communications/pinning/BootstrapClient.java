package com.twincoders.twinpush.sdk.communications.pinning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/** Fixed GET endpoints only. No arbitrary URL, method, or public bypass flag. */
final class BootstrapClient implements PinningController.Fetcher {
    private final javax.net.ssl.SSLSocketFactory testTrust;
    BootstrapClient() { this(null); }
    // Package-private injection for a local test CA, never exposed through SDK configuration.
    BootstrapClient(javax.net.ssl.SSLSocketFactory testTrust) { this.testTrust = testTrust; }
    @Override public byte[] get(PinningController.Config config, boolean verificationKey) throws IOException {
        String appSegment = java.net.URLEncoder.encode(config.app, "UTF-8").replace("+", "%20");
        URL url = new URL(config.origin + "/api/v2/apps/" + appSegment + "/certificate_pins" + (verificationKey ? "/verification_key" : ""));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        if (testTrust != null) connection.setSSLSocketFactory(testTrust);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000); connection.setReadTimeout(10000);
        connection.setUseCaches(false);
        connection.setRequestProperty("X-TwinPush-REST-API-Token", config.token);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Accept-Encoding", "identity");
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
        try {
            PinProtocol.require(connection.getResponseCode() == 200, "bootstrap HTTP " + connection.getResponseCode());
            PinProtocol.require(connection.getContentLength() <= PinProtocol.MAX_RESPONSE, "oversized bootstrap response");
            String encoding = connection.getContentEncoding();
            PinProtocol.require(encoding == null || "identity".equalsIgnoreCase(encoding), "unexpected bootstrap encoding");
            try (InputStream stream = connection.getInputStream()) { return PinProtocol.read(stream, PinProtocol.MAX_RESPONSE, deadline); }
        } finally { connection.disconnect(); }
    }
}
