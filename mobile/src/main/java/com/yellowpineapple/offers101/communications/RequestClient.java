package com.yellowpineapple.offers101.communications;

import android.content.Context;
import android.location.Location;

import com.yellowpineapple.offers101.communications.requests.BaseRequest;
import com.yellowpineapple.offers101.communications.requests.OfferListRequestListener;
import com.yellowpineapple.offers101.communications.requests.offers.FindOffersRequest;

import lombok.Getter;

public class RequestClient {

	private static RequestClient sharedInstance = null;

    public enum Environment {
        PRODUCTION("http://app.101offers.co/", false);

        @Getter
        String url;
        @Getter boolean dummy;

        private Environment(String url, boolean dummy) {
            this.url = url;
            this.dummy = dummy;
        }
    }
	
	/* Properties */
	RequestLauncher requestLauncher;
    Environment environment;
    Context context;
	String token = null;
	
	public static RequestClient getSharedInstance(Context context, Environment environment) {
		if (sharedInstance == null) {
			sharedInstance = new RequestClient(context, environment);
		}
		return sharedInstance;
	}
	
	private RequestClient(Context context, Environment environment) {
		requestLauncher = new DefaultRequestLauncher(context);
        this.context = context;
        this.environment = environment;
	}

    /* Public methods */

    // Offers

    public Request findOffers(Location location, OfferListRequestListener listener) {
        return launch(new FindOffersRequest(location, listener));
    }

	/* Private methods */
	private Request launch(BaseRequest request) {
        request.setRequestLauncher(requestLauncher);
        request.setEnvironment(environment);
        request.launch();
        return request;
	}
}
