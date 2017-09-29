package com.johanes.mgisadmin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.johanes.mgisadmin.menu.Login;

import java.util.Timer;
import java.util.TimerTask;

public class MSplash extends AppCompatActivity {

    private LocationManager locationManager = null;
    private String bestProvider = null;
    private double userLon = 0;
    private double userLat = 0;

    private RelativeLayout relativeLayout;

    private static final long JARAK_MINIMAL_UNTUK_UPDATE = 1; // dalam meter
    private static final long WAKTU_MINIMUM_UNTUK_UPDATE = 1000; // dalam detik
//
//    public static final String TAG = "MSplash";
//
//    private static final int REQUEST_LOCATION = 0;
//    private static final int REQUEST_STORAGE = 1;
//
//    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE};
//
//    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION};
//
//    private static String[] PERMISSIONS_PROVIDER = {Manifest.permission.ACCESS_NETWORK_STATE};

//    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.M};
    //    long Delay = 3000;
    long Delay = 100;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_msplash);
        mContext = this;
        cekInternet();
        initLocationManager();
        // Create a Time
        Timer RunSplash = new Timer();

        // Task to do when the timer ends
        TimerTask ShowSplash = new TimerTask() {
            @Override
            public void run() {
                // Close SplashScreenActivity.class
                finish();

                // Start MainActivity.class
                Intent myIntent = new Intent(MSplash.this,
                        Login.class);
                startActivity(myIntent);
            }
        };

        // Start the timer
        RunSplash.schedule(ShowSplash, Delay);
    }

    private void cekInternet() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                //we are connected to a network
                connected = true;
            } else {
                connected = false;
                Snackbar.make(relativeLayout, getString(R.string.no_internet_connection),
                        Snackbar.LENGTH_LONG).show();
            }

        } catch (Exception en){en.printStackTrace();}
    }


    private void initLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // List<String> listProviders = locationManager.getAllProviders();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        bestProvider = locationManager.getBestProvider(criteria, true);
        // location = locationManager.getLastKnownLocation(bestProvider);

        if (bestProvider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
//			locationManager.requestLocationUpdates(bestProvider, 0, 1000,
//					locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    WAKTU_MINIMUM_UNTUK_UPDATE, JARAK_MINIMAL_UNTUK_UPDATE, new MyLocationListener());
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            setUserLocation(loc);
        }

    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
//            String message = String.format(
//                    "Lokasi berubah \n Lon: %1$s \n Lat: %2$s",
//                    location.getLongitude(), location.getLatitude()
//            );
//            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
//            setUserLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle b) {
            Toast.makeText(mContext, "Status provider berubah",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(mContext,
                    "Provider disabled oleh user. GPS offline",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(mContext,
                    "Provider enabled oleh the user. GPS online",
                    Toast.LENGTH_LONG).show();
        }

    }
}
