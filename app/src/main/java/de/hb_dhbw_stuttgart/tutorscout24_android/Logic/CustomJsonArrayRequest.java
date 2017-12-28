package de.hb_dhbw_stuttgart.tutorscout24_android.Logic;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Robert
 */

/**
 * Der CustomJsonArrayRequest.
 * <p>
 * Diese Klasse dient dazu, einen JSONObject Request zu machen und als Antwort ein JSONArray zu erhalten.
 * (JSONObject senden und JSONArray als Antwort erhalten)
 */
public class CustomJsonArrayRequest extends JsonRequest<JSONArray> {

    public CustomJsonArrayRequest(int method, String url, JSONObject jsonRequest,
                                  Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    /**
     * Parst die Antwort des Netzwerks (JSONObject -> JSONArray).
     *
     * @param response Die NetworkResponse.
     * @return Das JSONArray.
     */
    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONArray(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}