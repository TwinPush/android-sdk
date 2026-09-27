package com.twincoders.twinpush.sdk.communications.pinning;

import android.content.Context;
import android.net.http.X509TrustManagerExtensions;
import android.util.AtomicFile;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/** Internal Android adapters shared by SDK configuration and its Volley queue. */
public final class PinningRuntime {
    private static PinningController instance;
    private PinningRuntime() {}
    public static synchronized PinningController get(Context context) {
        if (instance == null) {
            Context app = context.getApplicationContext();
            instance = new PinningController(new PinRepository(new AndroidStorage(app)), new BootstrapClient(),
                    System::currentTimeMillis, Executors.newSingleThreadScheduledExecutor(r -> {
                        Thread thread = new Thread(r, "TwinPush-pinning"); thread.setDaemon(true); return thread;
                    }));
        }
        return instance;
    }
    public static RequestQueue newQueue(Context context) {
        return newQueue(context, get(context));
    }
    static RequestQueue newQueue(Context context, PinningController controller) {
        // A queued request must not bypass admission via a previously cached HTTP response.
        DiskBasedCache cache = new DiskBasedCache(new File(context.getCacheDir(), "volley")) {
            @Override public synchronized Entry get(String key) { return controller.isEnabled() ? null : super.get(key); }
            @Override public synchronized void put(String key, Entry entry) { if (!controller.isEnabled()) super.put(key, entry); }
        };
        PinningTls.ServerTrust trust = (chain, authType, host) -> {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init((KeyStore) null);
                for (TrustManager manager : factory.getTrustManagers()) {
                    if (manager instanceof X509TrustManager) {
                        // Preserve domain-specific Android Network Security Configuration trust.
                        new X509TrustManagerExtensions((X509TrustManager) manager).checkServerTrusted(chain, authType, host);
                        return;
                    }
                }
                throw new CertificateException("No platform X509 trust manager");
            } catch (CertificateException e) { throw e; }
            catch (Exception e) { throw new CertificateException("Platform trust validation failed", e); }
        };
        RequestQueue queue = new RequestQueue(cache, new BasicNetwork(new PinningHurlStack(controller, trust)));
        queue.start(); return queue;
    }
    static final class AndroidStorage implements PinRepository.Storage {
        private final Context context;
        AndroidStorage(Context context) { this.context = context; }
        @Override public synchronized void transact(String host, PinRepository.Transaction transaction) throws IOException {
            File directory = new File(context.getNoBackupFilesDir(), "twinpush-pinning");
            if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("Cannot create pin ledger directory");
            String name = PinEncoding.hex(PinEncoding.hash(host.getBytes(StandardCharsets.UTF_8)));
            // Also serialize another app process updating the same environment's high-water mark.
            try (RandomAccessFile lockFile = new RandomAccessFile(new File(directory, name + ".lock"), "rw");
                 FileLock lock = lockFile.getChannel().lock()) {
                AtomicFile file = new AtomicFile(new File(directory, name));
                byte[] previous = null;
                if (file.getBaseFile().exists() || new File(directory, name + ".bak").exists()) {
                    try (FileInputStream stream = file.openRead()) { previous = PinProtocol.read(stream, PinRepository.MAX_STORE); }
                }
                byte[] next = transaction.apply(previous);
                if (Arrays.equals(previous, next)) return;
                FileOutputStream output = null;
                try {
                    output = file.startWrite(); output.write(next);
                    output.getFD().sync(); file.finishWrite(output);
                } catch (IOException e) { if (output != null) file.failWrite(output); throw e; }
                // AtomicFile.finishWrite has no return value and some Android versions log rename
                // failures instead of throwing. Never publish an unpersisted high-water mark.
                try (FileInputStream verify = file.openRead()) {
                    PinProtocol.require(Arrays.equals(next, PinProtocol.read(verify, PinRepository.MAX_STORE)), "pin ledger commit failed");
                }
            }
        }
    }
}
