package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Display.OnMapsFragmentLoadingListener, OnMapReadyCallback {

    private TextView mTextMessage;
    private MainActivity that = this;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @SuppressLint("ResourceType")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // Create new fragment and transaction
            Fragment blankFragment = new BlankFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_display:

                    Display displayFragment = new Display();
                    displayFragment.getMapAsync(that);
                    transaction.replace(R.id.fragment2, displayFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    mTextMessage.setText(R.string.title_display);
                    return true;

                case R.id.navigation_tutorien:

                    transaction.replace(R.id.fragment2, blankFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    mTextMessage.setText(R.string.title_tutorien);
                    return true;

                case R.id.navigation_create:

                    transaction.replace(R.id.fragment2, blankFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    mTextMessage.setText(R.string.title_create);
                    return true;

                case R.id.navigation_notifications:

                    transaction.replace(R.id.fragment2, blankFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    mTextMessage.setText(R.string.title_notifications);
                    return true;

                case R.id.navigation_profile:

                    Log.e("nix", "onNavigationItemSelected: ");
                    Fragment profileFragment = new profileFragment();
                    transaction.replace(R.id.fragment2, profileFragment);
                    transaction.addToBackStack("Profil");
                    transaction.commit();
                    mTextMessage.setText(" ");
                    return true;
            }
            return false;
        }

    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mTextMessage = findViewById(R.id.Home);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setEnabled(false);
        navigation.setFocusable(false);
        navigation.setFocusableInTouchMode(false);
        navigation.setClickable(false);
        navigation.setContextClickable(false);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        LoginFragment loginFragment = new LoginFragment();
        transaction.replace(R.id.fragment2, loginFragment);
        transaction.addToBackStack("Login");
        transaction.commit();

        // Nötig da durch einen Fehler, vermutlich durch Focus einer EditText, autmoatisch sich die Tastatur beim Start öffnete.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }



    @Override
    public void onMapsFragmentLoaded() {


    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
