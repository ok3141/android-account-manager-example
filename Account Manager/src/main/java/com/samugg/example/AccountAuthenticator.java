package com.samugg.example;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_INTENT;
import static com.samugg.example.BuildConfig.DEBUG;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
    private static final String LOG_TAG = AccountAuthenticator.class.getSimpleName();

    @NonNull
    private final Context context;

    public AccountAuthenticator(@NonNull Context context) {
        super(context);

        this.context = context;
    }

    @Override
    public Bundle addAccount(
            @NonNull AccountAuthenticatorResponse response,
            @NonNull String accountType,
            @Nullable String authTokenType,
            @Nullable String[] requiredFeatures,
            @Nullable Bundle options
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "addAccount: " + accountType + ", " + authTokenType + ", " + Arrays.toString(requiredFeatures) + ", " + options);
        }

        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);

        Bundle result = new Bundle();
        result.putParcelable(KEY_INTENT, intent);
        return result;
    }

    @Override
    public Bundle confirmCredentials(
            @NonNull AccountAuthenticatorResponse response,
            @NonNull Account account,
            @Nullable Bundle options
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "confirmCredentials: " + account + ", " + options);
        }

        return null;
    }

    @Override
    public Bundle editProperties(
            @NonNull AccountAuthenticatorResponse response,
            @NonNull String accountType
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "editProperties: " + accountType);
        }

        return null;
    }

    @Override
    public Bundle getAuthToken(
            @NonNull AccountAuthenticatorResponse response,
            @NonNull Account account,
            @NonNull String authTokenType,
            @Nullable Bundle options
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "getAuthToken: " + account + ", " + authTokenType + ", " + options);
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        AccountManager am = AccountManager.get(context);

        String authToken = am.peekAuthToken(account, authTokenType);

        // Lets give another try to authenticate the user
        if (authToken == null || authToken.isEmpty()) {
            String password = am.getPassword(account);
            if (password != null) {
                authToken = AccountUtils.mServerAuthenticator.signIn(account.name, password);
            }
        }

        // If we get an authToken - we return it
        if (authToken != null && !authToken.isEmpty()) {
            Bundle result = new Bundle();
            result.putString(KEY_ACCOUNT_NAME, account.name);
            result.putString(KEY_ACCOUNT_TYPE, account.type);
            result.putString(KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(LoginActivity.ARG_AUTH_TOKEN_TYPE, authTokenType);

        // This is for the case multiple accounts are stored on the device
        // and the AccountPicker dialog chooses an account without auth token.
        // We can pass out the account name chosen to the user of write it
        // again in the Login activity intent returned.
        if (account.name != null) {
            intent.putExtra(KEY_ACCOUNT_NAME, account.name);
        }

        Bundle result = new Bundle();
        result.putParcelable(KEY_INTENT, intent);
        return result;
    }

    @Override
    public String getAuthTokenLabel(@NonNull String authTokenType) {
        if (DEBUG) {
            Log.v(LOG_TAG, "getAuthTokenLabel: " + authTokenType);
        }

        return null;
    }

    @Override
    public Bundle hasFeatures(
            @NonNull AccountAuthenticatorResponse response,
            @NonNull Account account,
            @Nullable String[] features
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "hasFeatures: " + account + ", " + Arrays.toString(features));
        }

        return null;
    }

    @Override
    public Bundle updateCredentials(
            @NonNull AccountAuthenticatorResponse response,
            @NonNull Account account,
            @Nullable String accountTokenType,
            @Nullable Bundle options
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "updateCredentials: " + account + ", " + accountTokenType + ", " + options);
        }

        return null;
    }
}
