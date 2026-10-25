package com.example.uclab3part1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MotionService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private HandlerThread sensorThread;
    private Handler sensorHandler;

    private float lastX, lastY, lastZ;
    private boolean firstReading = true;
    private long lastNotificationTime = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {

            if (sensorThread == null) {

                sensorThread = new HandlerThread("SensorThread");
                sensorThread.start();

                sensorHandler = new Handler(sensorThread.getLooper());

                sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL,
                        sensorHandler
                );

                Log.d("MotionService", "Monitoring started on separate thread");
            }

        } else {
            Log.d("MotionService", "No accelerometer found");
            stopSelf(startId);
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (firstReading) {
            lastX = x;
            lastY = y;
            lastZ = z;
            firstReading = false;
            return;
        }

        float movement = Math.abs(x - lastX)
                + Math.abs(y - lastY)
                + Math.abs(z - lastZ);

        if (movement > 5) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastNotificationTime > 5000) {
                lastNotificationTime = currentTime;
                sendNotification();
            }
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }


    private void sendNotification() {

        String time = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault()).format(new Date());

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("message", "Motion Detected at " + time);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        NotificationChannel channel = new NotificationChannel(
                "channel1",
                "Motion Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "channel1")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Motion Detected!")
                .setContentText("Your phone was moved at " + time)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        manager.notify(1, notification);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        if (sensorThread != null) {
            sensorThread.quitSafely();
            sensorThread = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
