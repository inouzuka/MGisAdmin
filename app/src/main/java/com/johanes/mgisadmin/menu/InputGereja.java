package com.johanes.mgisadmin.menu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.johanes.mgisadmin.R;
import com.johanes.mgisadmin.helper.DatabaseHelper;
import com.johanes.mgisadmin.helper.DatabaseHelperGereja;
import com.johanes.mgisadmin.helper.InputValidation;
import com.johanes.mgisadmin.utils.Gereja;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class InputGereja extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Check";
    //    private static final String TAG = InputGereja.TAG;
    final int REQUEST_CODE_GALLERY = 999;

    private boolean editMode;
    private int gereja_id;

    private final AppCompatActivity activity = InputGereja.this;

    private NestedScrollView nestedScrollView;
    private TextInputLayout textInputNamaGereja;
    private TextInputLayout textInputAlamatGereja;
    private TextInputLayout textInputDeskripsiGereja;
    private TextInputLayout textInputLayoutLongi;
    private TextInputLayout textInputLayoutLati;

    private TextInputEditText textInputEditTextNamaGereja;
    private TextInputEditText textInputEditTextAlamatGereja;
    private TextInputEditText textInputEditTextDescGereja;
    private TextInputEditText textInputEditTextLong;
    private TextInputEditText textInputEditTextLati;

    //temps
    String nama,alamat,desk,lat,lng;
    byte[] img;

    private ImageView imageView;

    private AppCompatButton buttonAdd;
    private AppCompatButton buttonAddPic;

    private AppCompatTextView textViewPicName;

    private InputValidation inputValidation;
    //private DatabaseHelperGereja dbHelper;
    private DatabaseHelper dbHelper;
    private Gereja gereja;

    private ProgressBar progressBarInput;
    private int progressBarStatus = 0;

    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_gereja);
        initViews();
        initListeners();
        initObject();

        //dbHelper = new DatabaseHelperGereja(this, "GerejaData.sqlite", null, 1);
        dbHelper = DatabaseHelper.openSharedDB(this); //new DatabaseHelper(this);

        editMode = false;
        Intent in = getIntent();
        if(in.hasExtra("id")){
            editMode = true;
            gereja_id = in.getIntExtra("id",0);

            textInputEditTextNamaGereja.setText(in.getStringExtra("nama"));
            textInputEditTextAlamatGereja.setText(in.getStringExtra("alamat"));
            textInputEditTextDescGereja.setText(in.getStringExtra("deskripsi"));
            textInputEditTextLati.setText(in.getStringExtra("lat"));
            textInputEditTextLong.setText(in.getStringExtra("long"));

            byte[] bm = in.getByteArrayExtra("img");
            Bitmap bi = BitmapFactory.decodeByteArray(bm,0,bm.length);
            imageView.setImageBitmap(bi);

            buttonAdd.setText("Update Gereja");
        }
    }

    private void initViews() {
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        textInputNamaGereja = (TextInputLayout) findViewById(R.id.textInputLayoutNamaGereja);
        textInputAlamatGereja = (TextInputLayout) findViewById(R.id.textInputLayoutAlamatGereja);
        textInputDeskripsiGereja = (TextInputLayout) findViewById(R.id.textInputLayoutDescGereja);

        textInputEditTextNamaGereja = (TextInputEditText) findViewById(R.id.textInputEditTextNamaGereja);
        textInputEditTextAlamatGereja = (TextInputEditText) findViewById(R.id.textInputEditTextAlamatGereja);
        textInputEditTextDescGereja = (TextInputEditText) findViewById(R.id.textInputEditTextDescGereja);

        textInputEditTextLati = (TextInputEditText) findViewById(R.id.textInputEditTextLati);
        textInputEditTextLong = (TextInputEditText) findViewById(R.id.textInputEditTextLong);

//        latiVar = Double.parseDouble(textInputEditTextLati.getText().toString().trim());
//        longiVar = Double.parseDouble(textInputEditTextLong.getText().toString().trim());

        buttonAdd = (AppCompatButton) findViewById(R.id.appCompatButtonAddGereja);
        buttonAddPic = (AppCompatButton) findViewById(R.id.buttonUploadImage);
        textViewPicName = (AppCompatTextView) findViewById(R.id.textViewPicAddress);

        imageView = (ImageView) findViewById(R.id.uploadedImage);

        progressBarInput  = (ProgressBar) findViewById(R.id.progressBar6);
    }

    private void initListeners() {
        buttonAdd.setOnClickListener(this);
        buttonAddPic.setOnClickListener(this);
    }

    private void initObject() {
//        dbHelper = new DatabaseHelperGereja(activity);
        inputValidation = new InputValidation(activity);
//        gereja = new Gereja();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonUploadImage:
                chooseImage();
                break;

            case R.id.appCompatButtonAddGereja:
                progressInput(progressBarStatus);
                saveToSql();


                if(editMode){
                    Intent in = new Intent(getApplicationContext(), DetailGereja.class);
                    in.putExtra("id", gereja_id);
                    in.putExtra("nama",nama);
                    in.putExtra("alamat", alamat);
                    in.putExtra("deskripsi", desk);
                    in.putExtra("long", lng);
                    in.putExtra("lat", lat);
                    in.putExtra("img", img);
                    finish();
                    startActivity(in);
                }
                break;


        }

    }

    private void progressInput(final int progressBarStatus) {
        // set the progress
        progressBarInput.setProgress(progressBarStatus);
        // thread is used to change the progress value
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressBarStatus < 100) {
//                    doOperation();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBarInput.setProgress(progressBarStatus + 10);
                }
            }
        });
        thread.start();
    }

