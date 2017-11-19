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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private MainActivity that = this;
    private MapFragment mapFragment;
    FragmentTransaction transaction;

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

                    DisplayFragment displayFragment = new DisplayFragment();
                    ChangeFragment(displayFragment, "Display");
                    return true;

                case R.id.navigation_tutorien:

                    ChangeFragment(blankFragment, "Blank");
                    return true;

                case R.id.navigation_create:

                    ChangeFragment(blankFragment, "Blank");
                    return true;

                case R.id.navigation_notifications:

                    ChangeFragment(blankFragment, "Blank");
                    return true;

                case R.id.navigation_profile:

                    Log.e("nix", "onNavigationItemSelected: ");
                    Fragment profileFragment = new profileFragment();
                    ChangeFragment(profileFragment, "Profil");

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

        //DisableNavigation();
        BottomNavigationView nav = findViewById(R.id.navigation);
        nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        LoginFragment loginFragment = new LoginFragment();
        ChangeFragment(loginFragment, "Login");

        // Nötig da durch einen Fehler, vermutlich durch Focus einer EditText, autmoatisch sich die Tastatur beim Start öffnete.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void ChangeFragment(Fragment fragment, String name) {

        transaction = getFragmentManager().beginTransaction();
        transaction.replace(((ViewGroup) findViewById(R.id.contentFragment).getParent()).getId(), fragment);

        // Alternativ (neues Fragment wird nur drüber gesetzt (anderes evtl Fehlerhaft)
        // transaction.replace(R.id.contentFragment, fragment);

        transaction.addToBackStack(name);
        transaction.commit();
    }


    public void DisableNavigation() {
        BottomNavigationView nav = findViewById(R.id.navigation);
        nav.setEnabled(false);

        nav.getMenu().getItem(0).setEnabled(false);
        nav.getMenu().getItem(1).setEnabled(false);
        nav.getMenu().getItem(2).setEnabled(false);
        nav.getMenu().getItem(3).setEnabled(false);
        nav.getMenu().getItem(4).setEnabled(false);

        nav.setVisibility(View.GONE);
    }

    public void EnableNavigation() {
        BottomNavigationView nav = findViewById(R.id.navigation);
        nav.setEnabled(true);

        nav.getMenu().getItem(0).setEnabled(true);
        nav.getMenu().getItem(1).setEnabled(true);
        nav.getMenu().getItem(2).setEnabled(true);
        nav.getMenu().getItem(3).setEnabled(true);
        nav.getMenu().getItem(4).setEnabled(true);

        nav.setVisibility(View.VISIBLE);
    }
}
