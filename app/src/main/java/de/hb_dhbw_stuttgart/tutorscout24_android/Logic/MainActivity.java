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
import android.widget.TextView;
import android.widget.Toast;


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
 * Diese Klasse dient zum Managment des Logins und zum wechseln der Fragments.
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

    private GoogleApiClient credentialsApiClient;

    private boolean mIsResolving = false;

    /**
     * Wechselt die Fragments je nach Auswahlt.
     */
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

    /**
     * Instanziiert die MainActivity.
     *
     * @param savedInstanceState Der savedInstanceState.
     */
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

        // Instanziiert die Credentials Api
        credentialsApiClient
                = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();


        credentialsApiClient.connect();

        disableNavigation();


        BottomNavigationView nav = findViewById(R.id.navigation);
        nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Zu beginn wird das LoginFragment aufgerufen und gezeigt
        titleView = findViewById(R.id.toolbar_title);
        loginFragment = new LoginFragment();
        changeFragment(loginFragment, "Login");

        getWindow().setBackgroundDrawableResource(R.drawable.background_screen_small);

        // Scheduler zum Polling von neuen Nachrichten
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                utils.loadRecievedMessages(getApplicationContext());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Das onStatrt Event.
     * Fragt die Credentials an.
     */
    @Override
    public void onStart() {
        super.onStart();

        // Attempt auto-sign in.
        if (!mIsResolving) {
            requestCredentials();
        }
    }

    /**
     * Wechselt das aktuelle Fragment.
     *
     * @param fragment Das neue Fragment.
     * @param name     Der Name des Fragments.
     */
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


    /**
     * Das onActivityResult Event.
     *
     * @param requestCode Der resultCode.
     * @param resultCode  Der resultCode.
     * @param data Die data.
     */
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
     * Wird aufgerufen, sobald die Credentials gespeichert werden sollen.
     * Speicher den Nutzername und das Passwort.
     *
     * Code wurde entnommen aus:
     * https://developers.google.com/identity/protocols/application-default-credentials
     * Zudem finden sich hier weitere Informationen.
     */
    public void saveCredentialClicked(String email, String password) {


        if (!credentialsApiClient.isConnected()) {
            credentialsApiClient.connect();

        }

        // Erzeugt eine neue Credential.
        Log.d(TAG, "Saving Credential:" + email + ":" + anonymizePassword(password));
        final Credential credential = new Credential.Builder(email)
                .setPassword(password)
                .build();



       // Speichert die Credentials
        Auth.CredentialsApi.save(credentialsApiClient, credential).setResultCallback(
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
     * Request Credentials aus der Credentials API.
     */
    public void requestCredentials() {
        // Fragt nach allen Credentials die mit dem Benutzer gespeichert wurden.
        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        Auth.CredentialsApi.request(credentialsApiClient, request).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResult(@NonNull CredentialRequestResult credentialRequestResult) {
                        // hideProgress();
                        Status status = credentialRequestResult.getStatus();
                        if (status.isSuccess()) {
                            // Credentials erfolgreich gelesen.
                            loginFragment.processRetrievedCredential(credentialRequestResult.getCredential(), false);
                            Log.d(TAG, "onResult: success");
                        } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                            // Credentials nicht erfolgreich gelesen.
                            resolveResult(status, RC_READ);
                        }
                    }
                });
    }


    /**
     * Verarbeitung bei Fehlschlag des Lesens.
     *
     * @param status      Der status.
     * @param requestCode Der requestCode.
     */
    private void resolveResult(Status status, int requestCode) {

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
            }
        } else {
            Log.e(TAG, "STATUS: FAIL");
            showToast("Could Not Resolve Error");
        }
    }

    /**
     * Löschen der Credentials
     * @param credential Die credentials.
     */
    public void deleteLoadedCredentialClicked(Credential credential) {
        if (credential == null) {
            showToast("Error: no credential to delete");
        }

        Auth.CredentialsApi.delete(credentialsApiClient, credential).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            // Credentials erfolgreich gelöscht.
                            showToast("Credential Delete Success");
                        } else {
                            // Credentials löschen fehlgeschlagen
                            Log.e(TAG, "Credential Delete: NOT OK");
                            showToast("Credential Delete Failed");
                        }
                    }
                });
    }

    /**
     * Macht aus einem Passwort Sternchen der richtigen Länge für das Logging.
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
     * Zeigt einen Toas an
     **/
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * Setzt einen User in den Utils.
     * @param userName Der userName,
     * @param password Das password.
     */
    public void setUser(String userName, String password) {
        utils.setUserName(userName);
        utils.setPassword(password);
    }

    /*
    * Ändert den Titel (titleView).
     */
    public void changeTitle(String title) {
        if (titleView != null && title != null && !title.isEmpty()) {
            titleView.setText(title);
        }
    }

    /**
     * Vibriert für 500 millisekunden.
     */
    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(500);
        }
    }

    /**
     * Schickt eine Push notification.
     *
     * Code entnommen aus:
     * https://developer.android.com/guide/topics/ui/notifiers/notifications.html
     */
    public void notification() {
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

    /**
     * gibt die aktuelle Utils zurück.
     * @return Die Utils.
     */
    public Utils getUtils() {
        return utils;
    }

    /**
     * Weiterleitung an Utils, da die BottomNavigationView nicht in den Fragments vorhanden ist.
     */
    public void enableNavigation() {
        utils.EnableNavigation((BottomNavigationView) findViewById(R.id.navigation));
    }

    /**
     * Weiterleitung an Utils, da die BottomNavigationView nicht in den Fragments vorhanden ist.
     */
    public void disableNavigation() {
        utils.DisableNavigation((BottomNavigationView) findViewById(R.id.navigation));
    }
}
