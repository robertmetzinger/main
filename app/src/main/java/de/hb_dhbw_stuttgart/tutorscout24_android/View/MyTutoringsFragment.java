package de.hb_dhbw_stuttgart.tutorscout24_android.View;

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
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.CustomJsonArrayRequest;
import de.hb_dhbw_stuttgart.tutorscout24_android.Model.FeedItem;
import de.hb_dhbw_stuttgart.tutorscout24_android.Model.FeedItemAdapter;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;


/**
 * Created by Robert
 */

//Dieses Fragment dient zum Anzeigen der eigenen Tutorings in einer ListView
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
                //Beim Runterswipen werden die eigenen Offers neu aus dem Backend geladen
                getMyOffersFromBackend();
            }
        });

        swipeContainerRequests = rootView.findViewById(R.id.swipeContainerRequests);
        swipeContainerRequests.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Beim Runterswipen werden die eigenen Requests neu aus dem Backend geladen
                getMyRequestsFromBackend();
            }
        });

        //Initiales Anzeigen der eigenen Offers und Requests
        getMyOffersFromBackend();
        getMyRequestsFromBackend();

        return rootView;
    }

    //Senden eines Requests an das Backend zum Erhalten der eigenen Tutoring-Offers
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
                //wenn der Request erfolgreich war, werden die eigenen Tutoring-Offers in der ListView angezeigt
                ArrayList<FeedItem> feedArrayList = loadTutorings(response);
                showTutoringsInList(feedArrayList, "offers");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        // Übergeben des Requests an den RequestManager
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsArrRequest);
    }

    //Senden eines Requests an das Backend zum Erhalten der eigenen Tutoring-Requests
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
                //wenn der Request erfolgreich war, werden die eigenen Tutoring-Requests in der ListView angezeigt
                ArrayList<FeedItem> feedArrayList = loadTutorings(response);
                showTutoringsInList(feedArrayList, "requests");
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
            authentication.put("userName", MainActivity.getUserName());
            authentication.put("password", MainActivity.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }

    //lese Infos aus dem JSON Array und schreibe sie in eine Liste von FeedItems
    public ArrayList<FeedItem> loadTutorings(JSONArray feedData) {
        ArrayList<FeedItem> feedArrayList = new ArrayList<>();

        try {
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

    //Anzeigen der Tutorings in der ListView
    public void showTutoringsInList(final ArrayList<FeedItem> feedArrayList, String type) {

        //Auswählen der jeweiligen ListView, in der die Tutorings angezeigt werden (Offers oder Requests)
        ListView feedListView = null;
        if (type.equals("offers")) feedListView = rootView.findViewById(R.id.myOffers_list_view);
        else if (type.equals("requests")) feedListView = rootView.findViewById(R.id.myRequests_list_view);

        //Hinzufügen der Tutorings zur ListView per FeedItemAdapter
        FeedItemAdapter adapter = new FeedItemAdapter(feedArrayList, getContext());
        feedListView.setAdapter(adapter);

        //Bei Gedrückthalten eines Tutorings wird ein MyTutoringDetailFragment geöffnet
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
