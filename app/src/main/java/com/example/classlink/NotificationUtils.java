package com.example.classlink;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat.InboxStyle;

import java.util.List;

public class NotificationUtils {
    public static final String CHANNEL_ID = "notes_channel";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notes notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for newly uploaded notes");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    // lines = list of short lines to show in the notification (title/subject/uploader)
    public static void showNewNotesNotification(Context context, List<String> lines) {
        String title = (lines.size() == 1) ? "1 new note" : (lines.size() + " new notes");

        InboxStyle inboxStyle = new InboxStyle();
        for (int i = 0; i < lines.size() && i < 5; i++) { // show up to 5 lines
            inboxStyle.addLine(lines.get(i));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with your app icon
                .setContentTitle(title)
                .setStyle(inboxStyle)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}
