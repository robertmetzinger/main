package de.hb_dhbw_stuttgart.tutorscout24_android.Model.Tutoring;

/**
 * Created by Robert on 27.11.2017.
 */

//Diese Klasse enthält alle Daten eines Tutorings, das im Feed angezeigt wird
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

    public FeedItem(String tutoringId, String creationDate, String userName, String subject, String text, String expirationDate, Double latitude, Double longitude, Double distanceKm) {
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

    //Getters

    public String getTutoringId() {
        return tutoringId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }
}
