package iot.sensus.thesisearthquakedetector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, EarthquakeDataGatherService.class));
        // Coarse Location Permission
        if( ContextCompat.checkSelfPermission( getBaseContext(),
                "android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission( getBaseContext(),
                "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED)
        {
            try {

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            final int REQUEST_CODE_ASK_PERMISSIONS = 124;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            "android.permission.ACCESS_COARSE_LOCATION",
                            "android.permission.ACCESS_COARSE_LOCATION"
                    }, REQUEST_CODE_ASK_PERMISSIONS);
        }

        // Fine Location Permission
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
            try {

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            final int REQUEST_CODE_ASK_PERMISSIONS = 124;
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
}
