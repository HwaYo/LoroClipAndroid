package com.loroclip;

import com.loroclip.model.Record;

import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;

/**
 * Created by angdev on 15. 5. 12..
 */
public class LoroClipAPIClient {
    private static final String API_ENDPOINT = "http://10.0.2.2:3000/api/v1";
    private RestAdapter mRestAdapter;
    private LoroClipService mService;
    private String mAccessToken;

    public interface LoroClipService {
        @GET("/records")
        void listRecords(Callback<List<Record>> cb);
    }

    public LoroClipAPIClient(String accessToken) {
        this.mAccessToken = accessToken;

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
            request.addHeader("Authorization", "Bearer " + mAccessToken);
            }
        };

        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT)
                .setRequestInterceptor(requestInterceptor)
                .build();

        mService = mRestAdapter.create(LoroClipService.class);
    }

    public LoroClipService getService() {
        return mService;
    }
}
