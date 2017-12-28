package de.hb_dhbw_stuttgart.tutorscout24_android.View.Communication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.HttpRequestManager;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.Utils;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;


/*
  Created by patrick.woehnl on 26.11.2017.
 */

/**
 * Das Contact Fragment.
 *
 * Dieses Fragment übernimmt das Management der Kontakte und öffnet einen Chat falls nötig.
 */
public class ContactFragment extends android.app.Fragment {

    private ArrayList<String> kontakte;
    public ArrayAdapter<String> listAdapter;
    boolean isDeleteEnabled = false;
    private Utils utils;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        utils = (((MainActivity)getActivity()).getUtils());
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kontakte, container, false);
        ButterKnife.bind(this, view);

       // Restore preferences
        kontakte = new ArrayList<>();
        kontakte = ((MainActivity)getActivity()).getUtils().getKontakte();
        setKontakteList(view);

        return view;
    }

    /**
     * Setzt die KontaktListe.
     *
     * @param view Die view.
     */
    private void setKontakteList(View view) {


        ListView kontakteListView = view.findViewById(R.id.chat_list_view);

        if(kontakteListView == null){
            return;
        }

        kontakteListView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final TextView v = (TextView) view;

                // Löschen eines Kontaks aus der Liste.
                // Abfrage mit hilfe eines Dialogs.
                if(isDeleteEnabled){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Sind Sie sicher, dass dieser Kontakt gelöscht werden soll?");

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteKontakt(v);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    isDeleteEnabled = false;
                    return;
                }

                // Öffnet einen neuen Chat mit dem ausgewählten User.
                Toast.makeText(getContext(), v.getText(), Toast.LENGTH_SHORT).show();
                ChatFragment chatFragment= new ChatFragment();
                utils.setChatUser(v.getText().toString());
                ((MainActivity)getActivity()).changeFragment(chatFragment,"Chat");
            }
        });

       listAdapter =
               new ArrayAdapter<>(
                       getActivity(),
                       R.layout.kontakt_item,
                       R.id.kontakt_item_textview,
                       kontakte
               );
           kontakteListView.setAdapter(listAdapter);
    }

    @Override
    public void onAttach(Context context) {
        utils = (((MainActivity)getActivity()).getUtils());
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onStop() {
        super.onStop();
        kontakte = utils.getKontakte();
        if(kontakte.contains("keine Kontakte gefunden")){
            kontakte.remove("keine Kontakte gefunden");
        }

        SharedPreferences settings = getContext().getSharedPreferences("KontaktListe"+ utils.getUserName(), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("Kontakte", new HashSet<>(kontakte));

        editor.apply();
    }

    // Fügt einen neuen ChatPartner der Liste hinzu.
    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnNeuerChat)
    public void neuerChat() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bitte geben Sie die userId ein.");

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getUserInfo(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Überprüft ob der Kontakt vorhanden ist.
     *
     * Falls ja wird er der Kontaktliste hinzugefügt.
     * @param user Der user.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getUserInfo(final String user) {

        JSONObject js = new JSONObject();

        try {
            js.put("userToFind", user);
            js.put("authentication", utils.getUserPasswordAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/userInfo";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, usercreateURL, js, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        ((MainActivity)getActivity()).getUtils().addKontakt(user);
                        listAdapter.notifyDataSetChanged();


                    }
                }, new Response.ErrorListener() {

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Kontakt konnte nicht gefunden werden", Toast.LENGTH_SHORT).show();

                    }
                });

        HttpRequestManager.getInstance(getContext()).addToRequestQueue(jsObjRequest);
    }


    /**
     * Aktiviert den Deletemode.
     */
    @OnClick(R.id.btnDeleteKontakt)
    public void enableDelete(){
        isDeleteEnabled = true;
    }

    public void deleteKontakt(TextView v){
        String userName = v.getText().toString();

        kontakte.remove(userName);

        listAdapter.remove(userName);
    }
}
