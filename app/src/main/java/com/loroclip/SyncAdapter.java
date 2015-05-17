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
        Date oldestSyncedAt = Record.getOldestSyncedAt(Record.class);
        int oldestSyncedAtTimestamp = (int) (oldestSyncedAt.getTime() / 1000);

        try {
            // Pull
            List<Record> records = service.pullRecords(oldestSyncedAtTimestamp);
            for (Record record : records) {
                Record mappedRecord = Record.findByUuid(Record.class, record.uuid);
                if (mappedRecord != null) {
                    if (record.isDeleted()) {
                        mappedRecord.delete(true);
                    }
                    else if (mappedRecord.getUpdatedAt().before(record.getUpdatedAt())) {
                        mappedRecord.overwrite(record);
                    }
                    // else then required push sync
                } else {
                    // todo: override #save
                    record.saveAsSynced();
                }
            }

            List<Record> dirtyRecords = Record.find(Record.class, "dirty = ?", "1");
            List<Record> syncedRecords = service.pushRecords(new LoroClipAPIClient.LoroClipService.PushRecordsParams(dirtyRecords));

            for (Record record : syncedRecords) {
                Record mappedRecord = Record.findByUuid(Record.class, record.uuid);
                mappedRecord.saveAsSynced();
            }
        } catch (RetrofitError e) {

        }
    }
}
