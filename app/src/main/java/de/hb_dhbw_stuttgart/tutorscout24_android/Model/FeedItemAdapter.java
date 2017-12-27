package de.hb_dhbw_stuttgart.tutorscout24_android.Model;

import android.content.Context;
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
import java.util.Locale;

import de.hb_dhbw_stuttgart.tutorscout24_android.Model.FeedItem;
import de.hb_dhbw_stuttgart.tutorscout24_android.R;

/**
 * Created by Robert on 27.11.2017.
 */

//Dieser Adapter ermöglicht das Anzeigen von Tutorings in einer ListView
public class FeedItemAdapter extends BaseAdapter {

    private ArrayList<FeedItem> feed;
    private Context context;

    public FeedItemAdapter(ArrayList<FeedItem> feed, Context context) {
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

    //erzeugt eine View, die das Tutoring im Feed darstellt. Hierfür wird das ViewHolder Pattern verwendet
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FeedItem item = feed.get(position);
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.feed_item_fancy, parent, false);
        ViewHolderForFeedItem holder = new ViewHolderForFeedItem();

        holder.creatorTextView = convertView.findViewById(R.id.creatorTxt);
        holder.subjectTextView = convertView.findViewById(R.id.subjectTxt);
        holder.infoTextView = convertView.findViewById(R.id.infoTxt);
        holder.distanceTextView = convertView.findViewById(R.id.distanceTxt);
        holder.creationDateTextView = convertView.findViewById(R.id.creationDateTxt);
        holder.expirationDateTextView = convertView.findViewById(R.id.expirationDateTxt);
        holder.profilePic = convertView.findViewById(R.id.profilePic);

        convertView.setTag(holder);

        holder.creatorTextView.setText(item.getUserName());
        holder.subjectTextView.setText(item.getSubject());
        holder.infoTextView.setText(item.getText());
        holder.creationDateTextView.setText(formatDateString(item.getCreationDate()));
        holder.expirationDateTextView.setText(formatDateString(item.getExpirationDate()));
        if (item.getDistanceKm() != null) {
            String distanceString = String.format(Locale.getDefault(), "%.1f", item.getDistanceKm()) + " km";
            holder.distanceTextView.setText(distanceString);
        }
        return convertView;
    }

    //Methode zum Umformatieren der Datumangaben für eine nutzerfreundliche Darstellung
    private String formatDateString(String dateString) {
        SimpleDateFormat stringToDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat dateToString = new SimpleDateFormat("dd.MM.yyyy\nHH:mm", Locale.getDefault());
        try {
            Date date = stringToDate.parse(dateString);
            dateString = dateToString.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    //ViewHolder für das Tutoring
    private class ViewHolderForFeedItem{
        TextView creatorTextView;
        TextView subjectTextView;
        TextView infoTextView;
        TextView distanceTextView;
        TextView creationDateTextView;
        TextView expirationDateTextView;
        ImageView profilePic;
    }

}
