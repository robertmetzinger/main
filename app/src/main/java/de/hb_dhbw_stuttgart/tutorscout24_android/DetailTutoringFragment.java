package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailTutoringFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailTutoringFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class DetailTutoringFragment extends Fragment {

    private LayoutInflater inflater;

    private String userName;
    private String tutoringId;
    private String subject;
    private String description;
    private Double distance;
    private String creationDate;
    private String expirationDate;

    private OnFragmentInteractionListener mListener;

    public DetailTutoringFragment() {
        // Required empty public constructor
    }

    public void setParams(String userName, String tutoringId, String subject, String description, Double distance, String creationDate, String expirationDate) {
        this.userName = userName;
        this.tutoringId = tutoringId;
        this.subject = subject;
        this.description = description;
        this.distance = distance;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static DetailTutoringFragment newInstance(String param1, String param2) {
        DetailTutoringFragment fragment = new DetailTutoringFragment();
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
        View view = inflater.inflate(R.layout.fragment_detail_tutoring, container, false);
        ButterKnife.bind(this,view);

        TextView userTitle = (TextView) view.findViewById(R.id.titleTxt);
        TextView subj = (TextView) view.findViewById(R.id.subjectTxt);
        TextView id = (TextView) view.findViewById(R.id.tutoringIdTxt);
        TextView descr = (TextView) view.findViewById(R.id.descriptionTxt);
        TextView dist = (TextView) view.findViewById(R.id.distanceTxt);
        TextView creDate = (TextView) view.findViewById(R.id.creationDateTxt);
        TextView expDate = (TextView) view.findViewById(R.id.expirationDateTxt);

        userTitle.setText(userName);
        subj.setText(subject);
        id.setText(tutoringId);
        descr.setText(description);
        dist.setText(String.format("%.1f", distance) + " km");
        creDate.setText(formatDateString(creationDate));
        expDate.setText(formatDateString(expirationDate));

        return view;
    }

    private String formatDateString(String dateString){
        SimpleDateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat dateToString = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            Date date = stringToDate.parse(dateString);
            dateString = dateToString.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    @OnClick(R.id.btnContactUser)
    public void OnContactButtonClick() {
        ((MainActivity)getActivity()).addKontakt(userName);
        ((MainActivity)getActivity()).chatUser = userName;

        ChatFragment chatFragment = new ChatFragment();
        ((MainActivity)getActivity()).changeFragment(chatFragment, "ChatFragment");
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
