package de.hb_dhbw_stuttgart.tutorscout24_android.View;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.credentials.Credential;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring.DisplayFragment;


/*
  Created by patrick.woehnl on 25.11.2017.
 */

/**
 * Das LoginFragment
 * <p>
 * Übernimmt das Login des Benutzers
 */
public class LoginFragment extends android.app.Fragment {


    private Credential mCurrentCredential;
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isLockedIn;

    public LoginFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLockedIn = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Speichert das Passwort.
     */
    public void savePassword() {

        if (getView() == null) {
            return;
        }
        CheckBox saveCred = getView().findViewById(R.id.checkRememberMe);
        if (saveCred.isChecked()) {
            EditText userName = getView().findViewById(R.id.txtLoginUserName);
            EditText passwort = getView().findViewById(R.id.txtLoginPasswort);

            // Aufrufe von saveCredentials in Main Aktivity (hier beindet sich die Credentials Api)
            ((MainActivity) getActivity()).saveCredentialClicked(userName.getText().toString(), passwort.getText().toString());

        } else {
            if (mCurrentCredential != null) {
                ((MainActivity) getActivity()).deleteLoadedCredentialClicked(mCurrentCredential);
            }
        }
    }


    /**
     * Überprüft das Passwort und den UserName mit dem Backend.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnLogin)
    public void checkAuthentification() {

        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/checkAuthentication";


        if (getAutentificationJSON() == null) {
            return;
        }
        final String requestBody = getAutentificationJSON().toString();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, usercreateURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                // Überprüfung erfolgreich
                Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onResponse: " + response);
                if (response.contains("200")) {
                    ((MainActivity) getActivity()).enableNavigation();
                    ((MainActivity) getActivity()).changeFragment(new DisplayFragment(), String.valueOf(R.string.NameDisplayFragment));

                    savePassword();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                if (response != null) {
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
                return null;
            }
        };

        HttpRequestManager.getInstance(getContext()).addToRequestQueue(stringRequest);
    }


    private JSONObject getAutentificationJSON() {
        JSONObject jsonObject = new JSONObject();
        JSONObject userJson = new JSONObject();

        if (getView() == null) {
            return null;
        }
        EditText benutzerName = getView().findViewById(R.id.txtLoginUserName);
        EditText passwort = getView().findViewById(R.id.txtLoginPasswort);

        ((MainActivity) getActivity()).setUser(benutzerName.getText().toString(), passwort.getText().toString());
        try {
            userJson.put("userName", benutzerName.getText().toString());
            userJson.put("password", passwort.getText().toString());
            jsonObject.put("authentication", userJson);

        } catch (Exception e) {
            Log.e(TAG, "getAutentificationJSON: ", e);
        }

        return jsonObject;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnAdmin)
    public void accessKeyStore() {
        isLockedIn = true;
        ((MainActivity) getActivity()).requestCredentials();
        ((MainActivity) getActivity()).enableNavigation();
        ((MainActivity) getActivity()).changeFragment(new BlankFragment(), "Blank");
    }

    /**
     * Öffnet das Registrieren Fragment.
     */
    @OnClick(R.id.btnRegistrien)
    public void btnRegistrierenClicked() {
        RegisterFragment registrierenFragment = new RegisterFragment();
        ((MainActivity) getActivity()).changeFragment(registrierenFragment, "Registrieren");
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.txtPasswortVergessen)
    public void passwortVergessen() {

        Toast.makeText(getContext(), "Diese funktion wird leider noch nicht unterstützt.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Process a Credential object retrieved from a successful request.
     *
     * @param credential the Credential to process.
     * @param isHint     true if the Credential is hint-only, false otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void processRetrievedCredential(Credential credential, boolean isHint) {
        Log.d(TAG, "Credential Retrieved: " + credential.getId() + ":" +
                anonymizePassword(credential.getPassword()));


        if (isLockedIn) {
            return;
        }
        // If the Credential is not a hint, we should store it an enable the delete button.
        // If it is a hint, skip this because a hint cannot be deleted.
        if (!isHint) {
            showToast("Credential Retrieved");
            mCurrentCredential = credential;
            //findViewById(R.id.button_delete_loaded_credential).setEnabled(true);
        } else {
            showToast("Credential Hint Retrieved");
        }

        isLockedIn = true;
        if (getView() == null) {
            return;
        }
        EditText userName = getView().findViewById(R.id.txtLoginUserName);
        EditText passwort = getView().findViewById(R.id.txtLoginPasswort);
        userName.setText(credential.getId());
        passwort.setText(credential.getPassword());

        ((MainActivity) getActivity()).setUser(credential.getId(), credential.getPassword());
        CheckBox checkBox = getView().findViewById(R.id.checkRememberMe);

        checkBox.setChecked(true);
    }

    /**
     * Anonymisiert das Passwort.
     *
     * @param password Dass password.
     * @return Das Passwort als Sternchen.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
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
     * Zeigt einen Toast.
     *
     * @param msg Die msg.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}