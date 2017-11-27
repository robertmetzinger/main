package de.hb_dhbw_stuttgart.tutorscout24_android;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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
        holder.creationDateTextView = (TextView) feedItemView.findViewById(R.id.dateTxt);
        holder.profilePic = (ImageView) feedItemView.findViewById(R.id.profilePic);

        feedItemView.setTag(holder);

        holder.creatorTextView.setText(item.getCreator());
        holder.subjectTextView.setText(item.getSubject());
        holder.infoTextView.setText(item.getInfo());
        holder.creationDateTextView.setText(item.getCreationDate());
        holder.profilePic.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_account_circle_black_24dp, null));


        return feedItemView;
    }

    private class ViewHolderForFeedItem {
        public TextView creatorTextView;
        public TextView subjectTextView;
        public TextView infoTextView;
        public TextView creationDateTextView;
        public ImageView profilePic;
    }
}
