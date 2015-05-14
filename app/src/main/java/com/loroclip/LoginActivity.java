package com.loroclip;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends AccountAuthenticatorActivity {

    private Button mLoginButton;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker mAccessTokenTracker;

    public static final String ARG_ADD_NEW_ACCOUNT = "addNewAccount";
    public static final String ARG_ACCOUNT_TYPE = "accountType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null) {
            Log.d("login", token.getToken());

            retrieveAuthTokenWithFacebookToken(token);
            return;
        }

        bindUI();
    }

    private void retrieveAuthTokenWithFacebookToken(AccessToken token) {
        if (token == null) {
            return;
        }

        final String uid = token.getUserId();

        LoroClipAuthClient client = new LoroClipAuthClient();
        LoroClipAuthClient.LoroClipAuthService service = client.getService();
        service.getAccessToken(token.getToken(), new Callback<LoroClipAuthClient.AccessToken>() {
            @Override
            public void success(LoroClipAuthClient.AccessToken accessToken, Response response) {
                finishLogin(uid, accessToken);
            }

            @Override
            public void failure(RetrofitError error) {
                return;
            }
        });
    }

    private void finishLogin(String uid, LoroClipAuthClient.AccessToken accessToken) {
        final Intent intent = getIntent();
        final Account account = new Account(uid, intent.getStringExtra(ARG_ACCOUNT_TYPE));
        final AccountManager accountManager = AccountManager.get(getApplicationContext());

        if (intent.getBooleanExtra(ARG_ADD_NEW_ACCOUNT, false)) {
            accountManager.addAccountExplicitly(account, null, null);
        }
        accountManager.setUserData(account, LoroClipAccount.KEY_TOKEN_CREATED_AT, String.valueOf(accessToken.getCreatedAt()));
        accountManager.setUserData(account, LoroClipAccount.KEY_TOKEN_EXPIRES_IN, String.valueOf(accessToken.getExpiresIn()));
        accountManager.setAuthToken(account, LoroClipAccount.AUTHTOKEN_TYPE, accessToken.getAccessToken());

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void bindUI() {
        setContentView(R.layout.login);

        final Activity activity = this;

        mCallbackManager = CallbackManager.Factory.create();
        mLoginButton = (Button)findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessToken token = AccessToken.getCurrentAccessToken();
                if (token != null) {
                    retrieveAuthTokenWithFacebookToken(token);
                    return;
                }

                LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"));
            }
        });

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        retrieveAuthTokenWithFacebookToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d("login", "Cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("login", "Error");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
