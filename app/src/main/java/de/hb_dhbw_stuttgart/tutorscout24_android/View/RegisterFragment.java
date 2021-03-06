package de.hb_dhbw_stuttgart.tutorscout24_android.View;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;


/*
  Created by patrick.woehnl on 19.11.2017.
 */

/**
 * Das RegistrierenFragment.
 * <p>
 * Dieses Fragment übernimmt das Registrieren.
 */
public class RegisterFragment extends android.app.Fragment {

    final Calendar myCalendar = Calendar.getInstance();
    private String userName;
    private String password;

    public RegisterFragment() {
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
        View view = inflater.inflate(R.layout.fragment_registrieren, container, false);
        ButterKnife.bind(this, view);

        EditText geburtstag = view.findViewById(R.id.txtGeburtstag);

        geburtstag.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    setGeburtstag();
                }
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     * Verscuht einen neuen Nutzer zu erstellen, der im Backend gespeichert wird.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnRegistrien)
    public void saveUser() {

        // Kein Feld darf leer sein ( außer Notizen)
        if (!validateEmpty()) {
            return;
        }

        // Das Passwort muss den Anforderungen entsprechen.
        if (!ceckPassword()) {
            return;
        }


        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/create";

        StringRequest strRequest = new StringRequest(Request.Method.POST, usercreateURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Nutzer erfolgreich erstellt.

                        Log.e("response ", response);
                        ((MainActivity) getActivity()).setUser(userName, password);

                        android.app.Fragment blankFragment = new BlankFragment();
                        ((MainActivity) getActivity()).changeFragment(blankFragment, "Blank");
                        ((MainActivity) getActivity()).enableNavigation();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {

                            // Nutzer konnte nicht erstellt werden
                            NetworkResponse response = error.networkResponse;

                            String json = new String(response.data);

                            Log.e("", "onErrorResponse: " + json);
                            Toast.makeText(getContext(), json, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Bitte überprüfen Sie ihre Internetverbindung.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.headers);
                }
                if (response != null) {
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
                return null;
            }

            // Mapt die bestandteile des Users.
            @Override
            protected Map<String, String> getParams() {
                if (getView() == null) {
                    return null;
                }

                EditText benutzerName = getView().findViewById(R.id.txtBenutzername);
                EditText firstName = getView().findViewById(R.id.txtFirstName);
                EditText lastName = getView().findViewById(R.id.textLastName);
                EditText geschlecht = getView().findViewById(R.id.txtGeschlecht);
                EditText wohnort = getView().findViewById(R.id.txtWohnort);
                EditText mail = getView().findViewById(R.id.txtMail);
                EditText passwort = getView().findViewById(R.id.txtLoginPasswort);
                EditText akademischGrad = getView().findViewById(R.id.txtAbschluss);
                EditText notiz = getView().findViewById(R.id.txtNotiz);

                String myFormat = "yyyyMMdd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);

                userName = benutzerName.getText().toString();
                password = passwort.getText().toString();

                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);
                params.put("password", password);
                params.put("firstName", firstName.getText().toString());
                params.put("lastName", lastName.getText().toString());
                params.put("birthdate", sdf.format(myCalendar.getTime()));
                params.put("gender", geschlecht.getText().toString());
                params.put("email", mail.getText().toString());
                params.put("note", notiz.getText().toString());
                params.put("placeOfResidence", wohnort.getText().toString());
                params.put("maxGraduation", akademischGrad.getText().toString());

                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(strRequest);
    }

    /**
     * Überprüft ob Felder leer sind.
     *
     * @return true falls kein Feld leer ist, sonst false.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean validateEmpty() {
        if (getView() == null) {
            return false;
        }

        EditText benutzerName = getView().findViewById(R.id.txtBenutzername);
        EditText firstName = getView().findViewById(R.id.txtFirstName);
        EditText lastName = getView().findViewById(R.id.textLastName);
        EditText geschlecht = getView().findViewById(R.id.txtGeschlecht);
        EditText wohnort = getView().findViewById(R.id.txtWohnort);
        EditText mail = getView().findViewById(R.id.txtMail);
        EditText akademischGrad = getView().findViewById(R.id.txtAbschluss);

        if (benutzerName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Der Benutzername darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (firstName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Der Vorname darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (lastName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Der Nachname darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (geschlecht.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Das Geschlecht darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (wohnort.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Der Wohnort darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mail.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Die Email Adresse darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (akademischGrad.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Der Akademische Grad darf nicht leer sein.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * Überprüft ob das Passwort korrekt ist.
     *
     * @return true falls das Passwort korrekt ist, sonst false.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean ceckPassword() {
        if (getView() == null) {
            return false;
        }
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

    /**
     * Setzt den Geburtstag
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setGeburtstag() {

        if (getView() == null) {
            return;
        }

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
                EditText geburtstag = getView().findViewById(R.id.txtGeburtstag);

                geburtstag.setText(sdf.format(myCalendar.getTime()));
                EditText wohnort = getView().findViewById(R.id.txtWohnort);
                wohnort.requestFocus();
            }

        };

        new DatePickerDialog(getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }
}

