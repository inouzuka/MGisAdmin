package com.johanes.mgisadmin.menu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.johanes.mgisadmin.R;
import com.johanes.mgisadmin.helper.DatabaseHelper;
import com.johanes.mgisadmin.utils.Gereja;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private double cLat, cLong;
    private GoogleMap mMap;
    private Marker cMarker, gMarker;
    private Polyline cPoly;

    private Gereja gClosest;
    private LatLng gLoc;

    private String bestProvider = null;


    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseHelper dh;

    private boolean fixed_mode;

    private TelephonyManager myTelephonyManager;
    private PhoneStateListener phoneStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_view);

        // cek permission u/ M
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dh = DatabaseHelper.openSharedDB(this); // new DatabaseHelper(this);

        fixed_mode = false;
        Intent in = getIntent();
        if (in.hasExtra("id")) {
            fixed_mode = true;

            gClosest = new Gereja();

            gClosest.setId(in.getIntExtra("id", 0));

            gClosest.setName(in.getStringExtra("nama"));
            gClosest.setAlamat(in.getStringExtra("alamat"));
            gClosest.setDeskripsi(in.getStringExtra("deskripsi"));
            gClosest.setLati(in.getStringExtra("lat"));
            gClosest.setLongi(in.getStringExtra("long"));
            gClosest.setImage(in.getByteArrayExtra("img"));

            gLoc = new LatLng(Double.parseDouble(gClosest.getLati()), Double.parseDouble(gClosest.getLongi()));
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    void addPolylineToMap(List<LatLng> locs){
        mMap.addPolyline(new PolylineOptions().addAll(locs));

    }

    Gereja getClosestChurch(LatLng myPos) {
        double closestNow = Double.POSITIVE_INFINITY;
        Gereja g = null;
        double lt, lg, jarak;

        List<Gereja> churches = dh.getAllGereja();
        for (Gereja x : churches) {
            lt = Double.parseDouble(x.getLati());
            lg = Double.parseDouble(x.getLongi());
            jarak = Math.sqrt(Math.pow(lt - myPos.latitude, 2) + Math.pow(lg - myPos.longitude, 2));

            if (jarak < closestNow) {
                closestNow = jarak;
                g = x;
            }
        }

        return g;
    }

    LocationListener locationListenerGPS = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            cLat = location.getLatitude();
            cLong = location.getLongitude();
            LatLng nowp = new LatLng(cLat, cLong);

            if (!fixed_mode) {
                gClosest = getClosestChurch(nowp);
                if (gClosest != null)
                    gLoc = new LatLng(Double.parseDouble(gClosest.getLati()),
                            Double.parseDouble(gClosest.getLongi()));
            }

            String msg = "Posisi Berubah!";
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

            if (cMarker != null) cMarker.remove();
            cMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(cLat, cLong)).title("Posisi Sekarang"));

            if (gClosest != null) {
                if (cPoly != null) cPoly.remove();
                cPoly = mMap.addPolyline(new PolylineOptions()
                        .clickable(true).color(Color.BLUE).add(nowp).add(gLoc));

                if (!fixed_mode) {
                    byte[] bm = gClosest.getImage();
                    Bitmap bi = BitmapFactory.decodeByteArray(bm, 0, bm.length);
                    bi = Bitmap.createScaledBitmap(bi, 100, 100, false);

                    if (gMarker != null) gMarker.remove();
                    gMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(gClosest.getLati()), Double.parseDouble(gClosest.getLongi())))
                            .icon(BitmapDescriptorFactory.fromBitmap(bi))
                            .alpha(0.9f)
                            .title(gClosest.getName()));
                }
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(nowp);
            if (gClosest != null) builder.include(gLoc);
            LatLngBounds bounds = builder.build();

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // cek koneksi internet yang tersedia
        // param koneksi status awal False
        // -------------------------------
