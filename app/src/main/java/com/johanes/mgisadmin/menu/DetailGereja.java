package com.johanes.mgisadmin.menu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.johanes.mgisadmin.R;
import com.johanes.mgisadmin.helper.DatabaseHelper;

public class DetailGereja extends AppCompatActivity {
    private int gereja_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_gereja);

        Intent in = getIntent();
        if(in.hasExtra("id")){
            gereja_id = in.getIntExtra("id",0);

            TextView gName = (TextView) findViewById(R.id.tvGerejaName);
            TextView gDesk = (TextView) findViewById(R.id.tvGerejaDeskripsi);
            TextView gLL = (TextView) findViewById(R.id.tvLongLat);
            TextView gAl = (TextView) findViewById(R.id.tvAlamat);
            ImageView gImg = (ImageView) findViewById(R.id.ivGerejaImage);
            Button bHapus = (Button) findViewById(R.id.btnHapus);
            Button bEdit = (Button) findViewById(R.id.btnEdit);
            Button bShowMap = (Button) findViewById(R.id.btnShowMap);

            gName.setText(in.getStringExtra("nama"));
            gDesk.setText(in.getStringExtra("deskripsi"));
            gAl.setText(in.getStringExtra("alamat"));

            String lls = "Longitude: " + in.getStringExtra("long") + " Latitude: " + in.getStringExtra("lat");
            gLL.setText(lls);

            byte[] im = in.getByteArrayExtra("img");
            Bitmap bi = BitmapFactory.decodeByteArray(im,0,im.length);
            gImg.setImageBitmap(bi);

            bHapus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailGereja gj = (DetailGereja) v.getContext();
                    DatabaseHelper dh = DatabaseHelper.openSharedDB(getApplicationContext()); //new DatabaseHelper(getApplicationContext());
                    dh.deleteGereja(gj.gereja_id);
                    finish();
                    //Intent in = new Intent(getApplicationContext(), ListGereja.class);
                    //startActivity(in);
                }
            });

            bEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(getApplicationContext(), InputGereja.class);
                    in.putExtras(getIntent());
                    finish();
                    startActivity(in);
                }
            });

            bShowMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(getApplicationContext(), MapsActivity.class);
                    in.putExtras(getIntent());
                    //finish();
                    startActivity(in);
                }
            });
        }
    }
}
