package de.hb_dhbw_stuttgart.tutorscout24_android.Logic;

import java.util.Comparator;

import de.hb_dhbw_stuttgart.tutorscout24_android.Model.FeedItem;

/**
 * Created by Robert on 26.12.2017.
 */

//Klasse zum Sortieren der Tutorings nach aufsteigender Entfernung
public class FeedSorter implements Comparator<FeedItem> {

    @Override
    public int compare(FeedItem o1, FeedItem o2) {
        return o1.getDistanceKm().compareTo(o2.getDistanceKm());
    }
}
