package de.hb_dhbw_stuttgart.tutorscout24_android.Model;

/**
 * Created by patrick.woehnl on 26.11.2017.
 */

public enum UserType {
    OTHER("OTHER"), SELF("SELFE");

    private final String fieldDescription;

    private UserType(String value) {
        fieldDescription = value;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }
};
