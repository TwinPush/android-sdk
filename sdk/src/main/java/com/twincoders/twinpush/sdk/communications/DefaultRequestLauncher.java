package com.twincoders.twinpush.sdk.communications;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.DefaultRetryPolicy;
import com.twincoders.twinpush.sdk.communications.pinning.PinningController;
import com.twincoders.twinpush.sdk.communications.pinning.PinningRuntime;
import com.twincoders.twinpush.sdk.logging.Ln;

import java.util.Map;

class DefaultRequestLauncher implements TwinRequestLauncher {

    private Context context;

    private RequestQueue queue;

    /** Array of active requests */
    private Map<TwinRequest, Request> activeRequests = new java.util.concurrent.ConcurrentHashMap<>();

    /* Parameterized constructor will be used when RequestLauncher is not injected */
    DefaultRequestLauncher(@NonNull Context context) {
        this.context = context;
        queue = PinningRuntime.newQueue(context);
    }

    @Override
    public synchronized void launchRequest(TwinRequest request) {
        Ln.v("Starting request: %s", request.getClass().getName());
        // Check if request is already on queue
        if (!activeRequests.containsKey(request)) {
            // Include request in execution queue
            executeRequest(request);
        } else {
            Ln.w("Request already on queue. Ignoring...");
        }
    }

    @Override
    public void cancelRequest(TwinRequest twinRequest) {
        // Cancel request by calling linked Http client method
        Request request = activeRequests.remove(twinRequest);
        if (request != null) {
            request.cancel();
            Ln.v("Request canceled");
        } else {
            Ln.v("Could not cancel request, not currently active");
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

	/* PRIVATE METHODS */
    /** Starts request execution */
    private void executeRequest(final TwinRequest request) {

        Request volleyRequest;
        PinningController security = PinningRuntime.get(context);
        synchronized (security) {
            volleyRequest = request.getRequest();
            long generation = security.generation();
            if (request instanceof DefaultRequest) {
                DefaultRequest operation = (DefaultRequest) request;
                operation.bindSecurityGeneration(generation);
                generation = operation.securityGeneration();
            }
            volleyRequest.setTag(generation);
            if (security.isEnabled()) {
                volleyRequest.setShouldCache(false);
                volleyRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
            }
        }

        // Include request in active requests map
        activeRequests.put(request, volleyRequest);
        request.addOnRequestFinishListener(new TwinRequest.OnRequestFinishListener() {
            @Override
            public void onRequestFinish() {
                activeRequests.remove(request);
            }
        });

        if (request.isCanceled()) {
            activeRequests.remove(request);
            volleyRequest.cancel();
            return;
        }

        // Launch request
        if (!request.isDummy()) {
            queue.add(volleyRequest);
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (!request.isCanceled()) {
                        requestEnded(request);
                        request.onResponseProcess("");
                    }
                }
            }, 1000);
        }
    }

    private void requestEnded(TwinRequest request) {
        activeRequests.remove(request);
    }
}
