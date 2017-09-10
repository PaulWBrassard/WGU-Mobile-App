package com.example.wgumobile;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

/**
 * NotificationReceiver is a receiver that displays a notification
 * based on the received title, text, and id
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String ID = "id";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Retrieve title, text, and id
        String title = intent.getExtras().getString(TITLE);
        String text = intent.getExtras().getString(TEXT);
        int id = intent.getIntExtra(ID, 0);
        //Use a NotificationManager to handle the notification
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setSmallIcon(R.drawable.owlwgu)
                .setContentTitle(title).setContentText(text).build();
        notificationManager.notify(id, notification);
    }
}
