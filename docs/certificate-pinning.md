# Remote certificate pinning

Configure remote pinning in `TwinPushOptions.certificatePinningKey` with the public integration
key copied from **Configuration → Certificate pins → Subdomain** in TwinPush
(a superadmin can generate the signing key and publish the TLS pinset there).
Ship that integration key through your trusted application build/configuration.
It is public, not an API credential. Use the application's API token, never an
owner's token. Customers do not need PEM files, environment names, key IDs or
additional URLs.

```java
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.entities.TwinPushOptions;

TwinPushSDK twinPush = TwinPushSDK.getInstance(context);
TwinPushOptions options = new TwinPushOptions();
options.twinPushAppId = APP_ID;
options.twinPushApiKey = APP_API_TOKEN;
options.serverHost = "https://customer.twinpush.com";
options.certificatePinningKey = PINNING_INTEGRATION_KEY; // "tp-pinning-v1:..."
boolean accepted = twinPush.setup(options,
        () -> twinPush.register(deviceAlias, registrationListener));
// accepted only reports local validation. The lambda runs when setup is ready.
```

`SetupListener` has a single abstract method, `onReady()`, so it can be supplied as
a lambda. Its default `onError(Exception)` logs the error through the SDK logger
(`Ln.e(error)`). On failure, the lambda is not executed; a bootstrap failure keeps
pinning enabled.

Set the key and call setup on **every process start, before register, activityStart,
push handling or other operations that can contact TwinPush**. Initialize every
application process that uses the SDK. The key defaults to `null`; `null` or `""`
disables remote pinning. There is no separate enabled flag. Whitespace is an invalid
key, not a request to disable pinning. Existing platform TLS validation still applies.

The existing `boolean setup(TwinPushOptions)` remains supported and starts the same
asynchronous preparation without a listener. The overload is
`boolean setup(TwinPushOptions, TwinPushSDK.SetupListener)`.
`enableCertificatePinning(String)` remains available for compatibility but is
deprecated; configure the key through options in new integrations.

## Loading and errors

`setup` returns `false` for invalid options, a malformed/noncanonical key, invalid
HTTPS origin/app/token with pinning enabled, or activation during an unprotected
HTTP operation. These validation failures leave the previous configuration intact.
If provided, the listener receives the error asynchronously as well. A `true` return
means local options were accepted, **not that bootstrap has completed**. If setup was
rejected and no application ID is configured, a subsequent `register` reports
`Cannot register because TwinPush setup failed: ...` and preserves the original
validation exception as its cause. Register from `onReady` to avoid that follow-up
attempt after a failed setup.

For custom error handling, use an explicit listener and override `onError`:

```java
twinPush.setup(options, new TwinPushSDK.SetupListener() {
    @Override public void onReady() {
        twinPush.register(deviceAlias, registrationListener);
    }
    @Override public void onError(Exception error) {
        // Show an error or offer a retry of setup with these same options.
        // Do not register here or clear the key to bypass a bootstrap failure.
    }
});
```

`SetupListener` receives exactly one result on the main thread, asynchronously:

* Without pinning, `onReady()` follows local setup without any bootstrap request.
* With pinning, `onReady()` requires a verified, unexpired pinset from memory,
  persisted cache or a successful download. Valid cached pins allow readiness even
  while offline; readiness does not guarantee connectivity or a matching server TLS certificate.
* Without usable pins, download/verification failure calls `onError(Exception)`.
  Waiting is limited to 30 seconds; timeout reports `TimeoutException` even if the
  bootstrap worker is blocked. Delivery resumes when the main thread can run.
* An error or timeout leaves pinning enabled. Background recovery can still succeed,
  but the listener is not called again. Repeat setup with the same options to await
  readiness again; retries respect the bootstrap backoff.
* A successful later setup supersedes any undelivered result of the earlier setup,
  delivering an error to its listener instead of a stale `onReady`. This also applies
  when the later setup has identical options or omits a listener. Invalid setup does
  not supersede the previous valid attempt.

