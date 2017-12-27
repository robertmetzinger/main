package de.hb_dhbw_stuttgart.tutorscout24_android.Model.Communication;

import com.google.gson.annotations.SerializedName;


public class User {

    @SerializedName("userName")
    public String userName;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("age")
    public int age;

    @SerializedName("gender")
    public String gender;

    @SerializedName("email")
    public String email;

    @SerializedName("note")
    public String note;

    @SerializedName("placeOfResidence")
    public String placeOfResidence;

    @SerializedName("maxGraduation")
    public String maxGraduation;

    public User() {}

    public User(String userName, String firstName, String lastName, int age, String gender, String email, String note, String placeOfResidence, String maxGraduation) {

        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.note = note;
        this.placeOfResidence = placeOfResidence;
        this.maxGraduation = maxGraduation;
    }

}
