package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

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

                    transaction.replace(R.id.fragment2, blankFragment);
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
                    transaction.addToBackStack(null);
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

        ButterKnife.bind(this);

        mTextMessage = findViewById(R.id.Home);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
