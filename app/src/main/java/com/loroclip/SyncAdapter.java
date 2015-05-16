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
//        android.os.Debug.waitForDebugger();

        AccountManager accountManager = AccountManager.get(mContext);
        try {
            String accessToken = accountManager.blockingGetAuthToken(account, LoroClipAccount.AUTHTOKEN_TYPE, true);
            LoroClipAPIClient client = new LoroClipAPIClient(accessToken);
            List<Record> records = null;


            try {
                records = client.getService().pullRecords(0);
                for (Record record : records) {
                    record.save();
                }

            } catch (RetrofitError e) {
                if (e.getResponse().getStatus() == 401) {
                    accountManager.invalidateAuthToken(LoroClipAccount.ACCOUNT_TYPE, accessToken);
                    return;
                }
            }
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }
    }

    private void syncRecords(LoroClipAPIClient.LoroClipService service) {

        try {
            List<Record> records = service.pullRecords(0);
        } catch (RetrofitError e) {

        }
    }
}
