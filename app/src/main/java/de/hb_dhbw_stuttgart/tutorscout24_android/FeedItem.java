package de.hb_dhbw_stuttgart.tutorscout24_android;

/**
 * Created by Robert on 27.11.2017.
 */

public class FeedItem {

    private String creator;
    private String subject;
    private String info;
    private String creationDate;

    public FeedItem (String creator, String subject, String info, String creationDate) {
        this.creator = creator;
        this.subject = subject;
        this.info = info;
        this.creationDate = creationDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getSubject() {
        return subject;
    }

    public String getInfo() {
        return info;
    }

    public String getCreationDate() {
        return creationDate;
    }
}
