package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends android.app.Fragment {


    private Credential mCurrentCredential;
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isLockedIn;

    public LoginFragment() {
        // Required empty public constructor
    }


    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
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


   public void savePassword() {

        CheckBox saveCred = getView().findViewById(R.id.checkRememberMe);
        if (saveCred.isChecked()) {
            EditText userName = getView().findViewById(R.id.txtLoginUserName);
            EditText passwort = getView().findViewById(R.id.txtLoginPasswort);

            ((MainActivity) getActivity()).saveCredentialClicked(userName.getText().toString(), passwort.getText().toString());

        } else {
            if (mCurrentCredential != null) {
                ((MainActivity) getActivity()).deleteLoadedCredentialClicked(mCurrentCredential);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnLogin)
    public void checKAuthentification() {

        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/checkAuthentication";


        final String requestBody = getAutentificationJSON().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, usercreateURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onResponse: " + response);
                if (response.contains("200")) {
                    ((MainActivity) getActivity()).EnableNavigation();
                    ((MainActivity) getActivity()).ChangeFragment(new BlankFragment(), "Blank");

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
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        HttpRequestManager.getInstance(getContext()).addToRequestQueue(stringRequest);
    }


    private JSONObject getAutentificationJSON() {
        JSONObject jsonObject = new JSONObject();
        JSONObject userJson = new JSONObject();

        EditText benutzerName = getView().findViewById(R.id.txtLoginUserName);
        EditText passwort = getView().findViewById(R.id.txtLoginPasswort);

        ((MainActivity)getActivity()).setUser(benutzerName.getText().toString(), passwort.getText().toString());
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
        ((MainActivity) getActivity()).EnableNavigation();
        ((MainActivity) getActivity()).ChangeFragment(new BlankFragment(), "Blank");
    }

    @OnClick(R.id.txtRegistrieren)
    public void OnRegistrierenLabelClick() {
        RegistrierenFragment registrierenFragment = new RegistrierenFragment();


        ((MainActivity) getActivity()).ChangeFragment(registrierenFragment, "Registrieren");
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


        if(isLockedIn){
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
        EditText userName = getView().findViewById(R.id.txtLoginUserName);
        EditText passwort = getView().findViewById(R.id.txtLoginPasswort);
        userName.setText(credential.getId());
        passwort.setText(credential.getPassword());

        ((MainActivity)getActivity()).setUser(credential.getId(), credential.getPassword());
        CheckBox checkBox = getView().findViewById(R.id.checkRememberMe);

        checkBox.setChecked(true);
    }

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
