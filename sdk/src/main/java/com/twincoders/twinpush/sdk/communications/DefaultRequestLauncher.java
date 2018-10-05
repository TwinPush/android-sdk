package com.twincoders.twinpush.sdk.communications;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.twincoders.twinpush.sdk.logging.Ln;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class DefaultRequestLauncher implements TwinRequestLauncher {

    private Context context;

    private RequestQueue queue;

    /** Array of active requests */
    private Map<TwinRequest, Request> activeRequests = new HashMap<>();

    /* Parameterized constructor will be used when RequestLauncher is not injected */
    DefaultRequestLauncher(@NonNull Context context) {
        this.context = context;
        queue = Volley.newRequestQueue(context);
    }

    @Override
    public void launchRequest(TwinRequest request) {
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
        if (activeRequests.containsKey(twinRequest)) {
            Request request = activeRequests.get(twinRequest);
            request.cancel();
            activeRequests.remove(twinRequest);
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

        Request volleyRequest = request.getRequest();

        // Include request in active requests map
        activeRequests.put(request, volleyRequest);
        request.addOnRequestFinishListener(new TwinRequest.OnRequestFinishListener() {
            @Override
            public void onRequestFinish() {
                activeRequests.remove(request);
            }
        });

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
