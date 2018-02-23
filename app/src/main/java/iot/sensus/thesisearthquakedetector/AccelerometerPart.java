package iot.sensus.thesisearthquakedetector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
    private double timeInSec = 0;
    private List<Double> mData5s;
    private List<Double> mAcc5s;
    private List<Double> mTime5s;
    private List<Double> mVel5s;
    private long mStartTime = 0;
    private long mLastUpdate = 0;
    private String url = "https://cc69e0ab.ngrok.io/data";
    double latitude, longitude;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        initializeSensorManager();
    }

    private void initializeSensorManager() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mData5s = new ArrayList<Double>();
        mTime5s = new ArrayList<Double>();
        mAcc5s = new ArrayList<Double>();
        mVel5s = new ArrayList<Double>();
        mStartTime = System.currentTimeMillis();

        return;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this, mSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mAccelerometer = sensorEvent.sensor;
        if (mAccelerometer.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];

            long currTime = System.currentTimeMillis();

            if (mData5s.size() == 20) {
                Map<Double, Double> params = new HashMap<>();

                for (int i = 0; i < mData5s.size(); i++) params.put(mTime5s.get(i), mData5s.get(i));

                JSONObject jsonObject = new JSONObject();

                JSONArray jsonTimeArray = new JSONArray();
                JSONArray jsonDisArray = new JSONArray();
                JSONArray jsonAccArray = new JSONArray();

                jsonTimeArray.put(mTime5s);
                jsonDisArray.put(mData5s);
                jsonAccArray.put(mAcc5s);

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    this.stopSelf();
                }

                try {
                    jsonObject.put("Time", jsonTimeArray);
                    jsonObject.put("Displacement", jsonDisArray);
                    jsonObject.put("Acceleration",jsonAccArray);
                    jsonObject.put("PGA", determinePeakAcc(mAcc5s));
                    jsonObject.put("PGV", determinePeakVel(mVel5s));
                    jsonObject.put("PGD", determinePeakDis(mData5s));

                    if(lm != null) {
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

                        jsonObject.put("Latitude", latitude);
                        jsonObject.put("Longitude", longitude);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("RESPONSE SERVER", response.toString(4));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

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
                mVel5s.clear();

            }

            if(currTime-mLastUpdate>=50){

                long diffTime = currTime-mLastUpdate;
                mLastUpdate = currTime;

                //displacement
                double disX = (double) (mLastX*diffTime*diffTime-0.5*x*diffTime)/1000000;
                double disY = (double) (mLastY*diffTime*diffTime-0.5*y*diffTime)/1000000;
                double disXY = (double) (Math.sqrt(Math.pow(disX,2)+Math.pow(disY,2)));

                double accXY = (double) ((Math.sqrt(Math.pow(x,2)+Math.pow(y,2)))/9.8);

                //acceleration


                //velocity
                double velX = (double) (mLastX*diffTime+x*diffTime);
                double velY = (double) (mLastY*diffTime+y*diffTime);
                double velXY = (double) (Math.sqrt(Math.pow(velX,2)+Math.pow(velY,2)));



                Log.d("Acceleratio:", String.valueOf(accXY));
                mAcc5s.add(accXY);
                mData5s.add(disXY);
                mVel5s.add(velXY);

                timeInSec = (double) (currTime-mStartTime)/1000;
                mTime5s.add(timeInSec);

                mLastX=x;
                mLastY=y;



            }
        }
    }

    public double determinePeakDis(List<Double> dispData){
        double peakDis = 0;

        for(double disp : dispData)
            if(peakDis<disp)
                peakDis = disp;

        return peakDis;
    }

    public double determinePeakAcc(List<Double> accData){
        double peakAcc = 0;

        for(double acc : accData)
            if(peakAcc<acc)
                peakAcc = acc;

        return peakAcc;
    }

    public double determinePeakVel(List<Double> velData){
        double peakVel = 0;
        for(double vel : velData)
            if(peakVel<vel)
                peakVel = vel;

        return peakVel;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
