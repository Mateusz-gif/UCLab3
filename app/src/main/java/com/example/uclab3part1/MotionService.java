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

public class MotionService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private HandlerThread sensorThread;
    private Handler sensorHandler;

    private float lastX, lastY, lastZ;
    private boolean firstReading = true;

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
            Log.d("MotionService", "Movement Detected!");
        }

        lastX = x;
        lastY = y;
        lastZ = z;
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
