package com.sharmatushar.depressiondetector.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sharmatushar.depressiondetector.Activities.SplashScreen;
import com.sharmatushar.depressiondetector.Model.UserData;
import com.sharmatushar.depressiondetector.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.sharmatushar.depressiondetector.App.CHANNEL_ID;

public class ActivityCollectionService extends Service implements SensorEventListener {
    private static final int COLLECTION_TIME = 5;

    public ActivityCollectionService() {
    }

    private SensorManager sensorManager;
    private ArrayList<UserData> sensorData;
    private Notification notification;
    private long lastRecordedTime = 0;
    private boolean arraySwitch = true;
    private ArrayList<Double> list1, list2;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Objects.equals(intent.getAction(), "START")) {
            if (notification == null) {
                Intent intent2 = new Intent(this, SplashScreen.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Depression Detector")
                        .setSmallIcon(R.drawable.ic_brain_small)
                        .setContentText("Collecting your activity.")
                        .setContentIntent(pendingIntent)
                        .build();
                Log.d("Tushar", "Service Started....");
                startForeground(1, notification);
                sensorData = new ArrayList<>();
                list1 = list2 = new ArrayList<>();
                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                assert sensorManager != null;
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, 1000000);
            }
        } else if (Objects.equals(intent.getAction(), "STOP")){
            sensorManager.unregisterListener(this);
            stopSelf();
        }
        return START_STICKY;
    }


    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     *
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     *
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param sensorEvent the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long currTime = System.currentTimeMillis();
        long TIME_INTERVAL = 1000;
        if (currTime - lastRecordedTime > TIME_INTERVAL) {
            lastRecordedTime = currTime;
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            @SuppressLint("SimpleDateFormat") DateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
            @SuppressLint("SimpleDateFormat") DateFormat simpleTime = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date(currTime);
            double magnitude = Math.sqrt(x*x + y*y + z*z);
            if (arraySwitch) {
                list1.add(magnitude);
                if (list1.size() == COLLECTION_TIME) {
                    Log.d("Tushar", "List 1");
                    arraySwitch = false;
                    calculateParams(simpleDate.format(date), simpleTime.format(date) ,list1);
                }
            } else {
                list2.add(magnitude);
                if (list2.size() == COLLECTION_TIME) {
                    Log.d("Tushar", "List 2");
                    arraySwitch = true;
                    calculateParams(simpleDate.format(currTime), simpleTime.format(date), list2);
                }
            }
        }
    }

    /**
     * Calculate the activity from accelerometer second wise data
     * @param date Date of data collection. Can be used as a file name.
     * @param time Time of record. Can be used as timestamp in csv
     * @param list List of second wise activity
     */
    private void calculateParams(final String date, final String time, final ArrayList<Double> list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Double> mu = new ArrayList<>();
                double sum = 0;
                for (int i = 0; i < list.size(); i++) {
                    sum += list.get(i);
                    if (((i + 1) % 5) == 0) {
                        mu.add(1.0/5 * sum);
                        sum = 0;
                    }
                }
                Log.d("Tushar", "Size of mu: " + mu.size());
                ArrayList<Double> sigma = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    sum += (list.get(i) - mu.get(i/5)) * (list.get(i) - mu.get(i/5));
                    if (((i + 1) % 5) == 0) {
                        sigma.add(Math.sqrt(1.0/5) * sum);
                        sum = 0;
                    }
                }
                Log.d("Tushar", "Size of mu: " + sigma.size());
                for (Double d:sigma) {
                    sum += d;
                }
                Log.d("Tushar", "Activity Index: " + sum);
                sensorData.add(new UserData(date, time, sum));
                list.clear();
                String currTime = time.trim().substring(0, time.length()-3);
                Log.d("Tushar", "Time now: " + currTime);
                Log.d("Tushar", "SensorData length: " + sensorData.size());
                if (currTime.equals("23:59")) {
                    ArrayList<UserData> uploadData = new ArrayList<>(sensorData);
                    sensorData.clear();
                    saveCSVFile(date, uploadData);
                    //TODO save to csv and upload data
                }
            }
        }).start();
    }

    private void saveCSVFile(String date,  ArrayList<UserData> uploadData) {
        String folderDirectory = Environment.getExternalStorageDirectory() + "/.DepressionDetector";
        String fileName = date + ".csv";
        File folder = new File(folderDirectory);
        if(!folder.exists()) {
            folder.mkdir();
        }
        File csvFile = new File(folderDirectory, fileName);
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
                FileWriter outputStream = new FileWriter(csvFile);
                outputStream.append("timestamp");
                outputStream.append(",");
                outputStream.append("activity");
                outputStream.append("\n");
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            try {
                FileWriter outputStream = new FileWriter(csvFile, true);
                for (UserData data: uploadData) {
                    outputStream.write(data.getTimestamp());
                    outputStream.write(",");
                    outputStream.write(String.valueOf(data.getActivity()));
                    outputStream.write("\n");
                }
                outputStream.close();
                //TODO upload and delete csv here
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     *
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor Accelerometer
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ArrayList<UserData> uploadData = new ArrayList<>(sensorData);
        Log.d("Tushar", "SensorData length: " + sensorData.size());
        if (!uploadData.isEmpty()) {
            saveCSVFile(uploadData.get(uploadData.size() - 1).getDate(), uploadData);
        } else {
            Log.d("Tushar", "SensorData empty");
        }
        //COMPLETED File Handling here.
    }
}
