package com.applozic.mobicomkit.sample.pushnotification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FcmListenerService extends FirebaseMessagingService {
    private static final String TAG = "ApplozicGcmListener";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "Message data:" + remoteMessage.getData());

        if (remoteMessage.getData().size() > 0) {
            if (Applozic.isApplozicNotification(this, remoteMessage.getData())) {
                Log.i(TAG, "Applozic notification processed");
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String registrationId) {
        super.onNewToken(registrationId);

        Log.i(TAG, "Found Registration Id:" + registrationId);

        Applozic.Store.setDeviceRegistrationId(this, registrationId);
        if (MobiComUserPreference.getInstance(this).isRegistered()) {
            try {
                RegistrationResponse registrationResponse = new RegisterUserClientService(this).updatePushNotificationId(registrationId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}