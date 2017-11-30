package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DisplayFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    View rootView;
    MapView mMapView;
    private GoogleMap googleMap;
    GoogleApiClient googleApiClient;
    private HashMap<Marker, String> markerTutoringIdHashMap = new HashMap<Marker, String>();
    private double gpsBreitengrad;
    private double gpsLaengengrad;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_display, container, false);
        ButterKnife.bind(this, rootView);

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
                /*googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        googleApiClient.connect();
                        return true;
                    }
                });*/
                UiSettings settings = googleMap.getUiSettings();
                settings.setMyLocationButtonEnabled(true);
                settings.setZoomControlsEnabled(true);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String thisTutoringId = markerTutoringIdHashMap.get(marker);
                        Toast.makeText(getContext(), "Tutoring ID = " + thisTutoringId, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void connectToGoogleGPSApi() {

        Log.e("Try connect", "gps: ");

        String[] LOCATION_PERMS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(LOCATION_PERMS, 1);
        googleApiClient.connect();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
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
                String distanceKm = object.getString("distanceKm");
                FeedItem item = new FeedItem(tutoringId, creationDate, userName, subject, text, expirationDate, latitude, longitude, distanceKm);
                feedArrayList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return feedArrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setUpFeedAndMap (ArrayList<FeedItem> feedArrayList){
        //erzeuge Listenobjekte für die ListView
        feedItemAdapter adapter = new feedItemAdapter(feedArrayList, getContext());
        ListView feedListView = (ListView) rootView.findViewById(R.id.feed_list_view);
        feedListView.setAdapter(adapter);

        Marker currentMarker;
        for(FeedItem item : feedArrayList){
            String tutoringId = item.getTutoringId();
            LatLng pos = new LatLng(item.getLatitude(), item.getLongitude());
            String userName = item.getUserName();
            String subject = item.getSubject();
            currentMarker = googleMap.addMarker(new MarkerOptions().position(pos).title(userName).snippet(subject));
            markerTutoringIdHashMap.put(currentMarker, tutoringId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnLoadFeedDataFromBackend)
    public void getTutoringListAsArrayFromBackend2() {

        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/offers";

        Number latitude = 10;
        Number longitude = 10;
        Number rangeKm = 50;
        Number rowLimit = 100;
        Number rowOffset = 0;

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("latitude", latitude);
            requestBody.put("longitude", longitude);
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
                setUpFeedAndMap(feedArrayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = new String(error.networkResponse.data);
                json = trimMessage(json, "message");
                Log.e("", "onErrorResponse: " + json );

            }
        });

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);

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
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }

        Location myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (myLastLocation != null) {

            gpsBreitengrad = myLastLocation.getLatitude();
            gpsLaengengrad = myLastLocation.getLongitude();

            ShowMyLocation();
        }
    }

    private void ShowMyLocation() {

        LatLng myLocation = new LatLng(gpsBreitengrad, gpsLaengengrad);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}