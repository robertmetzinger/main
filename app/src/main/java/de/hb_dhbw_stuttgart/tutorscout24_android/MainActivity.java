package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdToken;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private MainActivity that = this;
    private MapFragment mapFragment;
    FragmentTransaction transaction;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final int RC_SAVE = 1;
    private static final int RC_HINT = 2;
    private static final int RC_READ = 3;

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    private static String userName;
    private static String password;

    LoginFragment loginFragment;

    private GoogleApiClient mCredentialsApiClient;

    private boolean mIsResolving = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.M)
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

                    ChatFragment chatFragment = new ChatFragment();
                    ChangeFragment(chatFragment, "Chat");
                    return true;

                case R.id.navigation_profile:

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
        mCredentialsApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();


        mCredentialsApiClient.connect();

        DisableNavigation();
        BottomNavigationView nav = findViewById(R.id.navigation);
        nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        loginFragment = new LoginFragment();
        ChangeFragment(loginFragment, "Login");

        // Nötig da durch einen Fehler, vermutlich durch Focus einer EditText, autmoatisch sich die Tastatur beim Start öffnete.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getWindow().setBackgroundDrawableResource(R.drawable.background_screen_small);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Attempt auto-sign in.
        if (!mIsResolving) {
            requestCredentials();
        }
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


        if(!mCredentialsApiClient.isConnected()){
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
     * Called when the Load Credentials button is clicked. Attempts to read the user's saved
     * Credentials from the Credentials API.  This may show UX, such as a credential picker
     * or an account picker.
     *
     * <b>Note:</b> in a normal application loading credentials should happen without explicit user
     * action, this is only connected to a 'Load Credentials' button for easier demonstration
     * in this sample.  Make sure not to load credentials automatically if the user has clicked
     * a "sign out" button in your application in order to avoid a sign-in loop. You can do this
     * with the function <code>Auth.CredentialsApi.disableAuthSignIn(...)</code>.
     */
    public void loadCredentialsClicked() {
         requestCredentials();
    }

    /**
     * Called when the Load Hints button is clicked. Requests a Credential "hint" which will
     * be the basic profile information and an ID token for an account on the device. This is useful
     * to auto-fill sign-up forms with an email address, picture, and name or to do password-free
     * authentication with a server by providing an ID Token.
     */
    private void loadHintClicked() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setHintPickerConfig(new CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .build())
                .setEmailAddressIdentifierSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build();

        PendingIntent intent =
                Auth.CredentialsApi.getHintPickerIntent(mCredentialsApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), RC_HINT, null, 0, 0, 0);
            mIsResolving = true;
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Could not start hint picker Intent", e);
            mIsResolving = false;
        }
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
                           // ChangeFragment(new BlankFragment(), "Blank");
                            loginFragment.processRetrievedCredential(credentialRequestResult.getCredential(), false);
                            Log.d(TAG, "onResult: success");
                        } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                            // This is most likely the case where the user has multiple saved
                            // credentials and needs to pick one
                            resolveResult(status, RC_READ);
                        } else if (status.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                            // This means only a hint is available, but we are handling that
                            // elsewhere so no need to act here.
                        } else {
                            Log.w(TAG, "Unexpected status code: " + status.getStatusCode());
                        }
                    }
                });
    }

    /**
     * Called when the delete credentials button is clicked.  This deletes the last Credential
     * that was loaded using the load button.
     */


    /**
     * Attempt to resolve a non-successful Status from an asynchronous request.
     * @param status the Status to resolve.
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




    /** Make a password into asterisks of the right length, for logging. **/
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

    /** Display a short Toast message **/
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public JSONObject getUserInfoJsn() {
        JSONObject jsonObject = new JSONObject();
        JSONObject userJson = new JSONObject();



        try {
            userJson.put("userName", userName);
            userJson.put("password", password);
            jsonObject.put("userToFind", userName);
            jsonObject.put("authentication", userJson);

        } catch (Exception e) {
            Log.e("aut", "getAutentificationJSON: ", e);
        }

        return jsonObject;
    }

    public void setUser(String userName, String password){
        this.userName = userName;
        this.password = password;
    }
}
