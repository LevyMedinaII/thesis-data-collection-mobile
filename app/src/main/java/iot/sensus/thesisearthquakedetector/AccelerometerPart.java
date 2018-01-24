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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Levy on 1/22/2018.
 */

public class AccelerometerPart extends Service implements SensorEventListener {
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private double mLastX = 0, mLastY = 0;
    private List<Double> mData5s;
    private List<Long> mTime5s;
    private long mStartTime = 0;
    private long mLastUpdate = 0;
    private String url;

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
        if(mAccelerometer.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];

            long currTime = System.currentTimeMillis();

            if(mData5s.size()==500){
                Map<Long,Double> params = new HashMap<>();
                for(int i=0; i<mData5s.size(); i++) params.put(mTime5s.get(i),mData5s.get(i));

                JSONObject jsonObject = new JSONObject(params);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET, url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
                );

                Volley.newRequestQueue(this).add(jsonObjectRequest);
                mData5s.clear();
                mTime5s.clear();


            }

            if(currTime-mLastUpdate>10){

                long diffTime = currTime-mLastUpdate;
                mLastUpdate = currTime;

                double disX = (double) (mLastX*diffTime*diffTime-0.5*x*diffTime);
                double disY = (double) (mLastY*diffTime*diffTime-0.5*y*diffTime);

                double disXY = (double) (Math.sqrt(Math.pow(disX,2)+Math.pow(disY,2)));


                mData5s.add(disXY);

                mTime5s.add(currTime-mStartTime);

                mLastX=x;
                mLastY=y;



            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
