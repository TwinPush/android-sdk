package com.yellowpineapple.wakup.sdk.communications;

import android.content.Context;
import android.location.Location;

import com.yellowpineapple.wakup.sdk.communications.requests.BaseRequest;
import com.yellowpineapple.wakup.sdk.communications.requests.OfferListRequestListener;
import com.yellowpineapple.wakup.sdk.communications.requests.offers.CompanyOffersRequest;
import com.yellowpineapple.wakup.sdk.communications.requests.offers.FindOffersRequest;
import com.yellowpineapple.wakup.sdk.communications.requests.offers.GetOffersByIdRequest;
import com.yellowpineapple.wakup.sdk.communications.requests.offers.RelatedOffersRequest;
import com.yellowpineapple.wakup.sdk.communications.requests.search.SearchRequest;
import com.yellowpineapple.wakup.sdk.models.Category;
import com.yellowpineapple.wakup.sdk.models.Company;
import com.yellowpineapple.wakup.sdk.models.CompanyDetail;
import com.yellowpineapple.wakup.sdk.models.Offer;
import com.yellowpineapple.wakup.sdk.models.Store;

import java.util.List;

public class RequestClient {

	private static RequestClient sharedInstance = null;

    public enum Environment {
        PRODUCTION("http://app.wakup.net/", false);

        String url;
        boolean dummy;

        Environment(String url, boolean dummy) {
            this.url = url;
            this.dummy = dummy;
        }

        public String getUrl() {
            return url;
        }

        public boolean isDummy() {
            return dummy;
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

    public Request findOffers(Location location, int page, OfferListRequestListener listener) {
        return launch(new FindOffersRequest(location, null, null, page, listener));
    }

    public Request findOffers(Location location, Company company, List<Category> categories, int page, OfferListRequestListener listener) {
        return launch(new FindOffersRequest(location, company, categories, page, listener));
    }

    public Request findLocatedOffers(Location location, Double radius, OfferListRequestListener listener) {
        return launch(new FindOffersRequest(location, radius, listener));
    }

    public Request relatedOffers(Offer offer, int page, int perPage, OfferListRequestListener listener) {
        return launch(new RelatedOffersRequest(offer, page, perPage, listener));
    }

    public Request findOffersById(List<String> offerIds, Location location, int page, OfferListRequestListener listener) {
        return launch(new GetOffersByIdRequest(offerIds, location, page, listener));
    }

    public Request getCompanyOffers(CompanyDetail company, Store store, int page, OfferListRequestListener listener) {
        return launch(new CompanyOffersRequest(company, store, page, BaseRequest.RESULTS_PER_PAGE, listener));
    }

    // Search

    public Request search(String query, SearchRequest.Listener listener) {
        return launch(new SearchRequest(query, listener));
    }

	/* Private methods */
	private Request launch(BaseRequest request) {
        request.setRequestLauncher(requestLauncher);
        request.setEnvironment(environment);
        request.launch();
        return request;
	}
}
