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


/*
  Created by Patrick Woehnl on 26.11.2017.
 */

/**
 * Der ChatListAdapter.
 * <p>
 * Diese Klasse kümmert sich um das Management der Nachrichten und deren umwandlung in ChatBubbles.
 */
public class ChatListAdapter extends BaseAdapter {

    private ArrayList<ChatMessage> chatMessages;
    private Context context;
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EE HH:mm", Locale.GERMANY);


    /**
     * Der Konstruktor.
     *
     * @param chatMessages Die chatMessages.
     * @param context      Der context.
     */
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

    /**
     * Gibt die View zurück die zur Nachricht gehört.
     *
     * @param position    Die position.
     * @param convertView Die convertView.
     * @param parent      Der parent.
     * @return Die View.
     */
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        ChatMessage message = chatMessages.get(position);
        ViewHolderOther holder1;
        ViewHolderSelfe holder2;

        // Falls, dass Nachricht vom Typ Other
        if (message.getUserType() == UserType.OTHER) {
            if (convertView == null) {

                // Erzeugt einen Neuen Holder welcher die Besntandteile der Other Chat Bubble enthält.
                v = LayoutInflater.from(context).inflate(R.layout.chat_bubble_other, null, false);
                holder1 = new ViewHolderOther();
                holder1.messageTextView = v.findViewById(R.id.message_text);
                holder1.timeTextView = v.findViewById(R.id.time_text);
                v.setTag(holder1);
            } else {
                v = convertView;
                holder1 = (ViewHolderOther) v.getTag();

            }

            // Befüllen des Holders
            holder1.messageTextView.setText(message.getMessageText());
            holder1.timeTextView.setText(SIMPLE_DATE_FORMAT.format(message.getMessageTime()));

            //Fall, dass Nachricht vom Typ Selge
        } else if (message.getUserType() == UserType.SELF) {

            if (convertView == null) {

                // Erzeugt einen Neuen Holder welcher die Besntandteile der Other Chat Bubble enthält.
                v = LayoutInflater.from(context).inflate(R.layout.chat_bubble_selfe, null, false);
                holder2 = new ViewHolderSelfe();
                holder2.messageTextView = v.findViewById(R.id.message_text);
                holder2.timeTextView = v.findViewById(R.id.time_text);
                holder2.messageStatus = v.findViewById(R.id.user_reply_status);
                v.setTag(holder2);

            } else {
                v = convertView;
                holder2 = (ViewHolderSelfe) v.getTag();

            }

            // Befüllen des Holders
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

    /**
     * Der ViewHolderOther.
     */
    private class ViewHolderOther {
        private TextView messageTextView;
        private TextView timeTextView;
    }

    /**
     * Der ViewHolderSelfe.
     */
    private class ViewHolderSelfe {
        private ImageView messageStatus;
        private TextView messageTextView;
        private TextView timeTextView;
    }
}