Bootstrap, disk revalidation and persistence run on a dedicated worker. An ordinary
operation without an available verified, unexpired pinset fails immediately on the
Volley network dispatcher. Its normal error callback receives `TwinPushException`,
whose cause chain contains `PinningException` with the reason. Bootstrap continues
independently. The SDK does not retain operations indefinitely or replay them when
bootstrap finishes. The application may offer a retry or use a bounded retry policy
when appropriate for the operation. An error after HTTP transmission must not be
assumed safe to retry merely because pinning is enabled.

Volley continues to deliver network callbacks on the main thread and suppresses
callbacks for canceled requests. Finishing or canceling a sequential operation
releases subsequent operations exactly once. The HTTP cache cannot authorize an
ordinary operation while remote pinning is enabled.

403/404/503, invalid signatures, unavailable storage, network failures and expired
documents never disable pinning. A verified, still-current in-memory or persisted
pinset can survive a failed refresh; an expired one cannot. Pin mismatch schedules
an independent refresh subject to backoff, but does not replay the failed request.
Ordinary protected requests have zero Volley retries.

## Configuration and the activation boundary

Each configuration has a generation. Activation and changes to domain, application,
token or integration key invalidate queued operations belonging to older generations.
Operations dispatched with an old generation fail before the transport sends their
HTTP data, including operations held behind sequential registration. Responses/cache
lookups already completed under the prior policy may still finish callback delivery.
Repeating setup with the same enabled configuration
preserves verified pins and the configuration generation and may request a refresh subject to backoff; it does not clear state.

If an unprotected operation is still executing its HTTP exchange, setup returns
`false` and reports `IllegalStateException` to the listener without changing the mode
or waiting on the main thread.
Retry activation after that operation finishes, or initialize before the first request.
Responses to previously transmitted requests can still finish delivery and are
**not protected retroactively**. A successful activation therefore cannot coexist
with an old transport that might still transmit unprotected HTTP. For protected requests,
the current generation, expiry and pins are checked during the TLS handshake.
Already authenticated, in-flight requests may finish after a pinset/configuration
update; subsequent connections must pass the updated checks.

Every successful setup uses its own `certificatePinningKey` option. Pass the key
again to retain pinning. To disable it locally, set the key to `null` or `""` and
call setup. Disabling invalidates old queued operations and pending callbacks; an
old protected operation cannot become an unpinned operation. It does not erase the
persisted version history, so reactivation retains rollback protection. A late
bootstrap result cannot reactivate a disabled configuration.

Changing hostname/environment or signing key requires an independently trusted
integration key in the new options. No server response can replace that trust anchor
or disable pinning: an empty pin list is invalid, as in the v1 protocol. Switching
from a custom serverHost to subdomain removes the old custom host.

Remote pinning covers the configured HTTPS origin and `/api/v2/apps/:app_id` only;
the application in the request must also match the current configuration. Cross-origin requests and
all redirects are rejected. In particular, the separately hosted legacy Forms API
at `forms.twinpush.com` is rejected in this mode; this domain's pins cannot authorize
that other domain. Rich notification WebView content and Firebase/Huawei traffic
do not use the TwinPush Volley queue and are outside this feature's scope.

## Transport and protocol

The existing Volley 1.2.1 queue now uses an internal `PinningHurlStack`. Without
opt-in it delegates to the original `HurlStack`. With opt-in it uses HTTPS, Android's
platform trust manager (including host-specific network security configuration),
normal `HttpsURLConnection` hostname validation, and SHA-256 of **the leaf certificate's
SPKI DER**. An intermediate or root cannot satisfy a leaf pin. No global TLS defaults
are changed. Existing application-level network security policies still apply.

Every protected operation has a fresh SSLContext/socket factory and requests
`Connection: close`. This intentionally sacrifices connection/session reuse so a
connection authenticated under an older policy cannot survive indefinitely. The
pin check runs inside the handshake before HTTP headers or bodies are sent. POST
bodies use fixed-length streaming to prevent HttpURLConnection authentication or
redirect replay. A certificate renewal retaining the same public key retains its
TLS pin. Rotate with an overlapping publication `[A] → [A,B] → [B]`.

