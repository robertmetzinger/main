package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class profileActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener{

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Profile", "onCreate: " );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);





        ButterKnife.bind(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnGetAdresse)
    void gps() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mGoogleApiClient.connect();
        String[] LOCATION_PERMS = {
                Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(LOCATION_PERMS, 1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }


        TextView breite = (TextView) findViewById(R.id.textBreite);
        TextView laenge = (TextView) findViewById(R.id.textLaenge);



        if (!breite.getText().toString().startsWith("B")){
            double breitengrad = Double.parseDouble(breite.getText().toString());
            double laengengrad = Double.parseDouble(laenge.getText().toString());

            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + breitengrad + " Lng: "
                            + laengengrad, Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + laengengrad;


        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(breitengrad, laengengrad, 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            TextView t = (TextView) findViewById(R.id.City);

            t.setText(cityName);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            TextView breite = (TextView) findViewById(R.id.textBreite);
            TextView laenge = (TextView) findViewById(R.id.textLaenge);
            breite.setText(String.valueOf(mLastLocation.getLatitude()));
            laenge.setText(String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
