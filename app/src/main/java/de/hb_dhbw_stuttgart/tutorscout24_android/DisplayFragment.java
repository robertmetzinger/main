package de.hb_dhbw_stuttgart.tutorscout24_android;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DisplayFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    View rootView;
    MapView mMapView;
    private GoogleMap googleMap;
    private HashMap<Marker, String> markerTutoringIdHashMap = new HashMap<Marker, String>();
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
    private SearchDialogFragment searchDialogFragment;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_display, container, false);
        ButterKnife.bind(this, rootView);

        Locale.Builder builder = new Locale.Builder();
        builder.setRegion("DE");
        builder.setLanguage("deu");
        Locale locale = builder.build();
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        getMyLocation();

        TabHost host = (TabHost) rootView.findViewById(R.id.tabHost);
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

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTutoringOffersFromBackend();
            }
        });

        searchDialogFragment = new SearchDialogFragment();
        searchDialogFragment.setParams(this, inflater, getContext(), getActivity(), geocoder);
        setUpFab(inflater);
        setUpSearchView();

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
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

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                UiSettings settings = googleMap.getUiSettings();
                settings.setMyLocationButtonEnabled(true);
                settings.setZoomControlsEnabled(true);
                settings.setMapToolbarEnabled(true);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String thisTutoringId = markerTutoringIdHashMap.get(marker);
                        Toast.makeText(getContext(), "Tutoring ID = " + thisTutoringId, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        FeedItem item = (FeedItem) marker.getTag();
                        DetailTutoringFragment detailTutoringFragment = new DetailTutoringFragment();
                        detailTutoringFragment.setParams(item.getUserName(),item.getTutoringId(),item.getSubject(),item.getText(),item.getDistanceKm(),item.getCreationDate(),item.getExpirationDate());
                        ((MainActivity)getActivity()).changeFragment(detailTutoringFragment,"DetailTutoring");
                    }
                    @Override
                    public void onMarkerDrag(Marker marker) {}
                    @Override
                    public void onMarkerDragEnd(Marker marker) {}
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

        getTutoringOffersFromBackend();

        return rootView;
    }

    public void setUpFab(final LayoutInflater inflater) {
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDialogFragment.createDialog();
                searchDialogFragment.openDialog();
            }
        });
    }


    public void setUpSearchView() {
        searchView = (SearchView) rootView.findViewById(R.id.searchView);

        final int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        suggestionsAdapterForMapSearch = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                columns,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        searchView.setSuggestionsAdapter(suggestionsAdapterForMapSearch);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getSearchSuggestions(suggestionsAdapterForMapSearch, newText);
                //if (suggestions != null) populateAdapter(suggestions);
                return true;
            }
        });
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

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

    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }
        try {

            Location myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            if (myLastLocation != null) {

                gpsBreitengrad = myLastLocation.getLatitude();
                gpsLaengengrad = myLastLocation.getLongitude();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Standort konnte nicht erfasst werden", Toast.LENGTH_SHORT).show();
        }
    }

    public void getSearchSuggestions(SimpleCursorAdapter suggestionsAdapter, String query) {

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

    public void search(String query) {

        List<Address> addresses = null;

        try {
            // Getting a maximum of 3 Address that matches the input
            // text
            int counter = 0;
            do {
                addresses = geocoder.getFromLocationName(query, 1);
                counter++;
            } while (addresses.size() == 0 && counter < 10);

            if (addresses != null && !addresses.equals("")) {
                Address address = (Address) addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }

        } catch (Exception e) {

        }
    }

    public void connectToGoogleGPSApi() {

        Log.e("Try connect", "gps: ");

        String[] LOCATION_PERMS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(LOCATION_PERMS, 1);
        googleApiClient.connect();
    }

    public ArrayList<FeedItem> loadData(JSONArray feedData) {
        ArrayList<FeedItem> feedArrayList = new ArrayList<>();

        try {
            //Backend Request um die Daten für den Feed zu erhalten
            //lese Infos aus dem JSON Array und schreibe sie in eine Liste von FeedItems
            for (int i = 0; i < feedData.length(); i++) {
                JSONObject object = feedData.getJSONObject(i);
                String tutoringId = object.getString("tutoringId");
                String creationDate = object.getString("creationDate");
                String userName = object.getString("userName");
                String subject = object.getString("subject");
                String text = object.getString("text");
                String expirationDate = object.getString("expirationDate");
                Double latitude = new Double(object.getString("latitude"));
                Double longitude = new Double(object.getString("longitude"));
                Double distanceKm = new Double(object.getString("distanceKm"));
                FeedItem item = new FeedItem(tutoringId, creationDate, userName, subject, text, expirationDate, latitude, longitude, distanceKm);
                feedArrayList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return feedArrayList;
    }

    public ArrayList<FeedItem> filterFeedItemList (ArrayList<FeedItem> feedArrayList) {
        ArrayList<FeedItem> filteredList = new ArrayList<>();
        for (FeedItem item : feedArrayList) {
            if (item.getSubject().toLowerCase().contains(subjectContains)) filteredList.add(item);
        }
        return filteredList;
    }

    public void showTutoringsInFeedAndMap(ArrayList<FeedItem> feedArrayList) {
        //erzeuge Listenobjekte für die ListView
        if (subjectContains != null && subjectContains != "") feedArrayList = filterFeedItemList(feedArrayList);
        feedItemAdapter adapter = new feedItemAdapter(feedArrayList, getContext());
        ListView feedListView = (ListView) rootView.findViewById(R.id.feed_list_view);
        feedListView.setAdapter(adapter);
        final ArrayList<FeedItem> finalFeedArrayList = feedArrayList;
        feedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem item = finalFeedArrayList.get(position);
                DetailTutoringFragment detailTutoringFragment = new DetailTutoringFragment();
                detailTutoringFragment.setParams(item.getUserName(),item.getTutoringId(),item.getSubject(),item.getText(),item.getDistanceKm(),item.getCreationDate(),item.getExpirationDate());
                ((MainActivity)getActivity()).changeFragment(detailTutoringFragment,"DetailTutoring");
                return false;
            }
        });
        swipeContainer.setRefreshing(false);

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
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(gpsBreitengrad, gpsLaengengrad)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(5));
    }

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
                ArrayList<FeedItem> feedArrayList = loadData(response);
                showTutoringsInFeedAndMap(feedArrayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

/*                String json = new String(error.networkResponse.data);
                json = trimMessage(json, "message");
                Log.e("", "onErrorResponse: " + json);
*/
            }
        });

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
    }

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
                ArrayList<FeedItem> feedArrayList = loadData(response);
                showTutoringsInFeedAndMap(feedArrayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
/*                String json = new String(error.networkResponse.data);
                json = trimMessage(json, "message");
                Log.e("", "onErrorResponse: " + json);
*/
            }
        });

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
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


    public void setSubjectContains(String subjectContains) {
        this.subjectContains = subjectContains;
    }

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