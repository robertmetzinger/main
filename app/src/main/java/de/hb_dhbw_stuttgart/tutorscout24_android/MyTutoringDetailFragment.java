package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyTutoringDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyTutoringDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class MyTutoringDetailFragment extends Fragment {

    private LayoutInflater inflater;

    private String userName;
    private String tutoringId;
    private String subject;
    private String description;
    private String creationDate;
    private String expirationDate;

    private OnFragmentInteractionListener mListener;

    public MyTutoringDetailFragment() {
        // Required empty public constructor
    }

    public void setParams(String userName, String tutoringId, String subject, String description, String creationDate, String expirationDate) {
        this.userName = userName;
        this.tutoringId = tutoringId;
        this.subject = subject;
        this.description = description;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static MyTutoringDetailFragment newInstance() {
        MyTutoringDetailFragment fragment = new MyTutoringDetailFragment();
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
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_my_tutoring_detail, container, false);
        ButterKnife.bind(this,view);

        TextView userTitle = (TextView) view.findViewById(R.id.titleTxt2);
        TextView subj = (TextView) view.findViewById(R.id.subjectTxt2);
        TextView id = (TextView) view.findViewById(R.id.tutoringIdTxt2);
        TextView descr = (TextView) view.findViewById(R.id.descriptionTxt2);
        TextView creDate = (TextView) view.findViewById(R.id.creationDateTxt2);
        TextView expDate = (TextView) view.findViewById(R.id.expirationDateTxt2);

        userTitle.setText(userName);
        subj.setText(subject);
        id.setText(tutoringId);
        descr.setText(description);
        creDate.setText(formatDateString(creationDate));
        expDate.setText(formatDateString(expirationDate));

        return view;
    }

    private String formatDateString(String dateString){
        SimpleDateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat dateToString = new SimpleDateFormat("dd.MM.yyyy\nHH:mm");
        try {
            Date date = stringToDate.parse(dateString);
            dateString = dateToString.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

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

    public void deleteTutoring() {
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/tutoring/delete";

        //erstelle JSON Object für den Request
        final JSONObject requestBody= new JSONObject();
        try {
            requestBody.put("tutoringId", Integer.parseInt(tutoringId));
            requestBody.put("authentication", getAuthenticationJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.DELETE, url, requestBody,
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
                    }
                });
        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(request);
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
