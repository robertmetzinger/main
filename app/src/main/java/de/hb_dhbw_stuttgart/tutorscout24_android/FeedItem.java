package de.hb_dhbw_stuttgart.tutorscout24_android;

/**
 * Created by Robert on 27.11.2017.
 */

public class FeedItem {

    private String tutoringId;
    private String creationDate;
    private String userName;
    private String subject;
    private String text;
    private String expirationDate;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;

    FeedItem(String tutoringId, String creationDate, String userName, String subject, String text, String expirationDate, Double latitude, Double longitude, Double distanceKm) {
        this.tutoringId = tutoringId;
        this.creationDate = creationDate;
        this.userName = userName;
        this.subject = subject;
        this.text = text;
        this.expirationDate = expirationDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
    }

    String getTutoringId() {
        return tutoringId;
    }

    String getCreationDate() {
        return creationDate;
    }

    public String getUserName() {
        return userName;
    }

    String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    String getExpirationDate() {
        return expirationDate;
    }

    Double getLatitude() {
        return latitude;
    }

    Double getLongitude() {
        return longitude;
    }

    Double getDistanceKm() {
        return distanceKm;
    }
}
