package de.hb_dhbw_stuttgart.tutorscout24_android.Logic;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.Communication.ContactFragment;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring.CreateTutoringFragment;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring.DisplayFragment;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.LoginFragment;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring.MyTutoringsFragment;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.ProfileFragment;

/*
  Created by Patrick Woehnl on 26.10.2017.
 */

/**
 * Der Haupteinsitiegspunkt des Programms.
 * <p>
 * Diese Klasse dient zum Managment der Übergreifenden Funktionen und zum wechseln der Fragments.
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    FragmentTransaction transaction;
    TextView titleView;

    ContactFragment kontateFragment;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SAVE = 1;
    private static final int RC_HINT = 2;
    private static final int RC_READ = 3;

    LoginFragment loginFragment;
    Utils utils;

    private GoogleApiClient mCredentialsApiClient;

    private boolean mIsResolving = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("ResourceType")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_display:

                    titleView.setText(R.string.NameDisplayFragment);

                    DisplayFragment displayFragment = new DisplayFragment();
                    changeFragment(displayFragment, String.valueOf(R.string.NameDisplayFragment));
                    return true;

                case R.id.navigation_tutorien:
                    titleView.setText(R.string.NameMyTutoringFragment);

                    MyTutoringsFragment myTutoringsFragment = new MyTutoringsFragment();
                    changeFragment(myTutoringsFragment, String.valueOf(R.string.NameMyTutoringFragment));
                    return true;

                case R.id.navigation_create:
                    titleView.setText(R.string.NameCreateTutoringFragment);

                    CreateTutoringFragment CreateTutoringFragment = new CreateTutoringFragment();
                    changeFragment(CreateTutoringFragment, String.valueOf(R.string.NameCreateTutoringFragment));
                    return true;

                case R.id.navigation_notifications:
                    titleView.setText(R.string.NameContactFragment);

                    kontateFragment = new ContactFragment();

                    utils.setKontateFragment(kontateFragment);

                    changeFragment(kontateFragment, String.valueOf(R.string.NameContactFragment));
                    return true;

                case R.id.navigation_profile:

                    titleView.setText(R.string.NameProfileFragment);

                    Fragment profileFragment = new ProfileFragment();
                    changeFragment(profileFragment, String.valueOf(R.string.NameProfileFragment));

                    return true;
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utils = new Utils(this, kontateFragment);

        // lade Kontaktliste
        SharedPreferences settings = getSharedPreferences("KontaktListe" + utils.getUserName(), 0);
        String[] defaultString = {"Keine Kontakte gefunden"};
        Set<String> defaultSet = new HashSet<>(Arrays.asList(defaultString));


        utils.kontakte.addAll(settings.getStringSet("Kontakte", defaultSet));

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mCredentialsApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();


        mCredentialsApiClient.connect();

        disableNavigation();


        BottomNavigationView nav = findViewById(R.id.navigation);
        nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        titleView = findViewById(R.id.toolbar_title);

        loginFragment = new LoginFragment();
        changeFragment(loginFragment, "Login");

        getWindow().setBackgroundDrawableResource(R.drawable.background_screen_small);

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                utils.loadRecievedMessages(getApplicationContext());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Attempt auto-sign in.
        if (!mIsResolving) {
            requestCredentials();
        }
    }

    public void changeFragment(Fragment fragment, String name) {

        transaction = getFragmentManager().beginTransaction();
        transaction.replace((findViewById(R.id.contentFragment)).getId(), fragment);
        transaction.addToBackStack(name);
        transaction.commit();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        //hideProgress();

        switch (requestCode) {
            case RC_HINT:
                // Drop into handling for RC_READ
                showToast("Diese funktion wird leider nicht unterstützt.");
            case RC_READ:
                if (resultCode == RESULT_OK) {
                    boolean isHint = (requestCode == RC_HINT);
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    loginFragment.processRetrievedCredential(credential, isHint);
                } else {
                    Log.e(TAG, "Credential Read: NOT OK");
                    showToast("Credential Read Failed");
                }

                mIsResolving = false;
                break;
            case RC_SAVE:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Credential Save: OK");
                    showToast("Credential Save Success");
                } else {
                    Log.e(TAG, "Credential Save: NOT OK");
                    showToast("Credential Save Failed");
                }

                mIsResolving = false;
                break;
        }
    }

    /**
     * Called when the save button is clicked.  Reads the entries in the email and password
     * fields and attempts to save a new Credential to the Credentials API.
     */
    public void saveCredentialClicked(String email, String password) {


        if (!mCredentialsApiClient.isConnected()) {
            mCredentialsApiClient.connect();

        }
        // Create a Credential with the user's email as the ID and storing the password.  We
        // could also add 'Name' and 'ProfilePictureURL' but that is outside the scope of this
        // minimal sample.
        Log.d(TAG, "Saving Credential:" + email + ":" + anonymizePassword(password));
        final Credential credential = new Credential.Builder(email)
                .setPassword(password)
                .build();

        // showProgress();

        // NOTE: this method unconditionally saves the Credential built, even if all the fields
        // are blank or it is invalid in some other way.  In a real application you should contact
        // your app's back end and determine that the credential is valid before saving it to the
        // Credentials backend.
        // showProgress();
        Auth.CredentialsApi.save(mCredentialsApiClient, credential).setResultCallback(
                new ResolvingResultCallbacks<Status>(this, RC_SAVE) {
                    @Override
                    public void onSuccess(@NonNull Status status) {
                        showToast("Credential Saved");
                        // hideProgress();
                    }

                    @Override
                    public void onUnresolvableFailure(@NonNull Status status) {
                        Log.d(TAG, "Save Failed:" + status);
                        showToast("Credential Save Failed");
                        // hideProgress();
                    }
                });
    }

    /**
     * Request Credentials from the Credentials API.
     */
    public void requestCredentials() {
        // Request all of the user's saved username/password credentials.  We are not using
        // setAccountTypes so we will not load any credentials from other Identity Providers.
        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        // showProgress();

        Auth.CredentialsApi.request(mCredentialsApiClient, request).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResult(@NonNull CredentialRequestResult credentialRequestResult) {
                        // hideProgress();
                        Status status = credentialRequestResult.getStatus();
                        if (status.isSuccess()) {
                            // Successfully read the credential without any user interaction, this
                            // means there was only a single credential and the user has auto
                            // changeFragment(new BlankFragment(), "Blank");
                            loginFragment.processRetrievedCredential(credentialRequestResult.getCredential(), false);
                            Log.d(TAG, "onResult: success");
                        } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                            // This is most likely the case where the user has multiple saved
                            // credentials and needs to pick one
                            resolveResult(status, RC_READ);
                        }
                    }
                });
    }


    /**
     * Attempt to resolve a non-successful Status from an asynchronous request.
     *
     * @param status      the Status to resolve.
     * @param requestCode the request code to use when starting an Activity for result,
     *                    this will be passed back to onActivityResult.
     */
    private void resolveResult(Status status, int requestCode) {
        // We don't want to fire multiple resolutions at once since that can result
        // in stacked dialogs after rotation or another similar event.
        if (mIsResolving) {
            Log.w(TAG, "resolveResult: already resolving.");
            return;
        }

        Log.d(TAG, "Resolving: " + status);
        if (status.hasResolution()) {
            Log.d(TAG, "STATUS: RESOLVING");
            try {
                status.startResolutionForResult(MainActivity.this, requestCode);
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "STATUS: Failed to send resolution.", e);
                //  hideProgress();
            }
        } else {
            Log.e(TAG, "STATUS: FAIL");
            showToast("Could Not Resolve Error");
            // hideProgress();
        }
    }

    public void deleteLoadedCredentialClicked(Credential mCurrentCredential) {
        if (mCurrentCredential == null) {
            showToast("Error: no credential to delete");
        }
        //showProgress();

        Auth.CredentialsApi.delete(mCredentialsApiClient, mCurrentCredential).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // hideProgress();
                        if (status.isSuccess()) {
                            // Credential delete succeeded, disable the delete button because we
                            // cannot delete the same credential twice. Clear text fields.
                            showToast("Credential Delete Success");
                        } else {
                            // Credential deletion either failed or was cancelled, this operation
                            // never gives a 'resolution' so we can display the failure message
                            // immediately.
                            Log.e(TAG, "Credential Delete: NOT OK");
                            showToast("Credential Delete Failed");
                        }
                    }
                });
    }

    /**
     * Make a password into asterisks of the right length, for logging.
     **/
    private String anonymizePassword(String password) {
        if (password == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }

    /**
     * Display a short Toast message
     **/
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public void setUser(String userName, String password) {
        utils.setUserName(userName);
        utils.setPassword(password);
    }

    public void changeTitle(String title) {
        if (titleView != null && title != null && !title.isEmpty()) {
            titleView.setText(title);
        }
    }
    public void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(500);
        }
    }


    public void notification() {
        // The id of the channel.
        String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_chat_bubble)
                        .setContentTitle("Tutorscout")
                        .setContentText("Neue Nachricht von: " + utils.getChatUser());
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

// mNotificationId is a unique integer your app uses to identify the
// notification. For example, to cancel the notification, you can pass its ID
// number to NotificationManager.cancel().
        if (null != mNotificationManager) {
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    public Utils getUtils(){
        return utils;
    }

    public void enableNavigation(){
        utils.EnableNavigation((BottomNavigationView) findViewById(R.id.navigation));
    }


    public void disableNavigation(){
        utils.DisableNavigation((BottomNavigationView) findViewById(R.id.navigation));
    }
}
