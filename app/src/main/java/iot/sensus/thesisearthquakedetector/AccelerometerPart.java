package iot.sensus.thesisearthquakedetector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Levy on 1/22/2018.
 */

public class AccelerometerPart extends Service implements SensorEventListener {
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private double mLastX = 0, mLasty = 0;
    private List<Double> mData5s;
    private List<Long> mTime5s;
    private long mStartTime = 0;
    private long mLastUpdate = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }

    @Override
    public void onCreate() {
        initializeSensorManager();
    }

    private void initializeSensorManager(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_FASTEST);

        return;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this,mSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mAccelerometer = sensorEvent.sensor;
        if(mAccelerometer == Sensor.TYPE_LINEAR_ACCELERATION){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];

            long currTime = System.currentTimeMillis();

            if(currTime-mLastUpdate>5000){
                //TODO::put volley

            }

            if(currTime-mLastUpdate>10){
                //TODO::put accelerometer process to displacement

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
