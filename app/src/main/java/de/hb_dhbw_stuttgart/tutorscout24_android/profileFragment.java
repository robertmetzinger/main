package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link profileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link profileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@TargetApi(23)
public class profileFragment extends android.app.Fragment  implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String USER_NAME = "Name";
    private static final String USER_MAIL = "E-Mail Adresse";

    String mParam1;
    String mParam2;

    GoogleApiClient mGoogleApiClient;
    private OnFragmentInteractionListener mListener;




    public profileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Parameter 1.
     * @param mail Parameter 2.
     * @return A new instance of fragment profileFragment.
     */
    public static profileFragment newInstance(String name, String mail) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        args.putString(USER_NAME, name);
        args.putString(USER_MAIL, mail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(USER_NAME);
            mParam2 = getArguments().getString(USER_MAIL);
        }


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

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
        super.onAttach(context);


        /*try {
            mListener = (OnArticleSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            TextView breite = (TextView) getView().findViewById(R.id.textBreite);
            TextView laenge = (TextView) getView().findViewById(R.id.textLaenge);
            breite.setText(String.valueOf(mLastLocation.getLatitude()));
            laenge.setText(String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnGetAdresse)
    public void gps() {

        Log.e("Aufruf", "gps: ");
        mGoogleApiClient.connect();
        String[] LOCATION_PERMS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(LOCATION_PERMS, 1);

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Falls keine Rechte zur erkennung des Standorts vorhanden sind, kann dieser nicht gefunden werden.
            return;
        }


        TextView breite = (TextView) getView().findViewById(R.id.textBreite);
        TextView laenge = (TextView) getView().findViewById(R.id.textLaenge);



        if (!breite.getText().toString().startsWith("B")){
            double breitengrad = Double.parseDouble(breite.getText().toString());
            double laengengrad = Double.parseDouble(laenge.getText().toString());

            Toast.makeText(
                    getContext(),
                    "Location changed: Lat: " + breitengrad + " Lng: "
                            + laengengrad, Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + laengengrad;


        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(breitengrad, laengengrad, 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            TextView t = (TextView)  getView().findViewById(R.id.City);

            t.setText(cityName);
        }
    }
}
