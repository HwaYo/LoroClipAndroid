package com.loroclip;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
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

    private ImageView mLoginButton;
    private CallbackManager mCallbackManager;

    public static final String ARG_FROM_AUTHENTICATOR = "fromAuthenticator";
    private ImageView logoImg;

    private TextView loginText1;
    private TextView loginText2;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventPublisher publisher = EventPublisher.getInstance();
        publisher.initialize(this);
        publisher.publishEvent("app_opened");

        FacebookSdk.sdkInitialize(getApplicationContext());

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Signing in..");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);

        // TODO Skip this Activity if user has already Logged in.

        Account account = LoroClipAccount.getInstance().getPrimaryAccount(this);
        if (account != null) {
            startMainActivity();
            finish();
            return;
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        bindUI();
    }

    private void retrieveAuthTokenWithFacebookToken(AccessToken token) {
        if (token == null) {
            return;
        }

        final String uid = token.getUserId();

        mProgressDialog.show();

        LoroClipAuthClient client = new LoroClipAuthClient();
        LoroClipAuthClient.LoroClipAuthService service = client.getService();
        service.getAccessToken(token.getToken(), new Callback<LoroClipAuthClient.AccessToken>() {
            @Override
            public void success(LoroClipAuthClient.AccessToken accessToken, Response response) {
                finishLogin(uid, accessToken);
                mProgressDialog.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                return;
            }
        });
    }

    private void finishLogin(String uid, LoroClipAuthClient.AccessToken accessToken) {
        final Intent intent = getIntent();
        final Account account = new Account(uid, LoroClipAccount.ACCOUNT_TYPE);
        final AccountManager accountManager = AccountManager.get(getApplicationContext());

        accountManager.addAccountExplicitly(account, null, null);
        accountManager.setUserData(account, LoroClipAccount.KEY_TOKEN_CREATED_AT, String.valueOf(accessToken.getCreatedAt()));
        accountManager.setUserData(account, LoroClipAccount.KEY_TOKEN_EXPIRES_IN, String.valueOf(accessToken.getExpiresIn()));
        accountManager.setAuthToken(account, LoroClipAccount.AUTHTOKEN_TYPE, accessToken.getAccessToken());

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        EventPublisher.getInstance().publishEvent("logged_in");

        if (intent.getBooleanExtra(ARG_FROM_AUTHENTICATOR, false)) {
            finish();
            return;
        }

        startMainActivity();
        finish();
    }

    private void bindUI() {
        setContentView(R.layout.login);

        final Activity activity = this;

        loginText1 = (TextView) findViewById(R.id.login_text_1);
        loginText2 = (TextView) findViewById(R.id.login_text_2);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        loginText1.setTypeface(typeface);
        loginText2.setTypeface(typeface);

        logoImg = (ImageView)findViewById(R.id.login_background);
//        logoImg.setAlpha(0.2f);

        mCallbackManager = CallbackManager.Factory.create();
        mLoginButton = (ImageView)findViewById(R.id.login_button);
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

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
