package com.johanes.mgisadmin.menu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.johanes.mgisadmin.helper.DatabaseHelper;
import com.johanes.mgisadmin.utils.Gereja;

import com.johanes.mgisadmin.R;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ListGereja extends AppCompatActivity {
    private DatabaseHelper db;

    private ListView gerejaListView;
    private Location loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_list_gereja);

        gerejaListView = (ListView) findViewById(R.id.listview_gereja);

        db = DatabaseHelper.openSharedDB(this); // new DatabaseHelper(this);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
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
        try {
            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception a){
            a.printStackTrace();
        }

        populateList();

    }

    @Override
    public void onRestart() {
        super.onRestart();

        populateList();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
    }

    private void populateList(){
        List<Gereja> ls = db.getAllGereja();

        //ArrayAdapter adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
        GerejaAdapter ga = new GerejaAdapter(this, new ArrayList<Gereja>(ls));
        gerejaListView.setAdapter(ga);
        gerejaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Gereja g = (Gereja)parent.getAdapter().getItem(position);
                Intent in = new Intent(getApplicationContext(), DetailGereja.class);
                in.putExtra("id", g.getId());
                in.putExtra("nama",g.getName());
                in.putExtra("alamat", g.getAlamat());
                in.putExtra("deskripsi", g.getDeskripsi());
                in.putExtra("long", g.getLongi());
                in.putExtra("lat", g.getLati());
                in.putExtra("img", g.getImage());
                startActivity(in);
            }
        });
    }

    private class GerejaAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private ArrayList<Gereja> mDataSource;

        public GerejaAdapter(Context context, ArrayList<Gereja> items) {
            mContext = context;
            mDataSource = items;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        //1
        @Override
        public int getCount() {
            return mDataSource.size();
        }

        //2
        @Override
        public Object getItem(int position) {
            return mDataSource.get(position);
        }

        //3
        @Override
        public long getItemId(int position) {
            return position;
        }

        //4
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get view for row item
            View rowView = mInflater.inflate(R.layout.act_list_items, parent, false);

            TextView namaGereja = (TextView) rowView.findViewById(R.id.textViewNamaGereja);
            TextView jarakGereja = (TextView) rowView.findViewById(R.id.textViewJarak);
            TextView alamatGereja = (TextView) rowView.findViewById(R.id.textViewAlamatGereja);
            ImageView gambarGereja = (ImageView) rowView.findViewById(R.id.uploadedImage);

            Gereja g = (Gereja) getItem(position);

            namaGereja.setText(g.getName());

            //calculating
            if(loc != null){
                Location loc2 = new Location("");
                loc2.setLatitude(Double.parseDouble(g.getLati()));
                loc2.setLongitude(Double.parseDouble(g.getLongi()));
                float jarak = loc2.distanceTo(loc);
                if(jarak > 1000.0){
                    jarak /= 1000.0;
                    jarakGereja.setText(String.format("%.1f km", jarak));
                } else {
                    jarakGereja.setText(String.format("%.1f meter", jarak));
                }
            } else {
                jarakGereja.setText("??? meter");
            }

            alamatGereja.setText(g.getAlamat());

            byte[] imgData = g.getImage();
            Bitmap bi = BitmapFactory.decodeByteArray(imgData,0,imgData.length);
            gambarGereja.setImageBitmap(bi);

            return rowView;
        }
    }
}
