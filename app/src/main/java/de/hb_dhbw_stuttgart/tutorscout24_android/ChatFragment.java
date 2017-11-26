package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends android.app.Fragment {

    private ListView chatListView;
    private EditText chatEditText1;
    private ArrayList<ChatMessage> chatMessages;
    private ImageView enterChatView1;
    private ChatListAdapter listAdapter;
    private SizeNotifierRelativeLayout sizeNotifierRelativeLayout;
    private int keyboardHeight;
    private boolean keyboardVisible;
    private WindowManager.LayoutParams windowLayoutParams;
    private OnFragmentInteractionListener mListener;

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

               chatMessages = new ArrayList<>();

        chatListView = (ListView) view.findViewById(R.id.chat_list_view);

        chatEditText1 = (EditText) view.findViewById(R.id.chat_edit_text1);
        enterChatView1 = (ImageView) view.findViewById(R.id.enter_chat1);

        chatEditText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



        listAdapter = new ChatListAdapter(chatMessages, getContext());

        chatListView.setAdapter(listAdapter);


        enterChatView1.setOnClickListener(clickListener);


        //sizeNotifierRelativeLayout = (SizeNotifierRelativeLayout) getView().findViewById(R.id.chat_layout);
        // sizeNotifierRelativeLayout.delegate = this;

        // NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);

        return view;
    }




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @OnClick(R.id.enter_chat1)
    public void sendesaage(){
        EditText editText = getView().findViewById(R.id.chat_edit_text1);
        sendMessage(editText.getText().toString(), UserType.OTHER);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {

    }

    private ImageView.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

                EditText chat_edit_text1 = getView().findViewById(R.id.chat_edit_text1);
                sendMessage(chat_edit_text1.getText().toString(), UserType.OTHER);


            chat_edit_text1.setText("");

        }
    };




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private void sendMessage(final String messageText, final UserType userType)
    {
        if(chatMessages == null){
            chatMessages = new ArrayList<>();
        }
        if(messageText.trim().length()==0)
            return;

        final ChatMessage message = new ChatMessage();
        message.setMessageStatus(Status.SENT);
        message.setMessageText(messageText);
        message.setUserType(userType);
        message.setMessageTime(new Date().getTime());
        chatMessages.add(message);

        if(listAdapter!=null)
            listAdapter.notifyDataSetChanged();

        // Mark message as delivered after one second

        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

        exec.schedule(new Runnable(){
            @Override
            public void run(){
                message.setMessageStatus(Status.DELIVERED);

                final ChatMessage message = new ChatMessage();
                message.setMessageStatus(Status.SENT);
                message.setMessageText(messageText);
                message.setUserType(UserType.SELF);
                message.setMessageTime(new Date().getTime());
                chatMessages.add(message);

               /* ((MainActivity)getActivity()).runOnUiThread(new Runnable() {
                    public void run() {
                        //listAdapter.notifyDataSetChanged();
                    }
                });*/


            }
        }, 1, TimeUnit.SECONDS);

    }
}
