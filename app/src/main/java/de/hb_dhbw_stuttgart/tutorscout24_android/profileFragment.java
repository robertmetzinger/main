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

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.Serializable;
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
@TargetApi(23)
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


    double gpsLaengengrad;
    double gpsBreitengrad;

    GoogleApiClient googleApiClient;
    private OnFragmentInteractionListener fragmentListener;

    public profileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Parameter 1.
     * @param mail Parameter 2.
     * @return A new instance of fragment profileFragment.
     */
    public static profileFragment newInstance(String name, String mail) {
        //TODO load userProfile

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {

            firstname = savedInstanceState.getSerializable("firstname");
            lastname = getArguments().getString(USER_LASTNAME);
            mail = getArguments().getString(USER_MAIL);
            alter = getArguments().getString(USER_ALTER);
            adresse = getArguments().getString(USER_ADRESSE);

            EditText firstNameEditText = getView().findViewById(R.id.txtFirstName);
            EditText lastNameEditText = getView().findViewById(R.id.textLastName);
            EditText alterEditText = getView().findViewById(R.id.txtAlter);
            EditText addresseEditText = getView().findViewById(R.id.txtAdress);
            EditText mailEditText = getView().findViewById(R.id.txtMail);

            firstNameEditText.setText(firstname.toString());
            lastNameEditText.setText(lastname);
            alterEditText.setText(alter);
            mailEditText.setText(mail);
            addresseEditText.setText(adresse);
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
        super.onAttach(context);


        /*try {
            mListener = (OnArticleSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }*/
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnGetAdresse)
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

        TextView t = getView().findViewById(R.id.City);
        t.setText(cityName);
    }

    @OnClick(R.id.btnSpeichern)
    public void saveUser() {

        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/create";

        StringRequest strRequest = new StringRequest(Request.Method.POST, usercreateURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                EditText firstName = getView().findViewById(R.id.txtFirstName);
                EditText lastName = getView().findViewById(R.id.textLastName);
                EditText alter = getView().findViewById(R.id.txtAlter);
                EditText addresse = getView().findViewById(R.id.txtAdress);
                EditText mail = getView().findViewById(R.id.txtMail);


                Map<String, String> params = new HashMap<>();
                params.put("userName", "android1234567");
                params.put("password", "androidTest12");
                params.put("firstName", firstName.getText().toString());
                params.put("lastName", lastName.getText().toString());
                params.put("age", alter.getText().toString());
                params.put("gender", "male");
                params.put("emaila", addresse.getText().toString());
                params.put("note", "keine Notiz");
                params.put("placeOfResidence", mail.getText().toString());
                params.put("maxGraduation", "kein Abschluss");

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(strRequest);
    }

    public void getUserInfo() {

        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/userInfo";

        StringRequest strRequest = new StringRequest(Request.Method.POST, usercreateURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("UserToFind", "android1234");
                params.put("userName", "androidTest");
                params.put("password", "androidTest");

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(strRequest);
    }

    @OnClick(R.id.btnHttpTest)
    public void htttpRequestTest() {
        Log.e("test", "htttpRequestTest: ");
        final TextView mTxtDisplay;

        mTxtDisplay = (TextView) getView().findViewById(R.id.userInfo);
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/info";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {


                    @Override
                    public void onResponse(JSONObject response) {
                        mTxtDisplay.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTxtDisplay.setText("Response: " + error.toString());

                    }
                });

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }

}
