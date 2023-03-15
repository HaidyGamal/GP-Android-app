package com.example.publictransportationguidance.Tracking;

import static com.example.publictransportationguidance.HelperClasses.Constants.PERMISSIONS;
import static com.example.publictransportationguidance.HelperClasses.Constants.PERMISSION_ALL;
import static com.example.publictransportationguidance.HelperClasses.Constants.TRACKER_BASE_TXT;
import static com.example.publictransportationguidance.HelperClasses.Functions.getLocationName;

import com.bumptech.glide.Glide;
import com.example.publictransportationguidance.R;
import com.example.publictransportationguidance.databinding.LiveLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class LiveLocation extends AppCompatActivity {
    private Handler mHandler;
    private Runnable mRunnable;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    LiveLocationBinding binding;
    private String tempLocation = "";
    private String tempLoc = "";
    double tempLatitude = 0.000;
    double tempLongitude = 0.000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.live_location);
        checkLocationEnabled();
        Glide.with(this).load(R.drawable.my_gif).into(binding.gifImageView);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();                                                          /* M Osama:  Create a LocationRequest object to specify the desired interval for location updates */
        mLocationRequest.setInterval(10000);     /* M Osama: Update Location Every 10 sec */

        /* M Osama: Callback Function handling location continues updates */
        tempLocation = binding.locationTxt.getText().toString();
        tempLoc = tempLatitude + "," + tempLongitude;
        mLocationCallback = new LocationCallback() {

            @SuppressLint("MissingPermission")
            @Override
            public void onLocationResult(LocationResult locationResult) {


                if (locationResult != null) {                                                          // Display the latitude and longitude of the user's current location in the TextView
                    Location location = locationResult.getLastLocation();

                    tempLatitude = location.getLatitude();
                    tempLongitude = location.getLongitude();

                    /* M Osama: Update the TextView with the latitude and longitude of the new location */
                    binding.locationTxt.setText(TRACKER_BASE_TXT + getLocationName(getApplicationContext(), tempLatitude, tempLongitude));

                    //haidy:to check if location has been changed
                    if (!(tempLatitude + "," + tempLongitude).equals(tempLoc)) {
                        Toast.makeText(getApplicationContext(), R.string.YourLocationHasBeenChanged + tempLatitude + ", " + tempLongitude, Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(500);                           // haidy: vibrates for 500 milliseconds
                    }
                    if (!binding.locationTxt.getText().toString().equals(tempLocation)) {
                        Toast.makeText(getApplicationContext(), R.string.YourLocationHasBeenChanged, Toast.LENGTH_SHORT).show();
                        vibrator.vibrate(500);                           // haidy: vibrates for 500 milliseconds
                    }

                    Bundle bundle = getIntent().getExtras();
                    if (bundle != null) {
                        String destination = bundle.getString("data");   // haidy:Receive destination from fragment
                        if (binding.locationTxt.getText().toString().equals(destination)) {
                            vibrator.vibrate(1500);
                            Toast.makeText(getApplicationContext(), R.string.YouReachedYourDestination, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }

            }
        };
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {                    // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);  // Request location permission if it is not granted
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());                // Request location updates using FusedLocationProviderClient
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }      // Location permission granted, start receiving location updates
            else {
                Toast.makeText(this, "نرجو تفعيل استخدام مكانك دائما", Toast.LENGTH_SHORT).show();
            }                   // Location permission denied, display an error message
        }
    }

    /* M Osama: Start receiving location updates when the activity starts*/
    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }

    /* M Osama: Stop receiving location updates when the activity stops */
    @Override
    protected void onStop() {
        super.onStop();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /* M Osama: Inform User to keep Location Opened to be able to track Him */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, R.string.PleaseKeepLocationPermission, Toast.LENGTH_SHORT).show();
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "نرجو تفعيل استخدام مكانك للمتابعة", Toast.LENGTH_LONG).show();
        }
    }

}
