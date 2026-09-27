package com.twincoders.twinpush.sdk;

import android.os.Handler;
import android.os.Looper;

import com.twincoders.twinpush.sdk.communications.pinning.PinningController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/** One setup attempt. Application callbacks only run on the main thread, outside controller locks. */
final class SetupCompletion {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final PinningController controller;
    private final long generation;
    private TwinPushSDK.SetupListener listener;
    private volatile boolean cancelled;
    private boolean done;
    private final PinningController.ReadyListener observer = error -> main.post(() -> finish(error));
    private final Runnable timeout = () -> finish(new TimeoutException("TwinPush setup timed out after 30 seconds"));

    SetupCompletion(PinningController controller, long generation, TwinPushSDK.SetupListener listener) {
        this.controller = controller;
        this.generation = generation;
        this.listener = listener;
    }

    void start() {
        main.postDelayed(timeout, 30000);
        controller.whenReady(generation, observer);
    }

    void cancel() {
        cancelled = true;
        main.post(() -> finish(new IOException("TwinPush setup was superseded")));
    }

    private void finish(Exception error) {
        if (done) return;
        if (cancelled || controller.generation() != generation) {
            error = new IOException("TwinPush setup was superseded");
        } else if (error == null && !controller.isReady(generation)) {
            error = new IOException("TwinPush pinning configuration is no longer ready");
        }
        done = true;
        main.removeCallbacks(timeout);
        controller.removeReadyListener(observer);
        TwinPushSDK.SetupListener target = listener;
        listener = null;
        if (error == null) target.onReady(); else target.onError(error);
    }
}
