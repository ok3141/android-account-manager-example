package com.samugg.example;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

public class AuthenticatorService extends Service {
    @NonNull
    private final AccountAuthenticator authenticator = new AccountAuthenticator(this);

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
