package com.loroclip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.loroclip.model.Bookmark;
import com.loroclip.model.BookmarkHistory;
import com.loroclip.model.Record;
import com.loroclip.model.SyncableModel;

import java.util.Date;
import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by angdev on 15. 5. 12..
 */

public class LoroClipAPIClient {
    private static final String API_ENDPOINT = "http://parrot.172.16.101.81.xip.io/api/v1";
    private RestAdapter mRestAdapter;
    private String mAccessToken;

    public LoroClipAPIClient(String accessToken) {
        this.mAccessToken = accessToken;

        Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GmtDateTypeAdapter()).create();
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
    }

    public <T> T getService(Class<T> type) {
        return mRestAdapter.create(type);
    }

    public static class PushEntitiesParams<T extends SyncableModel> {
        PushEntitiesParams(List<T> entities) {
            this.entities = entities;
            for (T entity : this.entities) {
                entity.decorate();
            }
        }
        @SerializedName("entities")
        public List<T> entities;
    }

    public interface RecordAPIService {
        @GET("/records/pull")
        List<Record> pullEntities(@Query("last_synced_at") int lastSyncedAt);

        @POST("/records/push")
        List<Record> pushEntities(@Body PushEntitiesParams<Record> params);

        @Multipart
        @POST("/records/file")
        Record uploadFile(@Part("uuid") TypedString uuid, @Part("file") TypedFile recordFile);
    }

    public interface BookmarkAPIService {
        @GET("/bookmarks/pull")
        List<Bookmark> pullEntities(@Query("last_synced_at") int lastSyncedAt);

        @POST("/bookmarks/push")
        List<Bookmark> pushEntities(@Body PushEntitiesParams<Bookmark> params);
    }

    public interface BookmarkHistoryAPIService {
        @GET("/bookmark_histories/pull")
        List<BookmarkHistory> pullEntities(@Query("last_synced_at") int lastSyncedAt);

        @POST("/bookmark_histories/push")
        List<BookmarkHistory> pushEntities(@Body PushEntitiesParams<BookmarkHistory> params);
    }
}
