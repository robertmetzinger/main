package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class RegistrierenFragment extends android.app.Fragment {

    private OnFragmentInteractionListener mListener;

    public RegistrierenFragment() {
        // Required empty public constructor
    }


    public static RegistrierenFragment newInstance(String param1, String param2) {
        RegistrierenFragment fragment = new RegistrierenFragment();

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
        View view = inflater.inflate(R.layout.fragment_registrieren, container, false);
        ButterKnife.bind(this, view);
        return view;
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


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnRegistrien)
    public void saveUser() {

        ((MainActivity) getActivity()).EnableNavigation();

        if (!CeckPassword()) {
            return;
        }
        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/create";

        StringRequest strRequest = new StringRequest(Request.Method.POST, usercreateURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                        Log.e("response ", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;

                        String json = new String(response.data);
                        json = trimMessage(json, "message");
                        Log.e("", "onErrorResponse: " + json );
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                EditText benutzerName = getView().findViewById(R.id.txtBenutzername);
                EditText firstName = getView().findViewById(R.id.txtFirstName);
                EditText lastName = getView().findViewById(R.id.textLastName);
                EditText geschlecht = getView().findViewById(R.id.txtGeschlecht);
                EditText alter = getView().findViewById(R.id.txtAlter);
                EditText wohnort = getView().findViewById(R.id.txtWohnort);
                EditText mail = getView().findViewById(R.id.txtMail);
                EditText passwort = getView().findViewById(R.id.txtLoginPasswort);
                EditText akademischGrad = getView().findViewById(R.id.txtAbschluss);


                Map<String, String> params = new HashMap<>();
                params.put("userName", benutzerName.getText().toString());
                params.put("password", passwort.getText().toString());
                params.put("firstName", firstName.getText().toString());
                params.put("lastName", lastName.getText().toString());
                params.put("age", alter.getText().toString());
                params.put("gender", geschlecht.getText().toString());
                params.put("email", mail.getText().toString());
                params.put("note", "keine Notiz");
                params.put("placeOfResidence", wohnort.getText().toString());
                params.put("maxGraduation", akademischGrad.getText().toString());

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(strRequest);

        android.app.Fragment blankFragment = new BlankFragment();
        ((MainActivity) getActivity()).ChangeFragment(blankFragment, "Blank");

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


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean CeckPassword() {
        EditText passwort = getView().findViewById(R.id.txtLoginPasswort);
        EditText passwortwdh = getView().findViewById(R.id.txtPasswortWdh);

        String pw = passwort.getText().toString();
        String pwwdh = passwortwdh.getText().toString();

        if (!pw.equals(pwwdh)) {
            Toast.makeText(getContext(), "Das Eingegebene Passwort stimmt nicht mit der Wiederholung überein.", Toast.LENGTH_LONG).show();
            passwortwdh.setText("");
            return false;
        }

        // pw muss größer 8 Zeichen und kleiner 70 Zeichen sein
        if (pw.length() < 8) {
            Toast.makeText(getContext(), "Das Eingegebene Passwort ist zu kurz.", Toast.LENGTH_LONG).show();
            return false;
        }
        // pw muss größer 8 Zeichen und kleiner 70 Zeichen sein
        if (pw.length() > 70) {
            Toast.makeText(getContext(), "Das Eingegebene Passwort ist zu lang.", Toast.LENGTH_LONG).show();
            return false;
        }

        //pw muss min 1 Sonderzeichen enthalten.
        if (!pw.matches(".*[!@#$%*()-=_+'~,.<>/?;:|{}].*")) {
            Toast.makeText(getContext(), "Das Eingegebene Passwort muss mindestens ein Sonderzeichen enthalten.", Toast.LENGTH_LONG).show();

            return false;
        }
        //pw muss min 1 Kleinbuschtaben enthalten.
        if (!pw.matches(".*[qwertzuiopüasdfghjklöäyxcvbnm].*")) {
            Toast.makeText(getContext(), "Das Eingegebene Passwort muss mindestens einen Kleinbuchstaben enthalen.", Toast.LENGTH_LONG).show();
            return false;
        }

        //pw muss min 1 Großbuchstaben enthalten.
        if (!pw.matches(".*[QWERTZUIOPÜASDFGHJKLÖÄYXCVBNM].*")) {
            Toast.makeText(getContext(), "Das Eingegebene Passwort muss mindestens einen Großbuchstaben enhalten.", Toast.LENGTH_LONG).show();
            return false;
        }

        //pw muss min 1 Zahl enthalten.
        if (!pw.matches(".*[1234567890].*")) {
            Toast.makeText(getContext(), "Das Eingegebene Passwort muss mindestens eine Zahl ethalten.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}

