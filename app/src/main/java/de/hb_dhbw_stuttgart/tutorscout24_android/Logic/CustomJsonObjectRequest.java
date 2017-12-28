package de.hb_dhbw_stuttgart.tutorscout24_android.Logic;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Robert on 04.12.2017.
 */

/**
 * Der CustomJsonObjectRequest.
 * <p>
 * Diese Klasse dient dazu, einen JSONObject Request zu machen und als Antwort einen String zu erhalten.
 * (JSONObject senden und String als Antwort erhalten)
 */
public class CustomJsonObjectRequest extends JsonObjectRequest {

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }


    /**
     * Parst die Antwort des Netzwerks (String -> JSONObject).
     *
     * @param response Die NetworkResponse.
     * @return Das JSONObject.
     */
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

        try {
            String json = new String(response.data, "UTF-8");

            if (json.length() != 0) {
                JSONObject jsResponse = new JSONObject().put("response", "success");
                return Response.success(jsResponse, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return super.parseNetworkResponse(response);
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException j) {
            return Response.error(new ParseError(j));
        }
    }
}