package de.hb_dhbw_stuttgart.tutorscout24_android.View;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MyJsonObjectRequest;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;


/**
 * Created by Robert
 */

//Dieses Fragment dient zum Erstellen von Tutorings (Offer oder Request)
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
    FusedLocationProviderClient locationProviderClient;


    public CreateTutoringFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
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
        subjectTxt = rootView.findViewById(R.id.subjectTxt);
        infoTxt = rootView.findViewById(R.id.infoTxt);
        durationSpinner = rootView.findViewById(R.id.spinnerDuration);

        SegmentedButtonGroup group = rootView.findViewById(R.id.buttonGroupCreate);
        group.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
                mode = position;
            }
        });

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        return rootView;
    }

    //Initialisieren des Suchfeldes für den Standort, an dem das Tutoring erstellt werden soll
    public void setUpLocationTextField() {
        locationSearch = rootView.findViewById(R.id.locationSearch);

        //Adapter für Suchvorschläge
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

            //Suchvorchläge anzeigen, wenn der Nutzer etwas eingibt
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

            //wenn der Nutzer einen Suchvorschlag auswählt, wird dieser in das Suchfeld geschrieben
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

    //Bei einer Eingabe des Benutzers in das Suchfeld werden mittels Geocoder Suchvorschläge angezeigt
    public void getSearchSuggestions(String query) {

        if (!query.equals(null) && !query.trim().equals("")) {

            List<Address> addresses;

            try {
                //versucht maximal 10-mal per Geocoder eine passende Adresse zu dem Eingabestring zu ermitteln, da manchmal die Adresse nicht gleich gefunden wird. Maximal werden 3 Vorschläge angezeigt
                int counter = 0;
                do {
                    addresses = geocoder.getFromLocationName(query, 3);
                    counter++;
                } while (addresses.size() == 0 && counter < 10);

                if (addresses.size() > 0) {
                    MatrixCursor matrixCursor = new MatrixCursor(columns);
                    for (int i = 0; i < addresses.size(); i++) {
                        Address address = addresses.get(i);
                        StringBuilder adressText = new StringBuilder();
                        for (int line = 0; line <= address.getMaxAddressLineIndex(); line++) {
                            adressText.append(address.getAddressLine(line));
                            if (line != address.getMaxAddressLineIndex()) adressText.append(", ");
                        }
                        matrixCursor.addRow(new Object[]{adressText.toString(), i});
                    }
                    suggestionsAdapter.swapCursor(matrixCursor);
                }

            } catch (Exception ignored) {
            }
        }
    }

    //Dem Spinner (Dropdown List) werden die vordefinierten Auswahlmöglichkeiten für die Dauer des Tutorings hinzugefügt
    public void addItemsToSpinner() {
        Spinner spinner = rootView.findViewById(R.id.spinnerDuration);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.duration_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    //Beim Klicken des MyLocation-Buttons wird der Standort des Nutzers in das Eingabefeld geschrieben
    @OnClick(R.id.btnMyLocation)
    public void setMyLocationToTextField() {
        getMyLocation();
        setCity();
    }

    //Methode zum Ermitteln des aktuellen Standortes des Nutzers (nur möglich, wenn Zugriff auf den Standort erlaubt ist)
    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                gpsBreitengrad = location.getLatitude();
                gpsLaengengrad = location.getLongitude();
            }
        });
    }

    //der aus Längen- und Breitengrad ermittlelte Standort wird in das Eingabefeld eingetragen
    private void setCity() {
        List<Address> addresses;
        try {
            int counter = 0;
            do {
                addresses = geocoder.getFromLocation(gpsBreitengrad, gpsLaengengrad, 1);
                counter++;
            } while (addresses.size() == 0 && counter < 10);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder adressText = new StringBuilder();
                for (int line = 0; line <= address.getMaxAddressLineIndex(); line++) {
                    adressText.append(address.getAddressLine(line));
                    if (line != address.getMaxAddressLineIndex()) adressText.append(", ");
                }
                SearchView t = getView().findViewById(R.id.locationSearch);
                t.setQuery(adressText.toString(), false);
                Toast.makeText(getContext(), "Dein aktueller Standort wird jetzt verwendet", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //bei Klicken des Erstellen-Buttons wird ein Bestätigungsdialog geöffnet. Nach Akzeptieren wird die Anfrage zum Erstellen des Tutorings an das Backend gesendet
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

    //liest die Eingaben aus der View und sendet eine Anfrage zum Erstellen eines Requests an das Backend
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
                        Toast.makeText(getContext(), "Anfrage wurde erfolgreich erstellt", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        // Übergeben des Requests an den RequestManager
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(request);
    }

    //liest die Eingaben aus der View und sendet eine Anfrage zum Erstellen eines Offers an das Backend
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
                    }
                });
        // Übergeben des Requests an den RequestManager
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(request);
    }

    //ermittelt Längen- und Breitengrad aus der Eingabe des Nutzers per Geocoder
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

    //erzeugt ein JSONObject mit den Username und Passwort zur Authentifizierung im Backend
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
}
