package com.loroclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by angdev on 15. 5. 12..
 */
public class TokenManager {
    private static TokenManager mInstance;
    private boolean mIsInitialized;
    private Context mContext;
    private AccountManager mAccountManager;
    private Account mAccount;

    public interface TokenManagerCallback {
        void run(String s);
    }

    private TokenManager() {
        mIsInitialized = false;
    }

    public static TokenManager getInstance() {
        if (mInstance == null) {
            synchronized (TokenManager.class) {
                if (mInstance == null) {
                    mInstance = new TokenManager();
                }
            }
        }
        return mInstance;
    }

    public AccountManagerFuture<Bundle> initialize(final Activity activity, final Context context, final TokenManagerCallback callback) {
        mContext = context;
        mAccountManager = AccountManager.get(mContext);

        Account[] accounts = mAccountManager.getAccountsByType(LoroClipAccount.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            return mAccountManager.addAccount(LoroClipAccount.ACCOUNT_TYPE, null, null, null, activity,
                    new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                            Account[] accounts = mAccountManager.getAccountsByType(LoroClipAccount.ACCOUNT_TYPE);
                            mAccount = accounts[0];
                            mIsInitialized = true;

                            if (callback != null) {
                                callback.run(null);
                            }
                        }
                    }, null);
        }

        mAccount = accounts[0];
        mIsInitialized = true;
        callback.run(null);

        return null;
    }

    public void getAccessToken(final Activity activity, final TokenManagerCallback callback) {
        final AccountManagerCallback<Bundle> authTokenCallback = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                try {
                    Bundle bundle = accountManagerFuture.getResult();
                    callback.run(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                } catch (Exception e) {
                    callback.run(null);
                }
            }
        };

        if (!mIsInitialized) {
            initialize(activity, activity.getApplicationContext(), new TokenManagerCallback() {
                @Override
                public void run(String s) {
                    mAccountManager.getAuthToken(mAccount, LoroClipAccount.AUTHTOKEN_TYPE, null, activity, authTokenCallback, null);
                }
            });
        } else {
            mAccountManager.getAuthToken(mAccount, LoroClipAccount.AUTHTOKEN_TYPE, null, activity, authTokenCallback, null);
        }
    }
}
