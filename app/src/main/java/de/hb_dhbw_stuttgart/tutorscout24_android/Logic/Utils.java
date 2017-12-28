package de.hb_dhbw_stuttgart.tutorscout24_android.Logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.hb_dhbw_stuttgart.tutorscout24_android.R;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.Communication.ContactFragment;

/**
 * Created by patrick.woehnl on 28.12.2017.
 */

public class Utils {

    private MainActivity mainActivity;

    public void setKontateFragment(ContactFragment kontateFragment) {
        this.kontateFragment = kontateFragment;
    }

    private ContactFragment kontateFragment;

    private String userName = null;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password = null;

    public ArrayList<String> kontakte;

    public String getChatUser() {
        return chatUser;
    }

    public void setChatUser(String chatUser) {
        this.chatUser = chatUser;
    }

    public String chatUser = null;

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    Utils(MainActivity mainActivity, ContactFragment kontateFragment){
        this.mainActivity = mainActivity;
        this.kontateFragment = kontateFragment;
        kontakte = new ArrayList<>();
    }


    /**
     * Disabled die BottomNavigationView.
     */
    public void DisableNavigation(BottomNavigationView view) {

        view.setEnabled(false);

        view.getMenu().getItem(0).setEnabled(false);
        view.getMenu().getItem(1).setEnabled(false);
        view.getMenu().getItem(2).setEnabled(false);
        view.getMenu().getItem(3).setEnabled(false);
        view.getMenu().getItem(4).setEnabled(false);

        view.setVisibility(View.GONE);
    }


    /**
     * Disabled die BottomNavigationView.
     */
    public void EnableNavigation(BottomNavigationView view) {
        view.setEnabled(true);

        view.getMenu().getItem(0).setEnabled(true);
        view.getMenu().getItem(1).setEnabled(true);
        view.getMenu().getItem(2).setEnabled(true);
        view.getMenu().getItem(3).setEnabled(true);
        view.getMenu().getItem(4).setEnabled(true);

        view.setVisibility(View.VISIBLE);
    }

    public JSONObject getFullAuthenticationJson() {
        JSONObject authentication = new JSONObject();
        JSONObject aut = new JSONObject();
        try {
            authentication.put("userName", getUserName());
            authentication.put("password", getPassword());
            aut.put("authentication", authentication);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return aut;
    }


    public JSONObject getUserPasswordAuthenticationJson() {
        JSONObject authentication = new JSONObject();
        try {
            authentication.put("userName", getUserName());
            authentication.put("password", getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void loadRecievedMessages(Context context) {

        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/message/getUnreadMessages";

        //erstelle JSON Object für den Request

        CustomJsonArrayRequest a = new CustomJsonArrayRequest(Request.Method.POST, url, getFullAuthenticationJson(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject o = (JSONObject) response.get(i);
                        chatUser = o.getString("fromUserId");

                        addKontakt(chatUser);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mainActivity.notification();
            }

        }, new Response.ErrorListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(context).addToRequestQueue(a);
    }




    public ArrayList<String> getKontakte() {
        SharedPreferences settings = mainActivity.getSharedPreferences("KontaktListe" + getUserName(), 0);
        String[] defaultString = {"Keine Kontakte gefunden"};
        Set<String> defaultSet = new HashSet<>(Arrays.asList(defaultString));
        kontakte = new ArrayList<>();
        kontakte.addAll(settings.getStringSet("Kontakte", defaultSet));
        return kontakte;
    }


    public void addKontakt(String kontakt) {
        if (!kontakte.contains(kontakt)) {
            kontakte.add(kontakt);
        }

        if (kontateFragment != null) {
            kontateFragment.listAdapter.notifyDataSetChanged();
        }

        SharedPreferences settings = mainActivity.getSharedPreferences("KontaktListe" + getUserName(), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("Kontakte", new HashSet<>(kontakte));

        // Commit the edits!
        editor.apply();
    }
}
