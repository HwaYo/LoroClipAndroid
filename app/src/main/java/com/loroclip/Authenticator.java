package com.loroclip;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Date;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Authenticator extends AbstractAccountAuthenticator {
    private Context mContext;

    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        final Bundle bundle = new Bundle();

        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_ADD_NEW_ACCOUNT, true);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        final AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(account, authTokenType);

        boolean tokenExpired = isTokenExpired(accountManager, account);
        if (tokenExpired) {
            accountManager.invalidateAuthToken(LoroClipAccount.ACCOUNT_TYPE, authToken);
        }

        if (!TextUtils.isEmpty(authToken) && !tokenExpired) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);

            return result;
        }

        final Intent intent = new Intent(mContext, LoginActivity.class);
        final Bundle bundle = new Bundle();

        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, account.type);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

    private boolean isTokenExpired(AccountManager accountManager, Account account) {
        int createdAt = Integer.valueOf(accountManager.getUserData(account, LoroClipAccount.KEY_TOKEN_CREATED_AT));
        int expiresIn = Integer.valueOf(accountManager.getUserData(account, LoroClipAccount.KEY_TOKEN_EXPIRES_IN));
        long expiresAt = (createdAt + expiresIn) * 1000;
        Date expiresAtDate = new Date(expiresAt);

        return new Date().after(expiresAtDate);
    }
}
