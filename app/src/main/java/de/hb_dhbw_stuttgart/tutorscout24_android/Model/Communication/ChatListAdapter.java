package de.hb_dhbw_stuttgart.tutorscout24_android.Model.Communication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hb_dhbw_stuttgart.tutorscout24_android.R;

public class ChatListAdapter extends BaseAdapter {

    private ArrayList<ChatMessage> chatMessages;
    private Context context;
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EE HH:mm", Locale.GERMANY);


    public ChatListAdapter(ArrayList<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;
    }


    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        ChatMessage message = chatMessages.get(position);
        ViewHolder1 holder1;
        ViewHolder2 holder2;

        if (message.getUserType() == UserType.OTHER) {
            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user1_item, null, false);
                holder1 = new ViewHolder1();


                holder1.messageTextView = v.findViewById(R.id.message_text);
                holder1.timeTextView = v.findViewById(R.id.time_text);


                v.setTag(holder1);
            } else {
                v = convertView;
                holder1 = (ViewHolder1) v.getTag();

            }

            holder1.messageTextView.setText(message.getMessageText());

            holder1.timeTextView.setText(SIMPLE_DATE_FORMAT.format(message.getMessageTime()));


        } else if (message.getUserType() == UserType.SELF) {

            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user2_item, null, false);

                holder2 = new ViewHolder2();


                holder2.messageTextView = v.findViewById(R.id.message_text);
                holder2.timeTextView = v.findViewById(R.id.time_text);
                holder2.messageStatus = v.findViewById(R.id.user_reply_status);
                v.setTag(holder2);

            } else {
                v = convertView;
                holder2 = (ViewHolder2) v.getTag();

            }

            holder2.messageTextView.setText(message.getMessageText());
            holder2.timeTextView.setText(SIMPLE_DATE_FORMAT.format(message.getMessageTime()));

                holder2.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_double_tick));

            }

        return v;
    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getUserType().ordinal();
    }

    private class ViewHolder1 {
        private TextView messageTextView;
        private TextView timeTextView;


    }

    private class ViewHolder2 {
        private ImageView messageStatus;
        private TextView messageTextView;
        private TextView timeTextView;

    }
}