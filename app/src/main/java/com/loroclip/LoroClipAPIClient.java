package com.loroclip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loroclip.model.Record;

import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by angdev on 15. 5. 12..
 */
public class LoroClipAPIClient {
    private static final String API_ENDPOINT = "http://parrot.192.168.0.11.xip.io/api/v1";
    private RestAdapter mRestAdapter;
    private LoroClipService mService;
    private String mAccessToken;

    public interface LoroClipService {
        @GET("/records/pull")
        List<Record> pullRecords(@Query("last_synced_at") int lastSyncedAt);

        class PushRecordsParams {
            PushRecordsParams(List<Record> records) { this.records = records; }
            public List<Record> records;
        }

        @POST("/records/push")
        List<Record> pushRecords(@Body PushRecordsParams params);
    }

    public LoroClipAPIClient(String accessToken) {
        this.mAccessToken = accessToken;

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
            request.addHeader("Authorization", "Bearer " + mAccessToken);
            }
        };

        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT)
                .setRequestInterceptor(requestInterceptor)
                .setConverter(new GsonConverter(gson))
                .build();

        mService = mRestAdapter.create(LoroClipService.class);
    }

    public LoroClipService getService() {
        return mService;
    }
}
