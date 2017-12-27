package de.hb_dhbw_stuttgart.tutorscout24_android.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.hb_dhbw_stuttgart.tutorscout24_android.R;

/**
 * Das Blank Fragment.
 * Dient als Container für die anderen Fragments.
 */
public class BlankFragment extends android.app.Fragment {


    public BlankFragment() {
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
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }
}
