package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DisplayFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    View rootView;

    @RequiresApi(api = Build.VERSION_CODES.M)
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

        //Da der Request im Backend noch nicht implementiert ist, werden Mockup Daten zum Anzeigen verwendet
        JSONObject jsonObject;
        JSONArray feedData;
        ArrayList<FeedItem> feedArrayList = new ArrayList<>();
        String jsonString = getString(R.string.mockdata);

        try {
            //MockDaten in JSON Array umwandeln
            jsonObject = new JSONObject(jsonString);
            feedData = jsonObject.getJSONArray("tutorien");

            //lese Infos aus dem JSON Array und schreibe sie in eine Liste von FeedItems
            for (int i = 0; i < feedData.length(); i++) {
                JSONObject object = feedData.getJSONObject(i);
                String creator = object.getString("creator");
                String subject = object.getString("subject");
                String info = object.getString("info");
                String creationDate = object.getString("creationDate");
                FeedItem item = new FeedItem(creator, subject, info, creationDate);
                feedArrayList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //erzeuge Listenobjekte für die ListView
        feedItemAdapter adapter = new feedItemAdapter(feedArrayList, getContext());
        ListView feedListView = (ListView) rootView.findViewById(R.id.feed_list_view);
        feedListView.setAdapter(adapter);

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
                //googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng horb = new LatLng(48.442078, 8.684851200000026);
                googleMap.addMarker(new MarkerOptions().position(horb).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(horb).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnLoadFeedDataFromBackend)
    public void loadData() {
        JSONArray feedData;
        ArrayList<FeedItem> feedArrayList = new ArrayList<>();

        try {
            //Backend Request um die Daten für den Feed zu erhalten
            JSONObject jsonObject = getTutoringListAsObjectFromBackend();
            feedData = jsonObject.getJSONArray("");
            //feedData = getTutoringListAsArrayFromBackend();

            //lese Infos aus dem JSON Array und schreibe sie in eine Liste von FeedItems
            for (int i = 0; i < feedData.length(); i++) {
                JSONObject object = feedData.getJSONObject(i);
                String creator = object.getString("userName");
                String subject = object.getString("tutoringId");
                String info = object.getString("distanceKm");
                String creationDate = object.getString("creationDate");
                FeedItem item = new FeedItem(creator, subject, info, creationDate);
                feedArrayList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //erzeuge Listenobjekte für die ListView
        feedItemAdapter adapter = new feedItemAdapter(feedArrayList, getContext());
        ListView feedListView = (ListView) rootView.findViewById(R.id.feed_list_view);
        feedListView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public JSONArray getTutoringListAsArrayFromBackend() {

        final JSONArray[] feedList = new JSONArray[1];
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/offers";

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("latitude", 48.442078);
            requestBody.put("longitude", 8.684851200000026);
            requestBody.put("rangeKm", 50);
            requestBody.put("rowLimit", 100);
            requestBody.put("rowOffset", 0);
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray requestBodySingletonArray = new JSONArray();
        try {
            requestBodySingletonArray.put(0, requestBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonArrayRequest jsArrRequest = new JsonArrayRequest(Request.Method.POST, url, requestBodySingletonArray, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(getContext(), "Response: " + response.toString(), Toast.LENGTH_SHORT).show();
                feedList[0] = response;
            }

        }, new Response.ErrorListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Response: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
        return feedList[0];
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public JSONObject getTutoringListAsObjectFromBackend() {

        final JSONObject[] feedList = new JSONObject[1];
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/offers";

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("latitude", 48.442078);
            requestBody.put("longitude", 8.684851200000026);
            requestBody.put("rangeKm", 50);
            requestBody.put("rowLimit", 100);
            requestBody.put("rowOffset", 0);
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getContext(), "Response: " + response.toString(), Toast.LENGTH_SHORT).show();
                feedList[0] = response;
            }

        }, new Response.ErrorListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Response: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsObjRequest);
        return feedList[0];
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
}