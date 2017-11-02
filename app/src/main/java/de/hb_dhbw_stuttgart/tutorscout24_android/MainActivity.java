package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.annotation.SuppressLint;
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
            LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainContentLayout);
            LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

            switch (item.getItemId()) {
                case R.id.navigation_display:
                    mTextMessage.setText(R.string.title_display);
                    return true;
                case R.id.navigation_tutorien:
                    mTextMessage.setText(R.string.title_tutorien);
                    return true;
                case R.id.navigation_create:
                    mTextMessage.setText(R.string.title_create);
                    return true;
                case R.id.navigation_notifications:

                    mainLayout.removeAllViews();
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
                case R.id.navigation_profile:
                    Log.e("nix", "onNavigationItemSelected: ");

                    Intent profileIntent = new Intent(getApplicationContext(), profileActivity.class);
                    View layout = inflater.inflate(R.layout.activity_profile, null);
                    mainLayout.removeAllViews();
                    mainLayout.addView(layout);

                    startActivities(new Intent[]{profileIntent});
                    mTextMessage.setText(" ");

                    return true;
            }
                // dies ist der develop Branch
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.print("Nav");

        mTextMessage = (TextView) findViewById(R.id.Home);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