The only bootstrap operations are independent HTTPS GETs to the configured origin:

* `/api/v2/apps/:app_id/certificate_pins/verification_key`
* `/api/v2/apps/:app_id/certificate_pins`

The app ID is encoded as a path segment. Bootstrap uses the application's
`X-TwinPush-REST-API-Token`, normal CA/hostname validation, no remote pins, no HTTP
cache and no redirects. Ordinary requests to these paths do **not** acquire a bypass.
Each body is capped at 16 KiB during reading, including unknown content lengths.
Connections/read operations have 10-second timeouts; a 30-second response deadline
also bounds slow streaming (a blocking read can add up to its read timeout).

The parser rejects duplicate/unknown/missing fields, wrong types, noncanonical
Base64, invalid UTC dates, fractions/exponents/signs in version numbers, invalid
pin order/count, extra PEM objects and noncanonical SPKI encodings. RSA is limited
to 2048–8192 bits. The environment-bound SHA-256 integration key authenticates the
downloaded key first; SHA256withRSA then verifies the exact v1 LF-delimited canonical
bytes, including the final LF. No JSON serialization is signed.

## Persistence and refresh

An `AtomicFile` ledger in the application's no-backup directory stores verified
original documents, the public anchor needed to revalidate them, app/origin identity,
expiry and the highest accepted version. The version and canonical payload are
derived again from the signed document on every load. A file lock serializes
read/compare/write transactions, including transactions from other processes.

High-water marks are indexed by the full authenticated environment and survive
app/token/key changes and reactivation. Lower versions are rejected; equal versions
must have identical canonical bytes. Each hostname has a separate ledger, and
cached documents are used only for the same app, origin and anchor. Corruption fails
closed rather than resetting the ledger. Accepted documents are committed and read
back successfully before they become active in memory. An older asynchronous result
cannot replace the current configuration.

Refresh occurs on activation, `activityStart`, before admitting requests near expiry,
after a pin mismatch, and on a scheduled check at most an hour away, normally five
minutes before expiry. Within that last five-minute window, it checks every 30 seconds
or halfway to expiry, whichever is sooner, with a one-second minimum. One refresh runs
at a time. Failure retries wait 1 and 4 seconds; after three failures, automatic
retries stop. Request/resume/repeated-setup activity can try again after
a 60-second cooldown. No background Android service is installed.

## Migration from static SSL methods

Version 3.9.1 removes `setSSLPublicKeyCheck`, `getSSLPublicKeyCheck`,
`addSSLIssuerCheck`, `addSSLSubjectCheck`, `getSSLIssuerChecks` and
`getSSLSubjectChecks`. These methods only persisted values; the default Volley
transport did not enforce them. This is a breaking API change for applications
that reference these methods.

To migrate, remove those calls, set `options.certificatePinningKey`, and call setup
before any request. Previously stored static SSL settings are ignored.
Custom application/network-security-config pinning
is separate and still applies, including to bootstrap; remove or migrate it explicitly
if it would prevent recovery from old TLS pins.

## Limits and verification

First installations and cleared app data have no previous high-water mark, so an
older but unexpired signed revision can be replayed. No minimum bundled version or
trusted remote clock is provided. Expiry uses the device wall clock; clock rollback
can extend the apparent validity window. App-data/OS compromise, deleting/restoring
the ledger, or restoring an old device image is outside this storage guarantee.
No-backup storage avoids ordinary Android cloud-backup rollback. Signatures do not
guarantee availability or automate signing-key rotation.

The SDK is process-local: although ledger commits serialize across processes,
another process's in-memory pins are replaced at its next refresh. Use a single
networking process when immediate cross-process retirement is required.

Transport reference: [Volley 1.2.1 HurlStack](https://github.com/google/volley/blob/1.2.1/core/src/main/java/com/android/volley/toolbox/HurlStack.java).
