package com.loroclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.loroclip.model.Bookmark;
import com.loroclip.model.BookmarkHistory;
import com.loroclip.model.Record;
import com.loroclip.model.SyncableModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit.RetrofitError;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by angdev on 15. 5. 15..
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        AccountManager accountManager = AccountManager.get(mContext);
        try {
            String accessToken = accountManager.blockingGetAuthToken(account, LoroClipAccount.AUTHTOKEN_TYPE, true);
            LoroClipAPIClient client = new LoroClipAPIClient(accessToken);

            syncIndependentEntities(client, LoroClipAPIClient.RecordAPIService.class, Record.class);
            syncIndependentEntities(client, LoroClipAPIClient.BookmarkAPIService.class, Bookmark.class);
            syncIndependentEntities(client, LoroClipAPIClient.BookmarkHistoryAPIService.class, BookmarkHistory.class);

            syncRecordFiles(client);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private <T, U extends SyncableModel>
    Set<U> syncIndependentEntities(LoroClipAPIClient client, Class<T> serviceType, Class<U> entityType)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Date recentSyncedAt = U.getRecentSyncedAt(entityType);
        int recentSyncedAtTimestamp = (int) (recentSyncedAt.getTime() / 1000);
        T service = client.getService(serviceType);
        Set<U> syncedEntities = new HashSet<>();

        try {
            // Pull
            Method pullMethod = serviceType.getDeclaredMethod("pullEntities", int.class);
            List<U> entities = (List<U>) pullMethod.invoke(service, recentSyncedAtTimestamp);

            for (U entity : entities) {
                U mappedEntity = U.findByUuid(entityType, entity.getUuid());
                if (mappedEntity != null) {
                    if (entity.isDeleted()) {
                        mappedEntity.delete(true);
                    } else if (mappedEntity.getUpdatedAt().before(entity.getUpdatedAt())) {
                        mappedEntity.overwrite(entity);
                        mappedEntity.saveAsSynced();
                    }
                } else if (!entity.isDeleted()) {
                    entity.saveAsSynced();
                }
                syncedEntities.add(entity);
            }

            // Push
            Method pushMethod = serviceType.getDeclaredMethod("pushEntities", LoroClipAPIClient.PushEntitiesParams.class);
            List<U> dirtyEntities = U.find(entityType, "dirty = ?", "1");
            List<U> clearEntities = (List<U>)pushMethod.invoke(service, new LoroClipAPIClient.PushEntitiesParams<U>(dirtyEntities));

            for (U entity : clearEntities) {
                U mappedEntity = U.findByUuid(entityType, entity.getUuid());
                if (mappedEntity == null) {
                    continue;
                }

                mappedEntity.saveAsSynced();

                syncedEntities.add(mappedEntity);
            }
        } catch (RetrofitError e) {

        }

        return syncedEntities;
    }

    private void syncRecordFiles(LoroClipAPIClient client) {
        List<Record> records = Record.listExists(Record.class);
        for (Record record : records) {
            File recordFile = record.getLocalFile();

            if (recordFile == null || record.getRemoteFilePath() != null) {
                continue;
            }

            String mimeType = URLConnection.guessContentTypeFromName(recordFile.getName());
            Record synced = client.getService(LoroClipAPIClient.RecordAPIService.class).uploadFile(
                    new TypedString(record.getUuid()),
                    new TypedFile(mimeType, recordFile)
            );

            if (synced != null) {
                record.setRemoteFilePath(synced.getRemoteFilePath());
                record.saveAsSynced();
            }
        }
    }
}
