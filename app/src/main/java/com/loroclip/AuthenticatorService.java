package com.loroclip;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by angdev on 15. 5. 13..
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        Authenticator authenticator = new Authenticator(this);
        return authenticator.getIBinder();
    }
}
