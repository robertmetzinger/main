package de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.provider.BaseColumns;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;

/**
 * Created by Robert on 06.12.2017.
 */


//Dialog zum Anpassen der Sucheinstellungen im Feed
@RequiresApi(api = Build.VERSION_CODES.M)
public class SearchDialogFragment extends DialogFragment {

    private LayoutInflater inflater;
    private DisplayFragment parent;
    private Context context;
    private Activity activity;
    private AlertDialog.Builder builder;
    private int mode = 0;
    private String subjectContains;
    private String location;
    private String rangeKm;
    private double gpsBreitengrad;
    private double gpsLaengengrad;
    private SegmentedButtonGroup segmentedButtonGroup;
    private EditText subjectFilterTxt;
    private SearchView feedSearchView;
    private Spinner rangeSpinner;
    private SimpleCursorAdapter suggestionsAdapter;
    Geocoder geocoder;
    FusedLocationProviderClient locationProviderClient;
    String[] columns = new String[]{"adress", BaseColumns._ID};

    public SearchDialogFragment() {
        super();
    }

    //Übergeben einiger benötigter Parameter aus dem DisplayFragment
    public void setParams(LayoutInflater inflater, DisplayFragment parent, Context context, Activity activity, Geocoder geocoder) {
        this.inflater = inflater;
        this.parent = parent;
        this.context = context;
        this.activity = activity;
        this.geocoder = geocoder;
    }

    //öffnet den Dialog
    public void openDialog() {
        builder.show();
    }

    //erzeugt den Dialog
    public void createDialog() {
        //Erzeugen der View für den Dialog
        View dialogView = inflater.inflate(R.layout.search_dialog_layout, null);
        builder = new AlertDialog.Builder(context);
        ButterKnife.bind(this, dialogView);
        setUpSearchDialog(dialogView);
        builder.setView(dialogView);

        //Beim Klicken des Abbrechen-Buttons wird der Dialog geschlossen
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Beim Klicken des Übernehmen-Buttons werden die Nutzereingaben im Dialog an das DisplayFragment übergeben und mit diesen Daten ein Backend-Request gesendet
        builder.setPositiveButton("Übernehmen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                subjectContains = subjectFilterTxt.getText().toString().trim();
                LatLng latLng = getLatLngFromSearchField();
                location = feedSearchView.getQuery().toString();
                gpsBreitengrad = latLng.latitude;
                gpsLaengengrad = latLng.longitude;
                rangeKm = rangeSpinner.getSelectedItem().toString();
                dialog.dismiss();
                parent.setSubjectContains(subjectContains);
                parent.setGpsBreitengrad(gpsBreitengrad);
                parent.setGpsLaengengrad(gpsLaengengrad);
                parent.setRangeKm(Integer.parseInt(rangeKm));
                if (mode == 0) parent.getTutoringOffersFromBackend();
                else if (mode == 1) parent.getTutoringRequestsFromBackend();
            }
        });

        //Auswählen des Suchmodus (Student oder Tutor) durch Klicken des entsprechenden Segmented Buttons
        segmentedButtonGroup.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
                mode = position;
            }
        });
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    //Initialisieren des Dialogs
    public void setUpSearchDialog(View dialogView) {

        //UI Elemente holen
        segmentedButtonGroup = dialogView.findViewById(R.id.buttonGroupCreate);
        subjectFilterTxt = dialogView.findViewById(R.id.subjectFilterTxt);
        feedSearchView = dialogView.findViewById(R.id.feed_search_view);
        rangeSpinner = dialogView.findViewById(R.id.range_spinner);

        //UI Elemente auf Anfangswerte setzen (wenn der Dialog schon mal zuvor geöffnet wurde, werden die Daten vom letzten Mal jetzt wieder angezeigt)
        addItemsToSpinner();
        segmentedButtonGroup.setPosition(mode);
        if (location != null) feedSearchView.setQuery(location, false);
        if (rangeKm != null) rangeSpinner.setPrompt(rangeKm);

        //Adapter für Suchvorschläge
        final int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        suggestionsAdapter = new SimpleCursorAdapter(activity,
                android.R.layout.simple_list_item_1,
                null,
                columns,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        feedSearchView.setSuggestionsAdapter(suggestionsAdapter);

        //Suchfeld anpassen
        feedSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Anzeigen von Suchvorschlägen, wenn der Nutzer etwas eingibt
                getSearchSuggestions(newText);
                return true;
            }
        });
        feedSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                //wenn der Nutzer einen Suchvorschlag auswählt, wird dieser in das Eingabefeld geschrieben
                Cursor cursor = suggestionsAdapter.getCursor();
                cursor.moveToPosition(position);
                feedSearchView.setQuery(cursor.getString(0), false);
                cursor.moveToFirst();
                return false;
            }
        });
    }

    //Beim Klicken des MyLocation-Buttons wird der aktuelle Standort in das Eingabefeld geschrieben
    @OnClick(R.id.btnMyLocation)
    public void setMyLocationToSearchField() {
        getMyLocation();
        setCity();
    }

    //Dem Spinner (Dropdown List) werden die vordefinierten Auswahlmöglichkeiten für die Entfernung, in der gesucht werden soll, hinzugefügt
    public void addItemsToSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.range_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rangeSpinner.setAdapter(adapter);
    }

    //Methode zum Ermitteln des aktuellen Standortes des Nutzers (nur möglich, wenn Zugriff auf den Standort erlaubt ist)
    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }
        locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                gpsBreitengrad = location.getLatitude();
                gpsLaengengrad = location.getLongitude();
                Toast.makeText(context, "Dein aktueller Standort wird jetzt verwendet", Toast.LENGTH_SHORT).show();
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
                feedSearchView.setQuery(adressText.toString(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Bei einer Eingabe des Benutzers in das Suchfeld werden mittels Geocoder Suchvorschläge angezeigt
    public void getSearchSuggestions(String query) {

        if (!query.equals(null) && !query.trim().equals("")) {

            List<Address> addresses;

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

    //ermittelt Längen- und Breitengrad aus der Eingabe des Nutzers per Geocoder
    public LatLng getLatLngFromSearchField() {
        String searchLocation = feedSearchView.getQuery().toString();
        List<Address> addresses;
        try {
            int counter = 0;
            do {
                addresses = geocoder.getFromLocationName(searchLocation, 1);
            } while (addresses.size() == 0 && counter < 10);
            Address address = addresses.get(0);
            double latitudeForFeedSearch = address.getLatitude();
            double longitudeForFeedSearch = address.getLongitude();
            return new LatLng(latitudeForFeedSearch, longitudeForFeedSearch);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
