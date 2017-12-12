package de.hb_dhbw_stuttgart.tutorscout24_android;

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
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;

/**
 * Created by Robert on 06.12.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class SearchDialogFragment extends DialogFragment {

    private DisplayFragment parent;
    private LayoutInflater inflater;
    private Context context;
    private Activity activity;
    private AlertDialog.Builder builder;
    private int mode = 0;
    private String location;
    private String rangeKm;
    private double gpsBreitengrad;
    private double gpsLaengengrad;
    private SegmentedButtonGroup segmentedButtonGroup;
    private SearchView feedSearchView;
    private Spinner rangeSpinner;
    private SimpleCursorAdapter suggestionsAdapter;
    GoogleApiClient googleApiClient;
    Geocoder geocoder;
    String[] columns = new String[]{"adress", BaseColumns._ID};

    public SearchDialogFragment() {
        super();
    }

    public void setParams(DisplayFragment parent, LayoutInflater inflater, Context context, Activity activity, Geocoder geocoder) {
        this.parent = parent;
        this.inflater = inflater;
        this.context = context;
        this.activity = activity;
        this.geocoder = geocoder;
    }

    public void openDialog() {
        builder.show();
    }

    public void createDialog() {
        builder = new AlertDialog.Builder(context);
        View dialogView = inflater.inflate(R.layout.search_dialog_layout, null);
        ButterKnife.bind(this, dialogView);
        builder.setView(dialogView);
        setUpSearchDialog(dialogView);
        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Übernehmen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LatLng latLng = getLatLngFromSearchField();
                location = feedSearchView.getQuery().toString();
                gpsBreitengrad = latLng.latitude;
                gpsLaengengrad = latLng.longitude;
                rangeKm = rangeSpinner.getSelectedItem().toString();
                dialog.dismiss();
                parent.setGpsBreitengrad(gpsBreitengrad);
                parent.setGpsLaengengrad(gpsLaengengrad);
                parent.setRangeKm(Integer.parseInt(rangeKm));
                if (mode == 0) parent.getTutoringOffersFromBackend();
                else if (mode== 1) parent.getTutoringRequestsFromBackend();
            }
        });
        segmentedButtonGroup.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
                mode = position;
            }
        });
    }

    public void setUpSearchDialog(View dialogView) {
        segmentedButtonGroup = (SegmentedButtonGroup) dialogView.findViewById(R.id.buttonGroupCreate);
        feedSearchView = (SearchView) dialogView.findViewById(R.id.feed_search_view);
        rangeSpinner = (Spinner) dialogView.findViewById(R.id.range_spinner);
        addItemsToSpinner();
        segmentedButtonGroup.setPosition(mode);
        if (location != null) feedSearchView.setQuery(location, false);
        if (rangeKm != null) rangeSpinner.setPrompt(rangeKm);
        final int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        suggestionsAdapter = new SimpleCursorAdapter(activity,
                android.R.layout.simple_list_item_1,
                null,
                columns,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        feedSearchView.setSuggestionsAdapter(suggestionsAdapter);
        feedSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        feedSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = suggestionsAdapter.getCursor();
                cursor.moveToPosition(position);
                feedSearchView.setQuery(cursor.getString(0), false);
                cursor.moveToFirst();
                return false;
            }
        });
    }

     @OnClick(R.id.btnMyLocation)
    public void setMyLocationToSearchField() {
         getMyLocation();
         setCity();
    }

    public void addItemsToSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.range_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rangeSpinner.setAdapter(adapter);
    }

    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }
        try {

            Location myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            if (myLastLocation != null) {

                gpsBreitengrad = myLastLocation.getLatitude();
                gpsLaengengrad = myLastLocation.getLongitude();
                Toast.makeText(context, "Dein aktueller Standort wird jetzt verwendet", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Standort konnte nicht erfasst werden", Toast.LENGTH_SHORT).show();
        }
    }

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
                String adressText = "";
                for (int line = 0; line <= address.getMaxAddressLineIndex(); line++) {
                    adressText += address.getAddressLine(line);
                    if (line != address.getMaxAddressLineIndex()) adressText += ", ";
                }
                feedSearchView.setQuery(adressText, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
