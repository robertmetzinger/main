package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;


/**
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class MyTutoringsFragment extends Fragment {

    private View rootView;
    private SwipeRefreshLayout swipeContainerOffers;
    private SwipeRefreshLayout swipeContainerRequests;

    public MyTutoringsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_my_tutorings, container, false);
        ButterKnife.bind(this, rootView);

        TabHost host = rootView.findViewById(R.id.tab_host_tutorings);
        host.setup();
        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Meine Angebote");
        spec.setContent(R.id.tabMyOffers);
        spec.setIndicator("Meine Angebote");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Meine Anfragen");
        spec.setContent(R.id.tabMyRequests);
        spec.setIndicator("Meine Anfragen");
        host.addTab(spec);

        swipeContainerOffers = rootView.findViewById(R.id.swipeContainerOffers);
        swipeContainerOffers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyOffersFromBackend();
            }
        });

        swipeContainerRequests = rootView.findViewById(R.id.swipeContainerRequests);
        swipeContainerRequests.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyRequestsFromBackend();
            }
        });

        getMyOffersFromBackend();
        getMyRequestsFromBackend();

        return rootView;
    }

    public void getMyOffersFromBackend() {

        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/myOffers";

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest jsArrRequest = new CustomJsonArrayRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ArrayList<FeedItem> feedArrayList = loadTutorings(response);
                showTutoringsInList(feedArrayList, "offers");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /*String json = new String(error.networkResponse.data);
                json = trimMessage(json, "message");
                Log.e("", "onErrorResponse: " + json);*/
            }
        });
        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
    }

    public void getMyRequestsFromBackend() {

        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/myRequests";

        //erstelle JSON Object für den Request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest jsArrRequest = new CustomJsonArrayRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ArrayList<FeedItem> feedArrayList = loadTutorings(response);
                showTutoringsInList(feedArrayList, "requests");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /*String json = new String(error.networkResponse.data);
                json = trimMessage(json, "message");
                Log.e("", "onErrorResponse: " + json);*/
            }
        });
        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
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

    public ArrayList<FeedItem> loadTutorings(JSONArray feedData) {
        ArrayList<FeedItem> feedArrayList = new ArrayList<>();

        try {
            //Backend Request um die Daten für den Feed zu erhalten
            //lese Infos aus dem JSON Array und schreibe sie in eine Liste von FeedItems
            for (int i = 0; i < feedData.length(); i++) {
                JSONObject object = feedData.getJSONObject(i);
                String tutoringId = object.getString("tutoringid");
                String creationDate = object.getString("creationdate");
                String userName = object.getString("createruserid");
                String subject = object.getString("subject");
                String text = object.getString("text");
                String expirationDate = object.getString("end");
                Double latitude = Double.valueOf(object.getString("latitude"));
                Double longitude = Double.valueOf(object.getString("longitude"));
                FeedItem item = new FeedItem(tutoringId, creationDate, userName, subject, text, expirationDate, latitude, longitude, null);
                feedArrayList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return feedArrayList;
    }

    public void showTutoringsInList(final ArrayList<FeedItem> feedArrayList, String type) {
        //erzeuge Listenobjekte für die ListView
        ListView feedListView = null;
        FeedItemAdapter adapter = new FeedItemAdapter(feedArrayList, getContext());
        if (type.equals("offers")) feedListView = rootView.findViewById(R.id.myOffers_list_view);
        else if (type.equals("requests")) feedListView = rootView.findViewById(R.id.myRequests_list_view);
        feedListView.setAdapter(adapter);
        feedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem item = feedArrayList.get(position);
                MyTutoringDetailFragment myTutoringDetailFragment = new MyTutoringDetailFragment();
                myTutoringDetailFragment.setParams(item.getUserName(),item.getTutoringId(),item.getSubject(),item.getText(),item.getCreationDate(),item.getExpirationDate());
                ((MainActivity)getActivity()).changeFragment(myTutoringDetailFragment,"MyTutoringDetail");
                return false;
            }
        });
        swipeContainerOffers.setRefreshing(false);
        swipeContainerRequests.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
