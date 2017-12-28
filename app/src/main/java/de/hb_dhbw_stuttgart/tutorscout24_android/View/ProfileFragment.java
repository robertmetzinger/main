package de.hb_dhbw_stuttgart.tutorscout24_android.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.CustomJsonObjectRequest;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.Utils;
import de.hb_dhbw_stuttgart.tutorscout24_android.Model.Communication.User;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;


/**
 * Created by patrick.woehnl on 03.11.2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class ProfileFragment extends android.app.Fragment {

    private User currentUser;

    private Utils utils;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        Log.e("TAg", "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        utils = (((MainActivity) getActivity()).getUtils());
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        Log.d("BIn", "onCreateView: ");


        return view;
    }

    @Override
    public void onAttach(Context context) {
        utils = (((MainActivity) getActivity()).getUtils());
        getUserInfo();
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.btnSpeichern)
    public void saveUser() {

        String updateUserURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/updateUser";

        if (getView() == null) {
            return;
        }
        EditText firstName = getView().findViewById(R.id.txtProfileFirstName);
        EditText lastName = getView().findViewById(R.id.txtProfileLastName);
        EditText geschlecht = getView().findViewById(R.id.txtProfileLastGender);
        EditText alter = getView().findViewById(R.id.txtProfileAge);
        EditText wohnort = getView().findViewById(R.id.txtProfileAdress);
        EditText mail = getView().findViewById(R.id.txtProfileMail);
        EditText akademischGrad = getView().findViewById(R.id.txtProfileGraduation);
        EditText note = getView().findViewById(R.id.txtProfileNotiz);

        JSONObject params = new JSONObject();
        try {

            //Umwandlung des Formates in das des Backends
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd", Locale.GERMANY);
            SimpleDateFormat oldFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.GERMANY);
            try {
                Date date = oldFormat.parse("" + alter.getText().toString());
                alter.setText(newFormat.format(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }

            params.put("password", utils.getPassword());
            params.put("firstName", firstName.getText().toString());
            params.put("lastName", lastName.getText().toString());
            params.put("birthdate", alter.getText().toString());
            params.put("gender", geschlecht.getText().toString());
            if (currentUser.email.compareTo(mail.getText().toString()) != 0) {
                params.put("email", mail.getText().toString());
            }
            params.put("note", note.getText().toString());
            params.put("placeOfResidence", wohnort.getText().toString());
            params.put("maxGraduation", akademischGrad.getText().toString());
            params.put("authentication", getAuthenticationJsonb());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonObjectRequest jsObjRequest = new CustomJsonObjectRequest
                (Request.Method.PUT, updateUserURL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Daten wurden gespeichert.", Toast.LENGTH_SHORT).show();
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getContext(), "Fehler beim Speichern der Daten.", Toast.LENGTH_SHORT).show();

                                String json = new String(error.networkResponse.data);
                                json = trimMessage(json);
                                Log.e("", "onErrorResponse: " + json);
                            }
                        });

// Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsObjRequest);


    }

    public String trimMessage(String json) {
        String trimmedString;

        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }

    public JSONObject getAuthenticationJsonb() {
        JSONObject authentication = new JSONObject();
        try {
            authentication.put("userName", utils.getUserName());
            authentication.put("password", utils.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }

    public void getUserInfo() {


        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/myUserInfo";


        JSONObject requestBody = utils.getFullAuthenticationJson();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, usercreateURL, requestBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            currentUser = new User();
                            currentUser.userName = response.getString("userid");
                            currentUser.firstName = response.getString("firstName");
                            currentUser.lastName = response.getString("lastName");
                            currentUser.age = Integer.parseInt(response.getString("dayOfBirth"));
                            currentUser.email = response.getString("email");
                            currentUser.maxGraduation = response.getString("maxGraduation");
                            currentUser.placeOfResidence = response.getString("placeOfResidence");
                            currentUser.note = response.getString("description");
                            currentUser.gender = response.getString("gender");

                            SetUserInfo();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (utils.getUserName() == null) {
                            return;
                        }

                        Toast.makeText(getContext(), "Fehler beim abrufen des Profils", Toast.LENGTH_SHORT).show();

                    }
                });

        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }

    @SuppressLint("SetTextI18n")
    private void SetUserInfo() {

        if (getView() == null) {
            return;
        }
        EditText firstName = getView().findViewById(R.id.txtProfileFirstName);
        EditText lastName = getView().findViewById(R.id.txtProfileLastName);
        EditText geschlecht = getView().findViewById(R.id.txtProfileLastGender);
        EditText alter = getView().findViewById(R.id.txtProfileAge);
        EditText wohnort = getView().findViewById(R.id.txtProfileAdress);
        EditText mail = getView().findViewById(R.id.txtProfileMail);
        EditText akademischGrad = getView().findViewById(R.id.txtProfileGraduation);
        EditText note = getView().findViewById(R.id.txtProfileNotiz);

        //Umwandlung des Formates vom Backand in ein Normales
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyyMMdd", Locale.GERMANY);
        SimpleDateFormat newFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.GERMANY);
        try {
            Date date = oldFormat.parse("" + currentUser.age);
            alter.setText(newFormat.format(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }


        firstName.setText(currentUser.firstName);
        lastName.setText(currentUser.lastName);
        wohnort.setText(currentUser.placeOfResidence);
        mail.setText(currentUser.email);
        geschlecht.setText(currentUser.gender);
        akademischGrad.setText(currentUser.maxGraduation);
        note.setText(currentUser.note);
    }

}
