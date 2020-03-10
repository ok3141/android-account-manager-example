package com.samugg.example;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private AuthPreferences mAuthPreferences;
    private String authToken;

    private TextView text1;
    private TextView text2;
    private TextView text3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = findViewById(android.R.id.text1);
        text2 = findViewById(android.R.id.text2);
        text3 = findViewById(R.id.text3);

        authToken = null;
        mAuthPreferences = new AuthPreferences(this);
        mAccountManager = AccountManager.get(this);

        // Ask for an auth token
        AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(AccountUtils.ACCOUNT_TYPE, AccountUtils.AUTH_TOKEN_TYPE, null, this, null, null, new GetAuthTokenCallback(), null);

        Toast.makeText(getApplicationContext(), "Future: " + future, Toast.LENGTH_SHORT).show();
    }

    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;

            Toast.makeText(getApplicationContext(), "GetAuthTokenCallback", Toast.LENGTH_SHORT).show();

            try {
                bundle = result.getResult();

                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (null != intent) {
                    startActivityForResult(intent, REQ_SIGNUP);
                } else {
                    authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);

                    // Save session username & auth token
                    mAuthPreferences.setAuthToken(authToken);
                    mAuthPreferences.setUsername(accountName);

                    text1.setText("Retrieved auth token: " + authToken);
                    text2.setText("Saved account name: " + mAuthPreferences.getAccountName());
                    text3.setText("Saved auth token: " + mAuthPreferences.getAuthToken());

                    // If the logged account didn't exist, we need to create it on the device
                    Account account = AccountUtils.getAccount(MainActivity.this, accountName);

                    Toast.makeText(getApplicationContext(), "Account: " + account, Toast.LENGTH_SHORT).show();

                    if (null == account) {
                        account = new Account(accountName, AccountUtils.ACCOUNT_TYPE);
                        mAccountManager.addAccountExplicitly(account, bundle.getString(LoginActivity.PARAM_USER_PASSWORD), null);
                        mAccountManager.setAuthToken(account, AccountUtils.AUTH_TOKEN_TYPE, authToken);
                    }
                }
            } catch (OperationCanceledException e) {
                // If signup was cancelled, force activity termination
                finish();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close_session) {// Clear session and ask for new auth token
            mAccountManager.invalidateAuthToken(AccountUtils.ACCOUNT_TYPE, authToken);
            mAuthPreferences.setAuthToken(null);
            mAuthPreferences.setUsername(null);
            mAccountManager.getAuthTokenByFeatures(AccountUtils.ACCOUNT_TYPE, AccountUtils.AUTH_TOKEN_TYPE, null, this, null, null, new GetAuthTokenCallback(), null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
