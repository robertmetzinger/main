package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ChatFragment extends android.app.Fragment {

    private ListView chatListView;
    private EditText chatEditText1;
    private ArrayList<ChatMessage> chatMessages;
    private ImageView enterChatView1;
    private ChatListAdapter listAdapter;
    private  String chatPartner;
    private String chatMessageFileName;
    private boolean sendloadSuccess = false;
    private boolean recievedloadSuccess = false;



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


        setUser(((MainActivity)getActivity()).chatUser, view);

        // AndroidUtilities.statusBarHeight = getStatusBarHeight();

        chatEditText1 = view.findViewById(R.id.chat_edit_text1);
        enterChatView1 = view.findViewById(R.id.enter_chat1);

        chatMessages = new ArrayList<>();
        listAdapter = new ChatListAdapter(chatMessages, getContext());

        chatListView = view.findViewById(R.id.chat_list_view);

            chatListView.setAdapter(listAdapter);



        loadRecievedMessages();
        loadSentMessages();
        loadChatMessagesInFile();

        sortMessages();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
            // loadSentMessages();
           // sendMessageBackend("Dies ist eine neue Nachricht.", "PatrickAndroid2");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick(R.id.enter_chat1)
    public void sendMessage(){
        EditText editText = getView().findViewById(R.id.chat_edit_text1);
        sendMessageBackend(editText.getText().toString(), ((MainActivity)getActivity()).chatUser);
        sendMessageFrontend();
        editText.setText("");
    }

    private void sendMessageFrontend()
    {
        if(chatMessages == null){
            chatMessages = new ArrayList<>();
        }

        if(listAdapter!=null)
            listAdapter.notifyDataSetChanged();
    }

    public void setUser(String user, View view){
        if(user == null || view == null){
            return;
        }
        chatPartner = user;
        ((MainActivity)getActivity()).changeTitle(user);

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
                    loadSentMessages();
                   Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    Log.e("", "onResponse: " + response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                   if(error == null){
                       return;
                   }
                   try{
                       NetworkResponse response = error.networkResponse;

                       String json = new String(response.data);
                       json = trimMessage(json, "message");
                       Log.e("", "onErrorResponse: " + json );
                   }catch (NullPointerException e){
                       Toast.makeText(getContext(), "Fehler beim senden der Nachricht", Toast.LENGTH_LONG);
                   }


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
    private void loadSentMessages(){

        final JSONArray[] feedList = new JSONArray[1];
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/message/getSentMessages";

        //erstelle JSON Object für den Request

        CustomJsonArrayRequest a = new CustomJsonArrayRequest(Request.Method.POST, url, getAuthenticationJson(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {


                for(int i = 0; i < response.length(); i++){
                    try {
                        JSONObject o = (JSONObject) response.get(i);

                        boolean exists = false;
                        for (ChatMessage msgCompare :chatMessages) {
                            if (msgCompare.getMessageId() ==Integer.parseInt(o.getString("messageId")) ){
                                exists = true;
                                break;
                            }
                        }
                        if(exists){
                            continue;
                        }
                        String toUserId = o.getString("toUserId");

                        if(!toUserId.equals(chatPartner)){
                            continue;
                        }
                        String string_date = o.getString("datetime");

                        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        try {
                            Date d = f.parse(string_date);
                            chatMessages.add(new ChatMessage(Integer.parseInt(o.getString("messageId")), o.getString("text"),  UserType.SELF, d,MainActivity.getUserName(),toUserId ));
                            Log.e("messages", "stringToMessage: Messageload send: " + o.getString("messageId"));


                            // sendMessageFrontend(o.getString("text"), UserType.SELF, d);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                sendloadSuccess = true;
                sortMessages();
            }

        }, new Response.ErrorListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;

//                String json = new String(response.data);
         //       json = trimMessage(json, "message");
           //     Log.e("", "onErrorResponse: " + json );
//
  //              Log.e("Messages", "onErrorResponse:" + error.getMessage() );
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
        String url = "http://tutorscout24.vogel.codes:3000/tutorscout24/api/v1/message/getReceivedMessages";

        //erstelle JSON Object für den Request

        CustomJsonArrayRequest a = new CustomJsonArrayRequest(Request.Method.POST, url, getAuthenticationJson(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for(int i = 0; i < response.length(); i++) {



                        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        try {
                            JSONObject o = (JSONObject) response.get(i);

                            boolean exists = false;
                            for (ChatMessage msgCompare :chatMessages) {
                                if (msgCompare.getMessageId() == Integer.parseInt(o.getString("messageId")) ){
                                    exists = true;
                                    break;
                                }
                            }
                            if(exists){
                                continue;
                            }

                            String fromUserId = o.getString("fromUserId");

                            if(!fromUserId.equals(chatPartner)){
                                continue;
                            }
                            String string_date = o.getString("datetime");
                            Date d = f.parse(string_date);
                            chatMessages.add(new ChatMessage(Integer.parseInt(o.getString("messageId")), o.getString("text"),  UserType.OTHER, d, fromUserId, MainActivity.getUserName() ));
                            Log.e("messages", "stringToMessage: Messageload recieved: " + o.getString("messageId"));

                            //- sendMessageFrontend(o.getString("text"), UserType.OTHER, d);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();


                    }
                }
                recievedloadSuccess = true;
                sortMessages();
            }

        }, new Response.ErrorListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onErrorResponse(VolleyError error) {
               // String json = new String(error.networkResponse.data);
               // json = trimMessage(json, "message");
          //      Log.e("", "onErrorResponse: " + json );

             //   Log.e("Messages", "onErrorResponse:" + error.getMessage() );
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

    private void saveChatMessagesInFile() {

        if(chatMessages.isEmpty()){
            return;
        }
        chatMessageFileName = MainActivity.getUserName() + chatPartner + "TutorscoutChatMessages";

        StringBuilder messagesString = new StringBuilder();


        for (ChatMessage message : chatMessages) {
            messagesString.append(message.toString());
        }


        try {

            FileOutputStream fos = getContext().openFileOutput(chatMessageFileName, Context.MODE_PRIVATE);
            fos.write(messagesString.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadChatMessagesInFile() {

        chatMessageFileName = MainActivity.getUserName()+ chatPartner + "TutorscoutChatMessages";

        try {
            FileInputStream fis = getContext().openFileInput(chatMessageFileName);

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            Log.e("TAG", "loadChatMessagesInFile: "+ sb.toString() );
            fis.close();
            if(sb.toString().isEmpty()){
                return;
            }
            Toast.makeText(getContext(),"Lade Nachrichten", Toast.LENGTH_SHORT).show();
            stringToMessage(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stringToMessage(String messages){
        String[] stringMessageArray = messages.split(Pattern.quote("|"));

        for (String messageString : stringMessageArray) {
            if(messageString.isEmpty()){
                continue;
            }
            try {
                Map<String, String> messageMap = new HashMap<>();
                String[] pairs = messageString.split("~~#~~");
                for (int i=0;i<pairs.length;i++) {
                    String pair = pairs[i];
                    String[] keyValue = pair.split("~~:~~");
                    messageMap.put(keyValue[0], String.valueOf(keyValue[1]));
                }

                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String userTypeString = messageMap.get("userType");
            UserType userType;
            if(userTypeString.compareTo("SELFE") == 0){
                userType = UserType.SELF;
            }else{
                    userType = UserType.OTHER;
                }

                Date d = f.parse(messageMap.get("datetime"));
                chatMessages.add(new ChatMessage(Integer.parseInt(messageMap.get("messageId")), messageMap.get("messageText"), userType,d, messageMap.get("fromUserId"), messageMap.get("toUserId")));
                Log.e("messages", "stringToMessage: Messageload lokal: " + messageMap.get("messageId"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sortMessages(){
        if(sendloadSuccess && recievedloadSuccess){

// Sorting
            Collections.sort(chatMessages, new Comparator<ChatMessage>() {
                @Override
                public int compare(ChatMessage msg1, ChatMessage msg2)
                {

                    return  msg1.getMessageTime().compareTo(msg2.getMessageTime());
                }
            });

            saveChatMessagesInFile();
            sendMessageFrontend();
        }
    }
}
