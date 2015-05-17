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

import com.loroclip.model.Record;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit.RetrofitError;

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
        android.os.Debug.waitForDebugger();

        AccountManager accountManager = AccountManager.get(mContext);
        try {
            String accessToken = accountManager.blockingGetAuthToken(account, LoroClipAccount.AUTHTOKEN_TYPE, true);
            LoroClipAPIClient client = new LoroClipAPIClient(accessToken);

            syncRecords(client.getService());

        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }
    }

    private void syncRecords(LoroClipAPIClient.LoroClipService service) {
        Date oldestSyncedAt = Record.getOldestSyncedAt();
        int oldestSyncedAtTimestamp = (int) (oldestSyncedAt.getTime() / 1000);

        try {
            // Pull
            List<Record> records = service.pullRecords(oldestSyncedAtTimestamp);
            for (Record record : records) {
                Record mappedRecord = Record.findByUUID(record.uuid);
                if (mappedRecord != null) {
                    if (record.deleted) {
                        mappedRecord.delete(true);
                    }
                    else if (mappedRecord.updatedAt.before(record.updatedAt)) {
                        mappedRecord.overwriteEntity(record);
                    }
                    // else then required push sync
                } else {
                    // todo: override #save
                    record.save(true);
                }
            }

            List<Record> dirtyRecords = Record.find(Record.class, "dirty = ?", "1");
            List<Record> syncedRecords = service.pushRecords(new LoroClipAPIClient.LoroClipService.PushRecordsParams(dirtyRecords));

            for (Record record : syncedRecords) {
                Record mappedRecord = Record.findByUUID(record.uuid);
                mappedRecord.save(true);
            }
        } catch (RetrofitError e) {

        }
    }
}
