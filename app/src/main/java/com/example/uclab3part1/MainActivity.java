package com.example.uclab3part1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel(
                "channel1",
                "My Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        manager.createNotificationChannel(channel);

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }

        Button notifyButton = findViewById(R.id.notifyButton);

        notifyButton.setOnClickListener(v -> {

            Intent intent = new Intent(this, NotificationActivity.class);
            intent.putExtra("message", "Hello from the notification!");

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Notification notification = new Notification.Builder(this, "channel1")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("New Notification")
                    .setContentText("Hello! This is a test notification.")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            manager.notify(1, notification);
        });
    }
}