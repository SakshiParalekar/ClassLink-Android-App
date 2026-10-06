package com.example.classlink;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "classlink_notes_channel";
    private static final String CHANNEL_NAME = "ClassLink Note Notifications";
    private static final String CHANNEL_DESC = "Notifications for new notes and updates in ClassLink.";

    public static void sendNotification(Context context, String title, String message) {
        // Create the notification channel (required for Android 8.0+)
        createNotificationChannel(context);

        // Create an intent to open the app when the user taps the notification
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Set the small notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Set the intent to be triggered on tap
                .setAutoCancel(true); // Automatically removes the notification when tapped

        // Show the notification
        NotificationManager notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private static void createNotificationChannel(Context context) {
        // Check if the Android version is Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
