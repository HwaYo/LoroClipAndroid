package com.loroclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Created by angdev on 15. 5. 14..
 */
public class LoroClipAccount {
    public static final String ACCOUNT_TYPE = "com.loroclip";
    public static final String AUTHTOKEN_TYPE = "Read/Write";
    public static final String CONTENT_AUTHORITY = "com.loroclip.content";
    public static final String KEY_TOKEN_EXPIRES_IN = "tokenExpiresIn";
    public static final String KEY_TOKEN_CREATED_AT = "tokenCreatedAt";

    private Account mPrimaryAccount;

    private static LoroClipAccount sAccount;
    private static Object sLockObject = new Object();
    private LoroClipAccount() {}

    public static LoroClipAccount getInstance() {
        if (sAccount == null) {
            synchronized (sLockObject) {
                if (sAccount == null) {
                    sAccount = new LoroClipAccount();
                }
            }
        }
        return sAccount;
    }

    public Account getPrimaryAccount (Context context) {
        if (mPrimaryAccount != null) {
            return mPrimaryAccount;
        }

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

        if (accounts.length == 0) {
            return null;
        }
        mPrimaryAccount = accounts[0];
        return mPrimaryAccount;
    }
}
