package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateTutoringFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateTutoringFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class CreateTutoringFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    View rootView;
    GoogleApiClient googleApiClient;
    private int mode = 0;
    private double gpsBreitengrad;
    private double gpsLaengengrad;
    EditText subjectTxt;
    EditText infoTxt;
    private SearchView locationSearch;
    Spinner durationSpinner;
    private SimpleCursorAdapter suggestionsAdapter;
    String[] columns = new String[]{"adress", BaseColumns._ID};
    Geocoder geocoder;


    private OnFragmentInteractionListener mListener;

    public CreateTutoringFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateTutoringFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateTutoringFragment newInstance(String param1, String param2) {
        CreateTutoringFragment fragment = new CreateTutoringFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_create_tutoring, container, false);
        ButterKnife.bind(this, rootView);
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        addItemsToSpinner();
        setUpLocationTextField();
        subjectTxt = (EditText) rootView.findViewById(R.id.subjectTxt);
        infoTxt = (EditText) rootView.findViewById(R.id.infoTxt);
        durationSpinner = (Spinner) rootView.findViewById(R.id.spinnerDuration);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        return rootView;
    }

    public void setUpLocationTextField() {
        locationSearch = (SearchView) rootView.findViewById(R.id.locationSearch);
        final int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        suggestionsAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                columns,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        locationSearch.setSuggestionsAdapter(suggestionsAdapter);

        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getSearchSuggestions(newText);
                return true;
            }
        });
        locationSearch.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = suggestionsAdapter.getCursor();
                cursor.moveToPosition(position);
                locationSearch.setQuery(cursor.getString(0), false);
                cursor.moveToFirst();
                return false;
            }
        });
    }

    public void getSearchSuggestions(String query) {

        if (!query.equals(null) && !query.trim().equals("")) {

            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                int counter = 0;
                do {
                    addresses = geocoder.getFromLocationName(query, 3);
                    counter++;
                } while (addresses.size() == 0 && counter < 10);

                if (addresses.size() > 0) {
                    MatrixCursor matrixCursor = new MatrixCursor(columns);
                    for (int i = 0; i < addresses.size(); i++) {
                        Address address = addresses.get(i);
                        String adressText = "";
                        for (int line = 0; line <= address.getMaxAddressLineIndex(); line++) {
                            adressText += address.getAddressLine(line);
                            if (line != address.getMaxAddressLineIndex()) adressText += ", ";
                        }
                        matrixCursor.addRow(new Object[]{adressText, i});
                    }
                    suggestionsAdapter.swapCursor(matrixCursor);
                }

            } catch (Exception e) {
            }
        }
    }

    public void addItemsToSpinner() {
        Spinner spinner = rootView.findViewById(R.id.spinnerDuration);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.duration_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @OnClick(R.id.btnMyLocation)
    public void setMyLocationToTextField() {
        getMyLocation();
        setCity();
    }

    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }
        Location myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (myLastLocation != null) {

            gpsBreitengrad = myLastLocation.getLatitude();
            gpsLaengengrad = myLastLocation.getLongitude();
            Toast.makeText(getContext(), "Dein aktueller Standort wird jetzt verwendet", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCity() {
        Toast.makeText(getContext(), "Dein aktueller Standort wird jetzt verwendet", Toast.LENGTH_SHORT).show();
        List<Address> addresses;
        try {
            int counter = 0;
            do {
                addresses = geocoder.getFromLocation(48.4421, 8.68485, 1);
                counter++;
            } while (addresses.size() == 0 && counter < 10);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String adressText = "";
                for (int line = 0; line <= address.getMaxAddressLineIndex(); line++) {
                    adressText += address.getAddressLine(line);
                    if (line != address.getMaxAddressLineIndex()) adressText += ", ";
                }
                SearchView t = getView().findViewById(R.id.locationSearch);
                t.setQuery(adressText, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btnCreateOffer)
    public void showConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("")
                .setMessage("Willst du wirklich dieses Tutoring erstellen?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mode == 0) createRequest();
                        else if (mode == 1) createOffer();
                    }
                })
                .setNegativeButton("Nein", null).show();
    }

    public void createRequest() {
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/createRequest";

        String subject = subjectTxt.getText().toString();
        String text = infoTxt.getText().toString();
        int duration = Integer.parseInt(durationSpinner.getSelectedItem().toString());
        LatLng latLng = getLatLngFromSearchField();
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("subject", subject);
            requestBody.put("text", text);
            requestBody.put("duration", duration);
            requestBody.put("latitude", latitude);
            requestBody.put("longitude", longitude);
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Request Body", requestBody.toString());

        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Tutoring wurde erfolgreich erstellt", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //String json = new String(error.networkResponse.data);
                        //json = trimMessage(json, "message");
                        //Log.e("", "onErrorResponse: " + json);
                    }
                });
        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(request);
    }

    public void createOffer() {
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/createOffer";

        String subject = subjectTxt.getText().toString();
        String text = infoTxt.getText().toString();
        int duration = Integer.parseInt(durationSpinner.getSelectedItem().toString());
        LatLng latLng = getLatLngFromSearchField();
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("subject", subject);
            requestBody.put("text", text);
            requestBody.put("duration", duration);
            requestBody.put("latitude", latitude);
            requestBody.put("longitude", longitude);
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Request Body", requestBody.toString());

        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Angebot wurde erfolgreich erstellt", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //String json = new String(error.networkResponse.data);
                        //json = trimMessage(json, "message");
                        //Log.e("", "onErrorResponse: " + json);
                    }
                });
        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(request);
    }

    public LatLng getLatLngFromSearchField() {
        String location = locationSearch.getQuery().toString();
        List<Address> addresses;
        try {
            int counter = 0;
            do {
                addresses = geocoder.getFromLocationName(location, 1);
            } while (addresses.size() == 0 && counter < 10);
            Address address = addresses.get(0);
            double latitudeForOffer = address.getLatitude();
            double longitudeForOffer = address.getLongitude();
            return new LatLng(latitudeForOffer, longitudeForOffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getAuthenticationJson() {
        JSONObject authentication = new JSONObject();
        try {
            authentication.put("userName", MainActivity.getUserName());
            authentication.put("password", MainActivity.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }

    public String trimMessage(String json, String key) {
        String trimmedString = null;

        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
}
