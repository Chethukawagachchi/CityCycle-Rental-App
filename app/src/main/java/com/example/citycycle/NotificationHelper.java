package com.example.citycycle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {
    private Context context;
    private static final String CHANNEL_ID = "service_notifications";
    private static int notificationId = 0;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for service updates");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendServiceNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Make sure to have this icon in your drawable resources
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId++, builder.build());
    }
}