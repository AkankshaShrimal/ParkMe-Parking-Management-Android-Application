package com.android.parkme.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.parkme.utils.Globals;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class MessagingService extends FirebaseMessagingService {
    public static final BehaviorSubject<Object> subject = BehaviorSubject.create();
    private static final String TAG = "MessagingService";

    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> m = message.getData();
        m.entrySet().stream().spliterator().forEachRemaining(c -> System.out.println(c.getKey() + " : " + c.getValue()));
        SharedPreferences sharedPreferences = getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        if (Globals.NOTIFICATION_CHAT.equals(m.get(Globals.NOTIFICATION_TYPE)))
            HandleFirebaseMessage.handleChatNotification(getApplicationContext(), sharedPreferences, m, subject);
        else if (Globals.NOTIFICATION_TOPIC.equals(m.get(Globals.NOTIFICATION_TYPE)))
            HandleFirebaseMessage.handleAnnouncementPushNotification(getApplicationContext(), m);
        else if (Globals.NOTIFICATION_RAISE.equals(m.get(Globals.NOTIFICATION_TYPE)))
            HandleFirebaseMessage.handleRaiseQueryPushNotification(getApplicationContext(), sharedPreferences, m);
        else if (Globals.NOTIFICATION_CLOSE.equals(m.get(Globals.NOTIFICATION_TYPE)))
            HandleFirebaseMessage.handleCloseQueryPushNotification(getApplicationContext(), m);
        else if (Globals.NOTIFICATION_CANCEL.equals(m.get(Globals.NOTIFICATION_TYPE)))
            HandleFirebaseMessage.handleCancelQueryPushNotification(getApplicationContext(), m);
        else
            Log.i(TAG, "message received.. but didn't match any criteria");
    }

    public void onNewToken(String token) {
        Log.i(TAG, "new Token generated:" + token);
        SharedPreferences sharedPreferences = getSharedPreferences(Globals.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Globals.TOKEN, token);
        editor.commit();
    }

}
