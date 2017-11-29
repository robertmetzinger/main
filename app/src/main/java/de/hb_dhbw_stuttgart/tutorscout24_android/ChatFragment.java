package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ChatFragment extends android.app.Fragment {

    private ListView chatListView;
    private EditText chatEditText1;
    private ArrayList<ChatMessage> chatMessages;
    private ImageView enterChatView1;
    private ChatListAdapter listAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);


        // AndroidUtilities.statusBarHeight = getStatusBarHeight();

        chatEditText1 = view.findViewById(R.id.chat_edit_text1);
        enterChatView1 = view.findViewById(R.id.enter_chat1);

        chatMessages = new ArrayList<>();
        listAdapter = new ChatListAdapter(chatMessages, getContext());

        chatListView = view.findViewById(R.id.chat_list_view);

            chatListView.setAdapter(listAdapter);



        loadMessages();

        enterChatView1.setOnClickListener(clickListener);

        setUser(((MainActivity)getActivity()).chatUser, view);

        return view;
    }




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

            // loadMessages();

           // sendMessageBackend("nix", "PatrickAndroid2");

    }



    @Override
    public void onDetach() {
        super.onDetach();
    }

    private ImageView.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

                EditText chat_edit_text1 = getView().findViewById(R.id.chat_edit_text1);
                sendMessageFrontend(chat_edit_text1.getText().toString(), UserType.OTHER, new Date().getTime());

            chat_edit_text1.setText("");

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick(R.id.enter_chat1)
    public void sendMessage(){
        EditText editText = getView().findViewById(R.id.chat_edit_text1);
        sendMessageFrontend(editText.getText().toString(), UserType.OTHER, new Date().getTime());
        sendMessageBackend(editText.getText().toString(), ((MainActivity)getActivity()).chatUser);
    }

    private void sendMessageFrontend(final String messageText, final UserType userType, final long datetime)
    {
        if(chatMessages == null){
            chatMessages = new ArrayList<>();
        }
        if(messageText.trim().length()==0)
            return;

        final ChatMessage message = new ChatMessage();
        message.setMessageText(messageText);
        message.setUserType(userType);
        message.setMessageTime(datetime);
        chatMessages.add(message);

        if(listAdapter!=null)
            listAdapter.notifyDataSetChanged();

        // Mark message as delivered after one second

        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

        exec.schedule(new Runnable(){
            @Override
            public void run(){

                final ChatMessage message = new ChatMessage();
                message.setMessageText(messageText);
                message.setUserType(UserType.SELF);
                message.setMessageTime(datetime);
                chatMessages.add(message);

                ((MainActivity)getActivity()).runOnUiThread(new Runnable() {
                    public void run() {
                        listAdapter.notifyDataSetChanged();
                    }
                });


            }
        }, 1, TimeUnit.SECONDS);

    }

    public void setUser(String user, View view){
        TextView textView = view.findViewById(R.id.txtuserName);
        textView.setText(user);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendMessageBackend(String mssg, String toUserId) {

            String usercreateURL = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/message/sendMessage";

            JSONObject message = new JSONObject();

        try {
            message.put("toUserId", toUserId);
            message.put("text", mssg);
            message.put("authentication" , getAuthenticationJsonb());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = message.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, usercreateURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    Log.e("", "onResponse: " + response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                   if(error == null){
                       return;
                   }
                    NetworkResponse response = error.networkResponse;

                    String json = new String(response.data);
                    json = trimMessage(json, "message");
                    Log.e("", "onErrorResponse: " + json );

                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            HttpRequestManager.getInstance(getContext()).addToRequestQueue(stringRequest);
        }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadMessages(){

        final JSONArray[] feedList = new JSONArray[1];
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/message/getSentMessages";

        //erstelle JSON Object für den Request




        CustomJsonArrayRequest a = new CustomJsonArrayRequest(Request.Method.POST, url, getAuthenticationJson(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for(int i = 0; i < response.length(); i++){
                    try {
                        JSONObject o = (JSONObject) response.get(i);

                        sendMessageFrontend(o.getString("text"), UserType.OTHER, new Date().getTime());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        }, new Response.ErrorListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;

                String json = new String(response.data);
                json = trimMessage(json, "message");
                Log.e("", "onErrorResponse: " + json );

                Log.e("Messages", "onErrorResponse:" + error.getMessage() );
            }
        });


        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(a);
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadRecievedMessages(){

        final JSONArray[] feedList = new JSONArray[1];
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/messages/getReceivedMessages";

        //erstelle JSON Object für den Request

        CustomJsonArrayRequest a = new CustomJsonArrayRequest(Request.Method.POST, url, getAuthenticationJson(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Toast.makeText(getContext(), "Response: load success", Toast.LENGTH_SHORT).show();

            }

        }, new Response.ErrorListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = new String(error.networkResponse.data);
                json = trimMessage(json, "message");
                Log.e("", "onErrorResponse: " + json );

                Log.e("Messages", "onErrorResponse:" + error.getMessage() );
            }
        });


        // Access the RequestQueue through your singleton class.
        HttpRequestManager.getInstance(getContext()).addToRequestQueue(a);
    }
    public JSONObject getAuthenticationJson() {
        JSONObject authentication = new JSONObject();
        JSONObject aut = new JSONObject();
        try {
            authentication.put("userName", MainActivity.getUserName());
            authentication.put("password", MainActivity.getPassword());
            aut.put("authentication", authentication);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return aut;
    }


    public JSONObject getAuthenticationJsonb() {
        JSONObject authentication = new JSONObject();
        try {
            authentication.put("userName", MainActivity.getUserName());
            authentication.put("password", MainActivity.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return authentication;
    }
}
