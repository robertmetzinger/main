package de.hb_dhbw_stuttgart.tutorscout24_android.Logic;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Created by Patrick Woehnl on 04.11.2017.
 */

/**
 * Diese Klasse Managed die Requests an das Backend
 *
 * Übernommen aus: https://developer.android.com/training/volley/requestqueue.html
 */
public class HttpRequestManager {
    @SuppressLint("StaticFieldLeak")
    private static HttpRequestManager instance;
    private RequestQueue requestQueue;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private HttpRequestManager(Context context) {
        HttpRequestManager.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized HttpRequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new HttpRequestManager(context);
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}