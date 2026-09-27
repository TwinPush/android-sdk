package com.twincoders.twinpush.sdk.communications.pinning;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** SDK-internal state machine. Bootstrap and pin repository IO run on the worker. */
public final class PinningController {
    interface Clock { long now(); }
    interface Fetcher { byte[] get(Config config, boolean verificationKey) throws IOException; }
    static final class Config {
        final String origin, host, app, token, anchor;
        final long generation;
        Config(String origin, String app, String token, String anchor, long generation) {
            if (origin == null) throw new IllegalArgumentException("Configure the TwinPush HTTPS origin first");
            try {
                URI uri = new URI(origin);
                if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null
                        || uri.getRawQuery() != null || uri.getRawFragment() != null
                        || !(uri.getRawPath().isEmpty() || uri.getRawPath().equals("/"))
                        || !uri.getHost().matches("[a-z0-9.-]+") || uri.getPort() == 0 || uri.getPort() > 65535) {
                    throw new IllegalArgumentException("Pinning requires an HTTPS origin with a lowercase hostname");
                }
                if (app == null || app.isEmpty() || app.length() > 1024 || app.equals(".") || app.equals("..")
                        || !app.matches("[\\x21-\\x7e]+") || token == null || token.isEmpty()
                        || !token.matches("[\\x21-\\x7e]+")) {
                    throw new IllegalArgumentException("Pinning requires an app ID and application API token");
                }
                this.origin = "https://" + uri.getRawAuthority(); this.host = uri.getHost();
                this.app = app; this.token = token; this.anchor = anchor; this.generation = generation;
            } catch (URISyntaxException e) { throw new IllegalArgumentException("Invalid TwinPush origin", e); }
        }
        boolean same(String origin, String app, String token, String anchor) {
            Config other = new Config(origin, app, token, anchor, generation);
            return this.origin.equals(other.origin) && this.app.equals(app) && this.token.equals(token) && this.anchor.equals(anchor);
        }
        boolean permits(URI uri) {
            int port = uri.getPort() == -1 ? 443 : uri.getPort();
            URI own = URI.create(origin);
            return "https".equals(uri.getScheme()) && host.equals(uri.getHost())
                    && port == (own.getPort() == -1 ? 443 : own.getPort())
                    && uri.getRawUserInfo() == null && uri.getRawFragment() == null;
        }
    }
    static final class Lease {
        final Config config;
        Lease(Config config) { this.config = config; }
    }
    private final PinRepository repository;
    private final Fetcher fetcher;
    private final Clock clock;
    private final ScheduledExecutorService worker;
    private boolean enabled;
    private int legacyTransports;
    private long generation;
    private Config config;
    private PinProtocol.Pins pins;
    private IOException lastFailure;
    private boolean refreshing;
    private int failures;
    private long retryAfter;
    private ScheduledFuture<?> scheduled;
    private final Map<ReadyListener, Long> readiness = new HashMap<>();

    /** Internal listeners must only enqueue delivery, never call application code here. */
    public interface ReadyListener { void onResult(Exception error); }

    public synchronized boolean isReady(long expected) {
        return expected == generation && (!enabled || (config != null && pins != null && pins.expires > clock.now()));
    }

    public synchronized void whenReady(long expected, ReadyListener listener) {
        readiness.put(listener, expected);
        notifyReadiness();
    }

    public synchronized void removeReadyListener(ReadyListener listener) { readiness.remove(listener); }

    private void notifyReadiness() {
        for (Map.Entry<ReadyListener, Long> entry : new HashMap<>(readiness).entrySet()) {
            Exception error = null;
            if (entry.getValue() != generation) error = new PinningException("SDK setup was superseded");
            else if (!isReady(entry.getValue())) {
                if (refreshing || lastFailure == null) continue;
                error = lastFailure;
            }
            readiness.remove(entry.getKey());
            entry.getKey().onResult(error);
        }
    }

    PinningController(PinRepository repository, Fetcher fetcher, Clock clock, ScheduledExecutorService worker) {
        this.repository = repository; this.fetcher = fetcher; this.clock = clock; this.worker = worker;
    }
    public synchronized boolean isEnabled() { return enabled; }
    public synchronized long generation() { return generation; }

    public static void validateConfiguration(String origin, String app, String token) {
        new Config(origin, app, token, null, 0);
    }

    public synchronized void enable(String anchor, String origin, String app, String token) {
        PinProtocol.validateAnchor(anchor);
        configure(anchor, origin, app, token, () -> {});
    }

    /** Validates before applying local options; request admission cannot observe a partial setup. */
    public synchronized long configure(String anchor, String origin, String app, String token, Runnable applyLocal) {
        boolean activate = anchor != null && !anchor.isEmpty();
        if (activate) PinProtocol.validateAnchor(anchor);
        Config next = activate ? new Config(origin, app, token, anchor, generation + 1) : null;
        if (activate && !enabled && legacyTransports != 0) {
            throw new IllegalStateException("Cannot enable pinning during an unprotected HTTP operation; enable before the first request or retry after it finishes");
        }
        applyLocal.run();
        if (activate && config != null && config.same(origin, app, token, anchor)) { refresh(); return generation; }
        enabled = activate; generation++; config = next; pins = null; lastFailure = null;
        failures = 0; retryAfter = 0;
        cancelScheduled();
        notifyReadiness();
        // All tasks share one worker: old tasks cannot overlap a new bootstrap.
        if (activate) worker.execute(() -> loadAndRefresh(next));
        return generation;
    }

    /** Invalidates queued work before setup mutates domain/app/token; opt-in is never cleared. */
    public synchronized String invalidateConfiguration() {
        if (!enabled) return null;
        String anchor = config == null ? null : config.anchor;
        config = null; pins = null; generation++; cancelScheduled();
        lastFailure = new PinningException("SDK configuration changed; enable pinning for the new configuration");
        notifyReadiness();
        return anchor;
    }

    private void loadAndRefresh(Config expected) {
        try {
            PinRepository.Record cached = repository.load(expected.host, expected.origin, expected.app, expected.anchor);
            synchronized (this) {
                if (config != expected) return;
                if (cached != null && cached.pins.expires > clock.now()) {
                    pins = cached.pins;
                    notifyReadiness();
                }
            }
        } catch (IOException e) { synchronized (this) { if (config == expected) lastFailure = e; } }
        synchronized (this) { if (config == expected) startRefresh(); }
    }

    public synchronized void refresh() {
        if (!enabled || config == null || refreshing || clock.now() < retryAfter) return;
        startRefresh();
    }
    private void startRefresh() {
        if (refreshing) return;
        refreshing = true;
        Config expected = config;
        worker.execute(() -> fetch(expected));
    }
    private void fetch(Config expected) {
        IOException failure = null;
        PinRepository.Record record = null;
        boolean committed = false;
        try {
            byte[] key = fetcher.get(expected, true);
            PinProtocol.key(key, expected.host, expected.anchor); // Authenticate before fetching pins.
            byte[] document = fetcher.get(expected, false);
            record = new PinRepository.Record(expected.anchor, expected.origin, expected.app, key, document, expected.host);
            synchronized (this) { if (config != expected) return; }
            repository.accept(expected.host, record, clock.now());
            committed = true;
        } catch (IOException e) { failure = e; }
        catch (RuntimeException e) { failure = new PinningException("bootstrap failed", e); }
        finally {
            synchronized (this) {
                refreshing = false;
                if (config != expected) {
                    if (config != null) startRefresh();
                } else if (committed) {
                    pins = record.pins; lastFailure = null; failures = 0;
                    retryAfter = clock.now() + 1000;
                    long remaining = pins.expires - clock.now();
                    long delay = remaining > 300000 ? Math.min(3600000, remaining - 300000)
                            : Math.max(1000, Math.min(30000, remaining / 2));
                    schedule(delay);
                } else {
                    lastFailure = failure; failures++;
                    long delay = failures == 1 ? 1000 : failures == 2 ? 4000 : 60000;
                    retryAfter = clock.now() + delay;
                    if (failures < 3) schedule(delay); // No permanent service or infinite retry loop.
                }
                notifyReadiness();
            }
        }
    }
    private void cancelScheduled() { if (scheduled != null) { scheduled.cancel(false); scheduled = null; } }
    private void schedule(long delay) {
        cancelScheduled();
        scheduled = worker.schedule(() -> { synchronized (this) { scheduled = null; refresh(); } }, delay, TimeUnit.MILLISECONDS);
    }

    synchronized Lease admit(String url, long requestGeneration) throws IOException {
        PinProtocol.require(requestGeneration == generation, "request belongs to an old SDK configuration");
        if (!enabled) return null;
        PinProtocol.require(requestGeneration == generation && config != null, "request belongs to an old SDK configuration");
        try {
            URI uri = new URI(url);
            PinProtocol.require(config.permits(uri), "request outside configured HTTPS origin");
            String prefix = "/api/v2/apps/", path = uri.getRawPath();
            PinProtocol.require(path.startsWith(prefix), "request outside configured application API");
            String rest = path.substring(prefix.length());
            String segment = rest.split("/", 2)[0];
            PinProtocol.require(config.app.equals(java.net.URLDecoder.decode(segment.replace("+", "%2B"), "UTF-8")), "request belongs to a different application");
        }
        catch (URISyntaxException e) { throw new PinningException("invalid request URL", e); }
        if (pins == null || pins.expires <= clock.now()) {
            refresh();
            throw new PinningException("no verified, current pinset available; retry the operation after bootstrap", lastFailure);
        }
        if (pins.expires - clock.now() <= 300000) refresh();
        return new Lease(config);
    }

    synchronized Lease beginTransport(String url, long requestGeneration) throws IOException {
        Lease lease = admit(url, requestGeneration);
        if (lease == null) legacyTransports++;
        return lease;
    }
    synchronized void finishLegacyTransport() { legacyTransports--; }

    /** Called inside the TLS handshake, after platform CA validation and before HTTP is written. */
    synchronized void checkLeaf(Lease lease, X509Certificate leaf) throws java.security.cert.CertificateException {
        try {
            PinProtocol.require(config == lease.config && pins != null, "configuration changed during TLS handshake");
            pins.requireCurrent(clock.now());
            String actual = "sha256/" + PinEncoding.base64(PinEncoding.hash(leaf.getPublicKey().getEncoded()));
            if (!pins.pins.contains(actual)) {
                if (!refreshing) {
                    long delay = retryAfter - clock.now();
                    if (delay > 0) schedule(delay); else refresh();
                }
                throw new PinningException("TLS leaf SPKI mismatch");
            }
        } catch (PinningException e) { throw new java.security.cert.CertificateException(e); }
    }
}
