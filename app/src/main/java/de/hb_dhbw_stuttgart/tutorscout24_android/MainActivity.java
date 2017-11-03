package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @SuppressLint("ResourceType")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // Create new fragment and transaction
            Fragment newFragment = new BlankFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_display:
                    transaction.replace(R.id.fragment2, newFragment);
                    transaction.addToBackStack(null);
                    mTextMessage.setText(R.string.title_display);
                    return true;
                case R.id.navigation_tutorien:
                  // Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
                    transaction.replace(R.id.fragment2, newFragment);
                    transaction.addToBackStack(null);

// Commit the transaction
                    transaction.commit();

                    mTextMessage.setText(R.string.title_tutorien);
                    return true;
                case R.id.navigation_create:

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
                    transaction.replace(R.id.fragment2, newFragment);
                    transaction.addToBackStack(null);

// Commit the transaction
                    transaction.commit();

                    mTextMessage.setText(R.string.title_create);
                    return true;
                case R.id.navigation_notifications:


// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
                    transaction.replace(R.id.fragment2, newFragment);
                    transaction.addToBackStack(null);

// Commit the transaction
                    transaction.commit();

                    mTextMessage.setText(R.string.title_notifications);
                    return true;
                case R.id.navigation_profile:
                    Log.e("nix", "onNavigationItemSelected: ");

                    Fragment profileFragment = new profileFragment();

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
                    transaction.replace(R.id.fragment2, profileFragment);
                    transaction.addToBackStack(null);

// Commit the transaction
                    transaction.commit();

                    mTextMessage.setText(" ");

                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTextMessage = (TextView) findViewById(R.id.Home);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
