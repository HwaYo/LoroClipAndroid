package com.loroclip;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by angdev on 15. 5. 12..
 */
public class LoroClipAuthClient {
    private static final String API_ENDPOINT = "http://loroclip-staging.herokuapp.com";
    private RestAdapter mRestAdapter;
    private LoroClipAuthService mService;

    public interface LoroClipAuthService {
        @POST("/oauth/token?grant_type=password")
        void getAccessToken(@Query("assertion") String assertion, Callback<AccessToken> cb);
    }

    public class AccessToken {
        private String access_token;
        private String token_type;
        private int expires_in;
        private int created_at;

        public String getAccessToken() {
            return access_token;
        }

        public String getTokenType() {
            return token_type;
        }

        public int getExpiresIn() {
            return expires_in;
        }

        public int getCreatedAt() { return created_at; }
    }

    public LoroClipAuthClient() {
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT)
                .build();

        mService = mRestAdapter.create(LoroClipAuthService.class);
    }

    public LoroClipAuthService getService() {
        return mService;
    }
}
