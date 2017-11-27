package de.hb_dhbw_stuttgart.tutorscout24_android;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

/**
 * Created by Robert on 27.11.2017.
 */

public class CustomJsonArrayRequest extends JsonArrayRequest {
    public CustomJsonArrayRequest(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }
}