//    private int doOperation() {

//        while (progressBarStatus <= 10000){
//            progressBarStatus ++;
//            try {
//                if ( == 1){
//                    return 33;
//                } else {
//
//                }
//            }
//        }
//
//        return 100;
//    }
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
    private Address getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address_;
//        Address p1 = null;

        try {
            address_ = coder.getFromLocationName(strAddress, 5);
            if (address_ == null) {
                return null;
                }
            return address_.get(0);
//            Address location = address_.get(0);
//            location.getLatitude();
//            location.getLongitude();
//            return location;

        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

//        return p1;
    }



    private void saveToSql() {

        Address addr = null;

        if (!inputValidation.isInputEditTextFilled(textInputEditTextNamaGereja, textInputNamaGereja,
                getString(R.string.error_message_name))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextAlamatGereja, textInputAlamatGereja,
                getString(R.string.error_message_alamat))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextDescGereja, textInputDeskripsiGereja,
                getString(R.string.error_message_desk))) {
            return;
        }

        if(editMode){
            try{

                nama = textInputEditTextNamaGereja.getText().toString().trim();
                alamat = textInputEditTextAlamatGereja.getText().toString().trim();
                desk = textInputEditTextDescGereja.getText().toString().trim();

                addr = getLocationFromAddress(alamat);
                if (addr != null) {
                    lat = ""+addr.getLatitude();
                }
                if (addr != null) {
                    lng = ""+addr.getLongitude();
                }

//                lng = textInputEditTextLong.getText().toString().trim();
//                lat = textInputEditTextLati.getText().toString().trim();
                img = imageViewToByte(imageView);
                dbHelper.updateGereja(gereja_id,
                        nama,
                        alamat,
                        desk,
                        Double.parseDouble(lng),
                        Double.parseDouble(lat),
                        img
                );
                Snackbar.make(nestedScrollView, getString(R.string.add_message), Snackbar.LENGTH_LONG).show();
                //emptyInputEditText();
                imageView.setImageResource(R.drawable.church);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        } else {
            try{
                alamat = textInputEditTextAlamatGereja.getText().toString().trim();
                addr = getLocationFromAddress(alamat);
                if (addr != null) {
                    lat = ""+addr.getLatitude();
                }
                if (addr != null) {
                    lng = ""+addr.getLongitude();
                }

                dbHelper.addGereja(
                        textInputEditTextNamaGereja.getText().toString().trim(),
//                        textInputEditTextAlamatGereja.getText().toString().trim(),
                        alamat,
                        textInputEditTextDescGereja.getText().toString().trim(),
                        Double.parseDouble(lng),
                        Double.parseDouble(lat),

//                        Double.parseDouble(textInputEditTextLong.getText().toString().trim()),
//                        Double.parseDouble(textInputEditTextLati.getText().toString().trim()),
                        imageViewToByte(imageView)
                );
                Snackbar.make(nestedScrollView, getString(R.string.add_message), Snackbar.LENGTH_LONG).show();
                emptyInputEditText();
                imageView.setImageResource(R.drawable.church);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void emptyInputEditText() {
        textInputEditTextNamaGereja.setText(null);
        textInputEditTextAlamatGereja.setText(null);
        textInputEditTextDescGereja.setText(null);
    }


    private void chooseImage() {
        ActivityCompat.requestPermissions(
                InputGereja.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_GALLERY
        );
    }

    private byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            Log.i(TAG, "onRequestPermissionsResult: "+ REQUEST_CODE_GALLERY);
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Snackbar.make(nestedScrollView, getString(R.string.error_user_access),
                        Snackbar.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
