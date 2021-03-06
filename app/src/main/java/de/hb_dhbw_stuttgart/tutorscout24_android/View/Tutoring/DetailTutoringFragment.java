package de.hb_dhbw_stuttgart.tutorscout24_android.View.Tutoring;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.MainActivity;
import de.hb_dhbw_stuttgart.tutorscout24_android.Logic.Utils;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;
import de.hb_dhbw_stuttgart.tutorscout24_android.View.Communication.ChatFragment;


/**
 * Created by Robert
 */

//Dieses Fragment ist eine Detailansicht eines Tutorings
@RequiresApi(api = Build.VERSION_CODES.M)
public class DetailTutoringFragment extends Fragment {

    private String userName;
    private String tutoringId;
    private String subject;
    private String description;
    private Double distance;
    private String creationDate;
    private String expirationDate;

    public DetailTutoringFragment() {
        // Required empty public constructor
    }

    //Die Daten des aufgerufenen Tutorings werden hier übergeben
    public void setParams(String userName, String tutoringId, String subject, String description, Double distance, String creationDate, String expirationDate) {
        this.userName = userName;
        this.tutoringId = tutoringId;
        this.subject = subject;
        this.description = description;
        this.distance = distance;
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
        View view = inflater.inflate(R.layout.fragment_detail_tutoring, container, false);
        ButterKnife.bind(this, view);

        TextView userTitle = view.findViewById(R.id.titleTxt);
        TextView subj = view.findViewById(R.id.subjectTxt);
        TextView id = view.findViewById(R.id.tutoringIdTxt);
        TextView descr = view.findViewById(R.id.descriptionTxt);
        TextView dist = view.findViewById(R.id.distanceTxt);
        TextView creDate = view.findViewById(R.id.creationDateTxt);
        TextView expDate = view.findViewById(R.id.expirationDateTxt);

        //Schreiben der Daten in die entsprechenden TextViews
        userTitle.setText(userName);
        subj.setText(subject);
        id.setText(tutoringId);
        descr.setText(description);
        String distanceString = String.format(Locale.getDefault(), "%.1f", distance) + "km";
        dist.setText(distanceString);
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

    //Beim Klicken des Kontaktieren-Buttons wird ein neuer Chat mit dem entsprechenden User angelegt und angezeigt
    @OnClick(R.id.btnContactUser)
    public void OnContactButtonClick() {
        Utils utils = (((MainActivity)getActivity()).getUtils());

       utils.addKontakt(userName);
       utils.chatUser = userName;

        ChatFragment chatFragment = new ChatFragment();
        ((MainActivity) getActivity()).changeFragment(chatFragment, "ChatFragment");
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