//        myTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//
//        phoneStateListener = new PhoneStateListener(){
//            public void onDataConnectionStateChanged(int state){
//                switch(state){
//                    case TelephonyManager.DATA_DISCONNECTED:
//                        Log.i("State: ", "Offline");
//                        // String stateString = "Offline";
//                        // Toast.makeText(getApplicationContext(),
//                        // stateString, Toast.LENGTH_LONG).show();
//                        break;
//                    case TelephonyManager.DATA_SUSPENDED:
//                        Log.i("State: ", "IDLE");
//                        // stateString = "Idle";
//                        // Toast.makeText(getApplicationContext(),
//                        // stateString, Toast.LENGTH_LONG).show();
//                        break;
//                }
//            }
//        };
//        myTelephonyManager.listen(phoneStateListener,
//                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);

        // -----------------------------
        // get lokasi posisi hp sekarang
        // -----------------------------
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        bestProvider = locationManager.getBestProvider(criteria, true);
        // location = locationManager.getLastKnownLocation(bestProvider);
        try{
            if (bestProvider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
//			locationManager.requestLocationUpdates(bestProvider, 0, 1000,
//					locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListenerGPS);
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                try{
                    if(loc != null){
                        Toast.makeText(getBaseContext(),"Menggunakan Posisi Terakhir!",Toast.LENGTH_LONG).show();

                        LatLng yourPosNow = new LatLng(loc.getLatitude(), loc.getLongitude());
                        //LatLng yourPosNow = new LatLng(-8.5617, 115.1771);

                        if(!fixed_mode){
                            gClosest = getClosestChurch(yourPosNow);
                            if(gClosest != null) gLoc = new LatLng(Double.parseDouble(gClosest.getLati()), Double.parseDouble(gClosest.getLongi()));
                            else Toast.makeText(getBaseContext(),"Data Gereja Masih Kosong!",Toast.LENGTH_LONG).show();
                        }

                        if(gClosest != null){
                            byte[] bm = gClosest.getImage();
                            Bitmap bi = BitmapFactory.decodeByteArray(bm,0,bm.length);
                            bi = Bitmap.createScaledBitmap(bi,100,100,false);
                            gMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(gClosest.getLati()), Double.parseDouble(gClosest.getLongi())))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bi))
                                    .alpha(0.9f)
                                    .title(gClosest.getName()));
                        }

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(yourPosNow);
                        if(gClosest != null) builder.include(gLoc);
                        LatLngBounds bounds = builder.build();

                        //mMap.setPadding(100,100,100,100);
                        cMarker = mMap.addMarker(new MarkerOptions().position(yourPosNow).title("Posisi Sekarang"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

                        if(gClosest != null)
                            cPoly = mMap.addPolyline(new PolylineOptions()
                                    .clickable(true).color(Color.BLUE).add(yourPosNow).add(gLoc));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(yourPosNow));
                    }

                    if (loc == null){
                        Location locs = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Toast.makeText(getBaseContext(),"Menggunakan Posisi Terakhir!",Toast.LENGTH_LONG).show();

                        LatLng yourPosNow = new LatLng(locs.getLatitude(), locs.getLongitude());
                        //LatLng yourPosNow = new LatLng(-8.5617, 115.1771);

                        if(!fixed_mode){
                            gClosest = getClosestChurch(yourPosNow);
                            if(gClosest != null) gLoc = new LatLng(Double.parseDouble(gClosest.getLati()), Double.parseDouble(gClosest.getLongi()));
                            else Toast.makeText(getBaseContext(),"Data Gereja Masih Kosong!",Toast.LENGTH_LONG).show();
                        }

                        if(gClosest != null){
                            byte[] bm = gClosest.getImage();
                            Bitmap bi = BitmapFactory.decodeByteArray(bm,0,bm.length);
                            bi = Bitmap.createScaledBitmap(bi,100,100,false);
                            gMarker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(gClosest.getLati()), Double.parseDouble(gClosest.getLongi())))
                                    .icon(BitmapDescriptorFactory.fromBitmap(bi))
                                    .alpha(0.9f)
                                    .title(gClosest.getName()));
                        }

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(yourPosNow);
                        if(gClosest != null) builder.include(gLoc);
                        LatLngBounds bounds = builder.build();

                        //mMap.setPadding(100,100,100,100);
                        cMarker = mMap.addMarker(new MarkerOptions().position(yourPosNow).title("Posisi Sekarang"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

                        if(gClosest != null)
                            cPoly = mMap.addPolyline(new PolylineOptions()
                                    .clickable(true).color(Color.BLUE).add(yourPosNow).add(gLoc));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(yourPosNow));
                    }
                    else{
                        Toast.makeText(getBaseContext(),"Belum Mendapatkan Data Lokasi!",Toast.LENGTH_LONG).show();
                    }
                } catch (Exception a){
                    a.printStackTrace();
                }
            }
        } catch (Exception a){
            a.printStackTrace();
        }

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted. Do the
//                    // contacts-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(this,
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//
//                        if (mGoogleApiClient == null) {
//                            buildGoogleApiClient();
//                        }
//                        mMap.setMyLocationEnabled(true);
//                    }
//
//                } else {
//
//                    // Permission denied, Disable the functionality that depends on this permission.
//                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other permissions this app might request.
//            // You can add here other case statements according to your requirement.
//        }
//    }

}
