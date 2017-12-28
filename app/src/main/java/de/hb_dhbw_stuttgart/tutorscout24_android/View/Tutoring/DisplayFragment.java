package de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring;

import android.app.Fragment;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.CustomJsonArrayRequest;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.Utils;
import de.hb_dhbw_stuttgart.tutorscout24_android.Model.Tutoring.FeedItem;
import de.hb_dhbw_stuttgart.tutorscout24_android.Model.Tutoring.FeedItemAdapter;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.FeedSorter;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;

/**
 * Created by Robert
 */

//Dieses Fragment enthält den Feed und die Map, in denen die Tutorings angezeigt werden
@RequiresApi(api = Build.VERSION_CODES.M)
public class DisplayFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    View rootView;
    MapView mMapView;
    private GoogleMap googleMap;
    private HashMap<Marker, String> markerTutoringIdHashMap = new HashMap<>();
    GoogleApiClient googleApiClient;
    private double gpsBreitengrad;
    private double gpsLaengengrad;
    private String subjectContains;
    private int rowOffset = 0;
    private int rangeKm = 50000;
    private int rowLimit = 100;
    private SwipeRefreshLayout swipeContainer;
    private SearchView searchView;
    private SimpleCursorAdapter suggestionsAdapterForMapSearch;
    String[] columns = new String[]{"adress", BaseColumns._ID};
    Geocoder geocoder;
    FusedLocationProviderClient locationProviderClient;
    private SearchDialogFragment searchDialogFragment;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_display, container, false);
        ButterKnife.bind(this, rootView);

        geocoder = new Geocoder(getContext(), Locale.getDefault());

        TabHost host = rootView.findViewById(R.id.tabHost);
        host.setup();
        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Feed");
        spec.setContent(R.id.tabFeed);
        spec.setIndicator("Feed");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Map");
        spec.setContent(R.id.tabMap);
        spec.setIndicator("Map");
        host.addTab(spec);

        swipeContainer = rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Beim Runterswipen werden die Tutorings neu aus dem Backend geladen
                getTutoringOffersFromBackend();
            }
        });

        //Der Suchdialog wird zu Anfang erstellt, damit er später angezeigt werden kann
        searchDialogFragment = new SearchDialogFragment();
        searchDialogFragment.setParams(inflater, this, getContext(), getActivity(), geocoder);
        setUpSearchView();

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // Prüfen, ob Standorterfassung erlaubt ist, zum Anzeigen des Standortes in der Map
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                UiSettings settings = googleMap.getUiSettings();
                settings.setMyLocationButtonEnabled(true);
                settings.setZoomControlsEnabled(true);
                settings.setMapToolbarEnabled(true);

                //Beim Klicken eines Markers auf der Map werden der entsprechende User und das Fach angezeigt, sowie eine Einblendung der Tutoring ID
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String thisTutoringId = markerTutoringIdHashMap.get(marker);
                        Toast.makeText(getContext(), "Tutoring ID = " + thisTutoringId, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                //Bei Gedrückthalten eines Markers auf der Map wird ein DetailTutoringFragment geöffnet (um das Gedrückthalten zu erkennen wird hierfür der DragListener des Markers verwendet)
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        FeedItem item = (FeedItem) marker.getTag();
                        DetailTutoringFragment detailTutoringFragment = new DetailTutoringFragment();
                        detailTutoringFragment.setParams(item.getUserName(), item.getTutoringId(), item.getSubject(), item.getText(), item.getDistanceKm(), item.getCreationDate(), item.getCreationDate() );
                        ((MainActivity) getActivity()).changeFragment(detailTutoringFragment, "DetailTutoring");
                    }
                    @Override
                    public void onMarkerDrag(Marker marker) {
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                    }
                });
            }
        });

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        //Initiales Anzeigen der Tutorings vom aktuellen Standort aus
        getMyLocation();
        getTutoringOffersFromBackend();

        return rootView;
    }

    //Beim Drücken des Sucheinstellungs-Buttons wird der Suchdialog geöffnet
    @OnClick(R.id.fab)
    public void showDialog() {
        searchDialogFragment.createDialog();
        searchDialogFragment.openDialog();
    }

    //Initialisieren des Suchfeldes in der Map
    public void setUpSearchView() {
        searchView = rootView.findViewById(R.id.searchView);

        //Adapter für Suchvorschläge
        final int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        suggestionsAdapterForMapSearch = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                columns,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        searchView.setSuggestionsAdapter(suggestionsAdapterForMapSearch);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //Suche starten wenn der Nutzer seine Eingabe bestätigt
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }
            //Suchvorchläge anzeigen, wenn der Nutzer etwas eingibt
            @Override
            public boolean onQueryTextChange(String newText) {
                getSearchSuggestions(suggestionsAdapterForMapSearch, newText);
                return true;
            }
        });
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            //wenn der Nutzer einen Suchvorschlag auswählt, wird dieser in das Suchfeld geschrieben
            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = suggestionsAdapterForMapSearch.getCursor();
                cursor.moveToPosition(position);
                searchView.setQuery(cursor.getString(0), false);
                cursor.moveToFirst();
                return false;
            }
        });
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

    //Bei einer Eingabe des Benutzers in das Suchfeld werden mittels Geocoder Suchvorschläge angezeigt
    public void getSearchSuggestions(SimpleCursorAdapter suggestionsAdapter, String query) {

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

    //bewegt die Kamera in der Map auf den Ort, nach dem gesucht wird
    public void search(String query) {

        List<Address> addresses;

        try {
            //versucht maximal 10-mal per Geocoder eine passende Adresse zu dem Eingabestring zu ermitteln, da manchmal die Adresse nicht gleich gefunden wird
            int counter = 0;
            do {
                addresses = geocoder.getFromLocationName(query, 1);
                counter++;
            } while (addresses.size() == 0 && counter < 10);

            if (addresses != null) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }

        } catch (Exception ignored) {

        }
    }

    //lese Infos aus dem JSON Array und schreibe sie in eine Liste von FeedItems
    public ArrayList<FeedItem> loadData(JSONArray feedData) {
        ArrayList<FeedItem> feedArrayList = new ArrayList<>();

        try {
            //lese Infos aus dem JSON Array und schreibe sie in eine Liste von FeedItems
            for (int i = 0; i < feedData.length(); i++) {
                JSONObject object = feedData.getJSONObject(i);
                String tutoringId = object.getString("tutoringId");
                String creationDate = object.getString("creationDate");
                String userName = object.getString("userName");
                String subject = object.getString("subject");
                String text = object.getString("text");
                String expirationDate = object.getString("expirationDate");
                Double latitude = Double.valueOf(object.getString("latitude"));
                Double longitude = Double.valueOf(object.getString("longitude"));
                Double distanceKm = Double.valueOf(object.getString("distanceKm"));
                FeedItem item = new FeedItem(tutoringId, creationDate, userName, subject, text, expirationDate, latitude, longitude, distanceKm);
                feedArrayList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return feedArrayList;
    }

    //Filtern der Tutorings nach dem gesuchten Fach (nur diejenigen werden angezeigt, deren Fach den vom Nutzer angegebenen String enthalten)
    public ArrayList<FeedItem> filterFeedItemList(ArrayList<FeedItem> feedArrayList) {
        ArrayList<FeedItem> filteredList = new ArrayList<>();
        for (FeedItem item : feedArrayList) {
            if (item.getSubject().toLowerCase().contains(subjectContains)) filteredList.add(item);
        }
        return filteredList;
    }

    //Anzeigen der Tutorings im Feed (aufsteigend nach Entfernung) und in der Map (Marker am jeweiligen Ort)
    public void showTutoringsInFeedAndMap(ArrayList<FeedItem> feedArrayList) {

        //Filtern der Tutorings, falls der Nutzer bei der Suche eine Eingabe zum Fach gemacht hat
        if (subjectContains != null && !Objects.equals(subjectContains, ""))
            feedArrayList = filterFeedItemList(feedArrayList);
        Collections.sort(feedArrayList, new FeedSorter());

        //Hinzufügen der Tutorings zur ListView per FeedItemAdapter
        FeedItemAdapter adapter = new FeedItemAdapter(feedArrayList, getContext());
        ListView feedListView = rootView.findViewById(R.id.feed_list_view);
        feedListView.setAdapter(adapter);
        final ArrayList<FeedItem> finalFeedArrayList = feedArrayList;

        //Bei Gedrückthalten eines Tutorings im Feed wird ein DetailTutoringFragment geöffnet
        feedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem item = finalFeedArrayList.get(position);
                DetailTutoringFragment detailTutoringFragment = new DetailTutoringFragment();
                detailTutoringFragment.setParams(item.getUserName(), item.getTutoringId(), item.getSubject(), item.getText(), item.getDistanceKm(), item.getCreationDate(), item.getExpirationDate());
                ((MainActivity) getActivity()).changeFragment(detailTutoringFragment, "DetailTutoring");
                return false;
            }
        });
        swipeContainer.setRefreshing(false);

        //Für jedes Tutoring wird ein Marker in der Map hinzugefügt
        googleMap.clear();
        Marker currentMarker;
        for (FeedItem item : feedArrayList) {
            String tutoringId = item.getTutoringId();
            LatLng pos = new LatLng(item.getLatitude(), item.getLongitude());
            String userName = item.getUserName();
            String subject = item.getSubject();
            currentMarker = googleMap.addMarker(new MarkerOptions().position(pos).title(userName).snippet(subject).draggable(true));
            currentMarker.setTag(item);
            markerTutoringIdHashMap.put(currentMarker, tutoringId);
        }
        //bewegen der Kamera zum aktuellen Standort
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(gpsBreitengrad, gpsLaengengrad)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(5));
    }

    //Backend-Request zum Erhalten der Tutorings
    public void getTutoringOffersFromBackend() {

        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/offers";

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("latitude", gpsBreitengrad);
            requestBody.put("longitude", gpsLaengengrad);
            requestBody.put("rangeKm", rangeKm);
            requestBody.put("rowLimit", rowLimit);
            requestBody.put("rowOffset", rowOffset);
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest jsArrRequest = new CustomJsonArrayRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //wenn der Request erfolgreich war, werden die Tutorings in Feed und Map angezeigt
                ArrayList<FeedItem> feedArrayList = loadData(response);
                showTutoringsInFeedAndMap(feedArrayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Übergeben des Requests an den RequestManager
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
    }

    //Senden eines Requests an das Backend zum Erhalten der Tutorings
    public void getTutoringRequestsFromBackend() {

        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/requests";

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("latitude", gpsBreitengrad);
            requestBody.put("longitude", gpsLaengengrad);
            requestBody.put("rangeKm", rangeKm);
            requestBody.put("rowLimit", rowLimit);
            requestBody.put("rowOffset", rowOffset);
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest jsArrRequest = new CustomJsonArrayRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //wenn der Request erfolgreich war, werden die Tutorings in Feed und Map angezeigt
                ArrayList<FeedItem> feedArrayList = loadData(response);
                showTutoringsInFeedAndMap(feedArrayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Übergeben des Requests an den RequestManager
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
    }

    //erzeugt ein JSONObject mit den Username und Passwort zur Authentifizierung im Backend
    public JSONObject getAuthenticationJson() {
        JSONObject authentication = new JSONObject();
        try {
            Utils utils = (((MainActivity)getActivity()).getUtils());

            authentication.put("userName", utils.getUserName());
            authentication.put("password", utils.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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

    //Setters

    public void setSubjectContains(String subjectContains) {this.subjectContains = subjectContains;}

    public void setGpsBreitengrad(double gpsBreitengrad) {
        this.gpsBreitengrad = gpsBreitengrad;
    }

    public void setGpsLaengengrad(double gpsLaengengrad) {
        this.gpsLaengengrad = gpsLaengengrad;
    }

    public void setRangeKm(int rangeKm) {
        this.rangeKm = rangeKm;
    }
}