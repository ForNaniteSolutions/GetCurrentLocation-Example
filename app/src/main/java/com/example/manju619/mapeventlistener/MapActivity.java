package com.example.manju619.mapeventlistener;

import android.Manifest;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements LocationListener,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {


    GoogleMap googleMap;
    double clat, clng;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    final int LOCATION_PERMISSION = 42;
    public static final int locaReq = 43;
    TextView txtLocationCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        txtLocationCenter=(TextView)findViewById(R.id.txt_map_location);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        googleMap.animateCamera(cameraUpdate);
        Log.wtf("loca tag", ""+location.getLatitude()+ " "+location.getLongitude() +" with accuracy "+location.getAccuracy());
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            boolean firstIdle = true;
            @Override
            public void onCameraIdle() {
                clat = googleMap.getCameraPosition().target.latitude;
                clng = googleMap.getCameraPosition().target.longitude;
                getAddress(clat, clng);
                if (firstIdle) {
                    firstIdle = false;
                    return;
                }
                Log.wtf("camera idle","");
            }
        });
    }




    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    void getAddress(final double lat, final double lng) {
        new AsyncTask<Void, Void, String>() {
            //   boolean isValidAdd;

            @Override
            protected String doInBackground(Void... params) {
//                Location loca = new Location("");//provider name is unecessary
//                loca.setLatitude(lat);//your coords of course
//                loca.setLongitude(lng);
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (addresses == null || addresses.size() == 0)
                        return "";

                    Log.wtf("address is %s", addresses.get(0).toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address = "";
                if (address!=null) {
                    try{
                    for (int i = 0; i <= addresses.get(0).getMaxAddressLineIndex(); i++)
                        address += addresses.get(0).getAddressLine(i) + " ";
                }catch (NullPointerException e){}
                }

                    return address;

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //Set the location to top of the textview
                txtLocationCenter.setText(s);
            }
        }.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        initLocReq();
        checkLocaOn();

        googleMap.setPadding(0, 200, 0, 200);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        checkConnected();


      /*  googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                Log.wtf("Camera move started","Started...");
            }
        });

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                Log.wtf("Camera move","Moving....");
            }
        });

        googleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
            @Override
            public void onCameraMoveCanceled() {
                Log.wtf("Camera move cancel","MOving Canceled..!");
            }
        });*/

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.wtf("Camera idle","Camera Idle");
            }
        });


    }
    void initLocReq() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public void checkLocaOn() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {

                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        checkConnected();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            status.startResolutionForResult(
                                    MapActivity.this,
                                    locaReq);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                }

            }
        });



        /*}*/
    }


    void checkConnected() {

       // final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (!mGoogleApiClient.isConnected()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, MapActivity.this);

            }
        }.execute();
    }
}
