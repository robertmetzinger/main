package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Robert on 27.11.2017.
 */

public class feedItemAdapter extends BaseAdapter {

    private ArrayList<FeedItem> feed;
    private Context context;

    public feedItemAdapter(ArrayList<FeedItem> feed, Context context) {
        this.feed = feed;
        this.context = context;
    }

    @Override
    public int getCount() {
        return feed.size();
    }

    @Override
    public Object getItem(int position) {
        return feed.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedItem item = feed.get(position);
        View feedItemView = LayoutInflater.from(context).inflate(R.layout.feed_item_fancy, null, false);
        ViewHolderForFeedItem holder = new ViewHolderForFeedItem();

        holder.creatorTextView = (TextView) feedItemView.findViewById(R.id.creatorTxt);
        holder.subjectTextView = (TextView) feedItemView.findViewById(R.id.subjectTxt);
        holder.infoTextView = (TextView) feedItemView.findViewById(R.id.infoTxt);
        holder.creationDateTextView = (TextView) feedItemView.findViewById(R.id.creationDateTxt);
        holder.expirationDateTextView = (TextView) feedItemView.findViewById(R.id.expirationDateTxt);
        holder.profilePic = (ImageView) feedItemView.findViewById(R.id.profilePic);

        feedItemView.setTag(holder);

        holder.creatorTextView.setText(item.getUserName());
        holder.subjectTextView.setText(item.getSubject());
        holder.infoTextView.setText(item.getText());
        holder.creationDateTextView.setText(formatDateString(item.getCreationDate()));
        holder.expirationDateTextView.setText(formatDateString(item.getExpirationDate()));
//        holder.profilePic.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.default_user_pic, null));


        return feedItemView;
    }

    private String formatDateString(String dateString){
        SimpleDateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat dateToString = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            Date date = stringToDate.parse(dateString);
            dateString = dateToString.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    private class ViewHolderForFeedItem {
        public TextView creatorTextView;
        public TextView subjectTextView;
        public TextView infoTextView;
        public TextView creationDateTextView;
        public TextView expirationDateTextView;
        public ImageView profilePic;
    }
}
