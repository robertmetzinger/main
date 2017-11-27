package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.Fragment;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hb_dhbw_stuttgart.tutorscout24_android.R;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class DisplayFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_display, container, false);

        TabHost host = (TabHost) rootView.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Feed");
        spec.setContent(R.id.tabFeed);
        spec.setIndicator("Feed");
        host.addTab(spec);

        JSONArray feedData;
        ArrayList<FeedItem> feedArrayList = new ArrayList<>();
        String jsonString = getString(R.string.mockdata);

        try {
            //generiere JSON Array aus den Mock Daten
            JSONObject jsnobject = new JSONObject(jsonString);
            feedData = jsnobject.getJSONArray("tutorien");

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
                LatLng sydney = new LatLng(-34, 151);
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return rootView;
    }

    private static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        InputStream is = null;
        try {
            AssetManager manager = context.getAssets();
            Log.d(TAG, "path " + jsonFileName);
            is = manager.open(jsonFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
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