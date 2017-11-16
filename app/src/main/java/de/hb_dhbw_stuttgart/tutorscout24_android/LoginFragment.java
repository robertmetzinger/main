package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.crypto.KeyGenerator;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends android.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_login, container, false);

        ButterKnife.bind(this, view);
        return  view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       /*
 * Load the Android KeyStore instance using the the
 * "AndroidKeyStore" provider to list out what entries are
 * currently stored.
 */
        try {
            KeyStore ks = KeyStore.getInstance("Tutorscout24");
            ks.load(null);
            Enumeration<String> aliases = ks.aliases();
            EditText passwortField = getView().findViewById(R.id.txtPasswort);

            char[] password = passwortField.getText().toString().toCharArray();

            FileInputStream fis = new FileInputStream("keyStoreName");
            ks.load(fis, password);

            passwortField.setText(password.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

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
    @OnClick(R.id.btnLogin)
    public void  Login(){

        BottomNavigationView nav = getActivity().findViewById(R.id.navigation);
        nav.setEnabled(true);

        nav.getMenu().getItem(0).setEnabled(true);
        nav.getMenu().getItem(1).setEnabled(true);
        nav.getMenu().getItem(2).setEnabled(true);
        nav.getMenu().getItem(3).setEnabled(true);
        nav.getMenu().getItem(4).setEnabled(true);

        nav.setVisibility(View.GONE);
        EditText passwortField = getView().findViewById(R.id.txtPasswort);

        // get user password and file input stream
        char[] password = passwortField.getText().toString().toCharArray();

        try {

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            //try (FileInputStream fis = new FileInputStream("Tutorscout24")) {
                ks.load(null, password);
           // }


            // get user password and file input stream



            KeyStore.ProtectionParameter protParam =
                    new KeyStore.PasswordProtection("Tutorscout24".toCharArray());

            ks.getEntry("Tutorscout24", protParam);

            // save my secret key
            javax.crypto.SecretKey mySecretKey = KeyGenerator.getInstance("AES").generateKey();;
            KeyStore.SecretKeyEntry skEntry =
                    new KeyStore.SecretKeyEntry(mySecretKey);
            ks.setEntry("Tutorscout24", skEntry, protParam);

            // store away the keystore
           try (FileOutputStream fos = new FileOutputStream("Tutorscout24")) {
                ks.store(fos, password);
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }




    }

    @OnClick(R.id.btnKeyTest)
    public void accessKeyStore(){
        try {
            KeyStore ks = KeyStore.getInstance("Tutorscout24");
            ks.load(null);
            Enumeration<String> aliases = ks.aliases();
            TextView passwortField = getView().findViewById(R.id.txtKeyTest);

            char[] password = passwortField.getText().toString().toCharArray();

            FileInputStream fis = new FileInputStream("Tutorscout24");
            ks.load(fis, password);

            passwortField.setText(password.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }
}
