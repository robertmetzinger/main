package de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.CustomJsonObjectRequest;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;


/**
 * Created by Robert
 */

//Dieses Fragment ist eine Detailansicht eines eigenen Tutorings
@RequiresApi(api = Build.VERSION_CODES.M)
public class MyTutoringDetailFragment extends Fragment {

    private String userName;
    private String tutoringId;
    private String subject;
    private String description;
    private String creationDate;
    private String expirationDate;

    public MyTutoringDetailFragment() {
        // Required empty public constructor
    }

    //Die Daten des aufgerufenen Tutorings werden hier übergeben
    public void setParams(String userName, String tutoringId, String subject, String description, String creationDate, String expirationDate) {
        this.userName = userName;
        this.tutoringId = tutoringId;
        this.subject = subject;
        this.description = description;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_tutoring_detail, container, false);
        ButterKnife.bind(this, view);

        TextView userTitle = view.findViewById(R.id.titleTxt2);
        TextView subj = view.findViewById(R.id.subjectTxt2);
        TextView id = view.findViewById(R.id.tutoringIdTxt2);
        TextView descr = view.findViewById(R.id.descriptionTxt2);
        TextView creDate = view.findViewById(R.id.creationDateTxt2);
        TextView expDate = view.findViewById(R.id.expirationDateTxt2);

        //Schreiben der Daten in die entsprechenden TextViews
        userTitle.setText(userName);
        subj.setText(subject);
        id.setText(tutoringId);
        descr.setText(description);
        creDate.setText(formatDateString(creationDate));
        expDate.setText(formatDateString(expirationDate));

        return view;
    }

    //Methode zum Umformatieren der Datumangaben für eine nutzerfreundliche Darstellung
    private String formatDateString(String dateString) {
        SimpleDateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat dateToString = new SimpleDateFormat("dd.MM.yyyy\nHH:mm", Locale.getDefault());
        try {
            Date date = stringToDate.parse(dateString);
            dateString = dateToString.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    //Beim Klicken des Löschen-Buttons wird ein Bestätigungs-Dialog geöffnet. Nach Akzeptieren wird ein Backend-Request zum Löschen des Tutorings erstellt
    @OnClick(R.id.btnDeleteTutoring)
    public void OnDeleteButtonPressed() {
        new AlertDialog.Builder(getContext())
                .setTitle("")
                .setMessage("Soll dieses Tutoring wirklich gelöscht werden?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Löschen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteTutoring();
                    }
                })
                .setNegativeButton("Abbrechen", null).show();
    }

    //Senden eines Requests an das Backend zum Löschen des entsprechenden Tutorings
    public void deleteTutoring() {
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/delete";

        //erstelle JSON Object für den Request
        final JSONObject requestBody = new JSONObject();
        int tutoringIdInteger = Integer.parseInt(tutoringId);
        try {
            requestBody.put("tutoringId", tutoringIdInteger);
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Request Body", requestBody.toString());
        CustomJsonObjectRequest request = new CustomJsonObjectRequest(Request.Method.DELETE, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Tutoring wurde gelöscht", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Löschen fehlgeschlagen", Toast.LENGTH_SHORT).show();
                        String json = new String(error.networkResponse.data);
                        //json = trimMessage(json, "message");
                        Log.e("", "onErrorResponse: " + json);
                    }
                });
        // Übergeben des Requests an den RequestManager
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(request);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
