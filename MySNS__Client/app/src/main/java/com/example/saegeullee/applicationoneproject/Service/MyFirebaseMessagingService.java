package com.example.saegeullee.applicationoneproject.Service;

import android.util.Log;

import com.example.saegeullee.applicationoneproject.Utility.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingServic";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "onMessageReceived: " + remoteMessage.getNotification());

        if(remoteMessage.getNotification() != null) {

            NotificationHelper.displayNotification(getApplicationContext(),
                    remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

        }
    }
}
