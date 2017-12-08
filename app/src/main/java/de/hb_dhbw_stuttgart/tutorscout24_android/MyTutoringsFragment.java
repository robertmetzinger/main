package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyTutoringsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyTutoringsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class MyTutoringsFragment extends Fragment {

    private View rootView;
    private SwipeRefreshLayout swipeContainerOffers;
    private SwipeRefreshLayout swipeContainerRequests;

    private OnFragmentInteractionListener mListener;

    public MyTutoringsFragment() {
        // Required empty public constructor
    }

    public static MyTutoringsFragment newInstance(String param1, String param2) {
        MyTutoringsFragment fragment = new MyTutoringsFragment();
        return fragment;
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

        TabHost host = (TabHost) rootView.findViewById(R.id.tab_host_tutorings);
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

        swipeContainerOffers = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerOffers);
        swipeContainerOffers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyOffersFromBackend();
            }
        });

        swipeContainerRequests = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerRequests);
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
                Double latitude = new Double(object.getString("latitude"));
                Double longitude = new Double(object.getString("longitude"));
                FeedItem item = new FeedItem(tutoringId, creationDate, userName, subject, text, expirationDate, latitude, longitude, null);
                feedArrayList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return feedArrayList;
    }

    public void showTutoringsInList(ArrayList<FeedItem> feedArrayList, String type) {
        //erzeuge Listenobjekte für die ListView
        ListView feedListView = null;
        feedItemAdapter adapter = new feedItemAdapter(feedArrayList, getContext());
        if (type.equals("offers")) feedListView = (ListView) rootView.findViewById(R.id.myOffers_list_view);
        else if (type.equals("requests")) feedListView = (ListView) rootView.findViewById(R.id.myRequests_list_view);
        feedListView.setAdapter(adapter);
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
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
