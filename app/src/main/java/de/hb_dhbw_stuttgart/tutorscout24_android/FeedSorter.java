package de.hb_dhbw_stuttgart.tutorscout24_android;

import java.util.Comparator;

/**
 * Created by Robert on 26.12.2017.
 */

public class FeedSorter implements Comparator<FeedItem> {

    @Override
    public int compare(FeedItem o1, FeedItem o2) {
        return o1.getDistanceKm().compareTo(o2.getDistanceKm());
    }
}
