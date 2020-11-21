package com.example.pocketbook.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.pocketbook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            sendOreoNotification(remoteMessage);
        }
        else{
            sendNotification(remoteMessage);
        }

    }

    private void sendOreoNotification(RemoteMessage remoteMessage){
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String date = remoteMessage.getData().get("date");
        String group = remoteMessage.getData().get("group");
        String icon = remoteMessage.getData().get("icon");

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        //TODO: add functionality to click on a notification

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title,body,icon,group);

        // generate an id for the notification from its date
        int j = 0;
        String[] s = date.split("[-:.]");
        for (int i = 0; i < s.length; i++) {
            j+= Integer.parseInt(s[i].replace(" ",""));
        }

        // show the notification and assign it a unique id so it does not get overwritten
        oreoNotification.getManager().notify(j,builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage){
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String date = remoteMessage.getData().get("date");
        String icon = remoteMessage.getData().get("icon");
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        //TODO: add functionality to click on a notification

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // generate an id for the notification from its date
        int j = 0;
        String[] s = date.split("[-:.]");
        for (int i = 0; i < s.length; i++) {
            j+= Integer.parseInt(s[i].replace(" ",""));
        }

        // show the notification and assign it a unique id so it does not get overwritten
        manager.notify(j, builder.build());

    }
}