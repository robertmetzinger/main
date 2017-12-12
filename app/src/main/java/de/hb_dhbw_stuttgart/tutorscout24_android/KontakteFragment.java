package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;


public class KontakteFragment extends android.app.Fragment {

    public static final String PREFS_NAME = "KontaktListe";

    ArrayList<String> kontakte;
    ArrayAdapter<String> listAdapter;
    boolean isDeleteEnabled = false;

    public KontakteFragment() {
        // Required empty public constructor
    }

    public static KontakteFragment newInstance() {
        KontakteFragment fragment = new KontakteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        SharedPreferences settings = getContext().getSharedPreferences(PREFS_NAME, 0);
        kontakte = new ArrayList<String>();

        String[] defaultString = {"keine Kontakte gefunden"};
        Set<String> defaultSet = new HashSet<String>(Arrays.asList(defaultString));

        kontakte.addAll(settings.getStringSet("Kontakte", defaultSet));

        setKontakteList(view);



        return view;
    }

    private void setKontakteList(View view) {


        ListView kontakteListView = view.findViewById(R.id.chat_list_view);

        kontakteListView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TextView v = (TextView) view;

                if(isDeleteEnabled){
                    deleteKontakt(v);
                    return;
                }

                Toast.makeText(getContext(), v.getText(), Toast.LENGTH_SHORT).show();

                ChatFragment chatFragment= new ChatFragment();

                ((MainActivity)getActivity()).chatUser = v.getText().toString();


                ((MainActivity)getActivity()).ChangeFragment(chatFragment,"Chat");

            }
        });

       listAdapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.kontakt_item,
                        R.id.kontakt_item_textview,
                        kontakte
                );

        kontakteListView.setAdapter(listAdapter);
    }

    @Override
    public void onAttach(Context context) {
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

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        if(kontakte.contains("keine Kontakte gefunden")){
            kontakte.remove("keine Kontakte gefunden");
        }

        editor.putStringSet("Kontakte", new HashSet<String>(kontakte));

        // Commit the edits!
        editor.commit();
    }

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getUserInfo(final String user) {

        JSONObject js = new JSONObject();

        try {
            js.put("userToFind", user);
            js.put("authentication", getAuthenticationJsonb());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/user/userInfo";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, usercreateURL, js, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        kontakte.add(user);
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

    public JSONObject getAuthenticationJsonb() {
        JSONObject authentication = new JSONObject();
        try {
            authentication.put("userName", MainActivity.getUserName());
            authentication.put("password", MainActivity.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }

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
