package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link profileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link profileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class profileFragment extends android.app.Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static String USER_FIRSTNAME = "USER_FIRSTNAME";
    private final static String USER_MAIL = "USER_MAIL";
    private final static String USER_ALTER = "USER_ALTER";
    private final static String USER_ADRESSE = "USER_ADRESSE";
    private final static String USER_LASTNAME = "USER_LASTNAME";


    private Serializable firstname;
    private String mail;
    private String alter;
    private String adresse;
    private String lastname;

    private User currentUser;

    double gpsLaengengrad;
    double gpsBreitengrad;

    GoogleApiClient googleApiClient;
    private OnFragmentInteractionListener fragmentListener;

    public profileFragment() {
        // Required empty public constructor
    }


    public static profileFragment newInstance(String name, String mail) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        args.putString(USER_FIRSTNAME, name);
        args.putString(USER_LASTNAME, mail);
        args.putString(USER_MAIL, mail);
        args.putString(USER_ADRESSE, mail);
        args.putString(USER_ALTER, mail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        Log.e("TAg", "onSaveInstanceState: ");
        outState.putSerializable("firstname", firstname);
        outState.putSerializable("lastname", lastname);
        outState.putSerializable("alter", alter);
        outState.putSerializable("adresse", adresse);
        super.onSaveInstanceState(outState);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {

            firstname = savedInstanceState.getSerializable("firstname");
            lastname = getArguments().getString(USER_LASTNAME);
            mail = getArguments().getString(USER_MAIL);
            alter = getArguments().getString(USER_ALTER);
            adresse = getArguments().getString(USER_ADRESSE);

            EditText firstName = getView().findViewById(R.id.txtProfileFirstName);
            EditText lastName = getView().findViewById(R.id.txtProfileLastName);
            EditText geschlecht = getView().findViewById(R.id.txtProfileLastGender);
            EditText txtalter = getView().findViewById(R.id.txtProfileAge);
            EditText wohnort = getView().findViewById(R.id.txtProfileAdress);
            EditText txtmail = getView().findViewById(R.id.txtProfileMail);
            EditText akademischGrad = getView().findViewById(R.id.txtProfileGraduation);
            EditText note = getView().findViewById(R.id.txtProfileNotiz);

            firstName.setText(firstname.toString());
            lastName.setText(lastname);
            txtalter.setText(alter);
            txtmail.setText(mail);
            wohnort.setText(adresse);
        }

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        Log.d("BIn", "onCreateView: ");




        return view;
    }

    @Override
    public void onAttach(Context context) {
        getUserInfo();




        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }

        Location myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (myLastLocation != null) {

            gpsBreitengrad = myLastLocation.getLatitude();
            gpsLaengengrad = myLastLocation.getLongitude();

            SetCity();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("Error", "onConnectionSuspended: Connection to Fragment suspendet");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Error", "onConnectionFailed: Connection to Fragment lost");
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


  //  @OnClick(R.id.btnGetAdresse)
    public void connectToGoogleGPSApi() {

        Log.e("Try connect", "gps: ");

        String[] LOCATION_PERMS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(LOCATION_PERMS, 1);
        googleApiClient.connect();
    }

    private void SetCity() {

        String cityName = null;
        Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(gpsBreitengrad, gpsLaengengrad, 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView t = getView().findViewById(R.id.txtWohnort);
        t.setText(cityName);
    }

    @OnClick(R.id.btnSpeichern)
    public void saveUser() {

        String updateUserURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/updateUser";

        EditText firstName = getView().findViewById(R.id.txtProfileFirstName);
        EditText lastName = getView().findViewById(R.id.txtProfileLastName);
        EditText geschlecht = getView().findViewById(R.id.txtProfileLastGender);
        EditText alter = getView().findViewById(R.id.txtProfileAge);
        EditText wohnort = getView().findViewById(R.id.txtProfileAdress);
        EditText mail = getView().findViewById(R.id.txtProfileMail);
        EditText akademischGrad = getView().findViewById(R.id.txtProfileGraduation);
        EditText note = getView().findViewById(R.id.txtProfileNotiz);

        JSONObject params = new JSONObject();
        try {
            params.put("password", MainActivity.getPassword());
            params.put("firstName", firstName.getText().toString());
            params.put("lastName", lastName.getText().toString());
            params.put("birthdate", alter.getText().toString());
            params.put("gender", geschlecht.getText().toString());
            if(currentUser.email.compareTo( mail.getText().toString()) != 0){
                params.put("email", mail.getText().toString());
            }
            params.put("note", note.getText().toString());
            params.put("placeOfResidence", wohnort.getText().toString());
            params.put("maxGraduation", akademischGrad.getText().toString());
            params.put("authentication", getAuthenticationJsonb());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MyJsonObjectRequest jsObjRequest = new MyJsonObjectRequest
                (Request.Method.PUT, updateUserURL, params,   new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Daten wurden gespeichert.", Toast.LENGTH_SHORT).show();
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String json = new String(error.networkResponse.data);
                                json = trimMessage(json, "message");
                                Log.e("", "onErrorResponse: " + json);
                            }
                        });

// Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsObjRequest);


        }

    public String trimMessage(String json, String key){
        String trimmedString = null;

        try{
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        } catch(JSONException e){
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }

    public JSONObject getAuthenticationJsonb() {
        JSONObject authentication = new JSONObject();
        try {
            authentication.put("userName", MainActivity.getUserName());
            authentication.put("password", MainActivity.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }

    public void getUserInfo() {


        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/myUserInfo";


        JSONObject requestBody = ((MainActivity)getActivity()).getUserInfoJsn();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, usercreateURL, requestBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        String a = response.toString();
                        try {
                            currentUser = new User();
                            currentUser.userName = response.getString("userid");
                            currentUser.firstName = response.getString("firstName");
                            currentUser.lastName = response.getString("lastName");
                            currentUser.age = Integer.parseInt(response.getString("dayOfBirth"));
                            currentUser.email = response.getString("email");
                            currentUser.maxGraduation = response.getString("maxGraduation");
                            currentUser.placeOfResidence = response.getString("placeOfResidence");
                            currentUser.note = response.getString("description");
                            currentUser.gender = response.getString("gender");

                            SetUserInfo();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                       // Toast.makeText(getContext(), "Fehler beim abrufen des Profils", Toast.LENGTH_SHORT).show();

                    }
                });

        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }

    private void SetUserInfo(){

        EditText firstName = getView().findViewById(R.id.txtProfileFirstName);
        EditText lastName = getView().findViewById(R.id.txtProfileLastName);
        EditText geschlecht = getView().findViewById(R.id.txtProfileLastGender);
        EditText alter = getView().findViewById(R.id.txtProfileAge);
        EditText wohnort = getView().findViewById(R.id.txtProfileAdress);
        EditText mail = getView().findViewById(R.id.txtProfileMail);
        EditText akademischGrad = getView().findViewById(R.id.txtProfileGraduation);
        EditText note = getView().findViewById(R.id.txtProfileNotiz);

        firstName.setText(currentUser.firstName);
        lastName.setText(currentUser.lastName);
        alter.setText("" + currentUser.age);
        wohnort.setText(currentUser.placeOfResidence);
        mail.setText(currentUser.email);
        geschlecht.setText(currentUser.gender);
        akademischGrad.setText(currentUser.maxGraduation);
        note.setText(currentUser.note);
    }

}
