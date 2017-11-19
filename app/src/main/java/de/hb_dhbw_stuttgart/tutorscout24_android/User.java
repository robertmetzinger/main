package de.hb_dhbw_stuttgart.tutorscout24_android;

/**
 * Created by patrick.woehnl on 15.11.2017.
 */

public class User {

    public String userName;
    public String firstName;
    public String lastName;
    public int age;
    public String gender;
    public String email;
    public String note;
    public String placeOfResidence;
    public String maxGraduation;


    public void User(String userName, String firstName, String email) {

        this.userName = userName;
        this.firstName = firstName;
        this.email = email;
    }

}
