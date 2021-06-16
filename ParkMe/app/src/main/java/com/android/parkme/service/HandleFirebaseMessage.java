package com.android.parkme.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.android.parkme.R;
import com.android.parkme.database.Announcement;
import com.android.parkme.database.Chat;
import com.android.parkme.database.DatabaseClient;
import com.android.parkme.database.Query;
import com.android.parkme.main.MainActivity;
import com.android.parkme.utils.Functions;
import com.android.parkme.utils.Globals;

import java.util.Map;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class HandleFirebaseMessage {
    private static final String TAG = "HandleFirebaseMessage";

    public static void handleRaiseQueryPushNotification(Context context, SharedPreferences sharedpreferences, Map<String, String> m) {

        saveQuery(context, new Query(Integer.parseInt(m.get(Globals.QID)),
                m.get(Globals.STATUS),
                m.get(Globals.FROM_USER_NAME),
                Integer.parseInt(m.get(Globals.FROM_USER_ID)),
                sharedpreferences.getString(Globals.NAME, ""),
                sharedpreferences.getInt(Globals.ID, 0),
                Long.parseLong(m.get(Globals.CREATE_TIME)),
                m.get(Globals.CHAT_MESSAGE),
                m.get(Globals.VEHICLE_REGISTRATION_NUMBER)));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Globals.NOTIFICATION_CHANNEL_ID, "My Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Globals.CHAT_MESSAGE, m.get(Globals.CHAT_MESSAGE));
        intent.putExtra(Globals.DATE, m.get(Globals.DATE));
        intent.putExtra(Globals.QID, m.get(Globals.QID));

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Globals.NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_identity_card)
                .setTicker("Hearty365")
                .setContentTitle(m.get(Globals.TITLE))
                .setContentText(m.get(Globals.CHAT_MESSAGE))
                .setContentIntent(pendingIntent);
        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

    public static void handleCancelQueryPushNotification(Context context, Map<String, String> m) {
        DatabaseClient.getInstance(context).getAppDatabase().parkMeDao().updateCancelRequest(Globals.QUERY_CANCEL_STATUS, Long.parseLong(m.get(Globals.CLOSE_TIME)), Integer.parseInt(m.get(Globals.QID)));
    }

    public static void handleCloseQueryPushNotification(Context context, Map<String, String> m) {
        DatabaseClient.getInstance(context).getAppDatabase().parkMeDao().updateCloseRequest(Globals.QUERY_CLOSE_STATUS, Long.parseLong(m.get(Globals.CLOSE_TIME)), Integer.parseInt(m.get(Globals.QID)), Float.parseFloat(m.get(Globals.RATING)));
    }

    public static void handleAnnouncementPushNotification(Context context, Map<String, String> m) {
        DatabaseClient.getInstance(context).getAppDatabase().parkMeDao().insert(new Announcement(Long.parseLong(m.get(Globals.TIME).toString()), m.get(Globals.MESSAGE).toString()));
    }

    public static void handleChatNotification(Context context, SharedPreferences sharedpreferences, Map<String, String> m, BehaviorSubject subject) {
        Chat chat = new Chat(
                Integer.parseInt(m.get(Globals.QID)),
                Integer.parseInt(m.get(Globals.FROM_USER_ID)),
                sharedpreferences.getInt(Globals.ID, 0),
                Long.parseLong(m.get(Globals.TIME)),
                m.get(Globals.CHAT_MESSAGE));
        chat.setStatus(1);
        saveChat(context, chat, subject);
    }

    public static void saveQuery(Context context, Query query) {
        Functions.printJson(query);
        DatabaseClient.getInstance(context).getAppDatabase().parkMeDao().insert(query);
    }

    private static void saveChat(Context context, Chat chat, BehaviorSubject subject) {
        DatabaseClient.getInstance(context).getAppDatabase().parkMeDao().insert(chat);
        subject.onNext(chat);
    }

}