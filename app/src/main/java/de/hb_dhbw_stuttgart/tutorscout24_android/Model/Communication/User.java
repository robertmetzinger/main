package de.hb_dhbw_stuttgart.tutorscout24_android.Model.Communication;

import com.google.gson.annotations.SerializedName;


/*
  Created by patrick.woehnl on 15.11.2017.
 */

/**
 * Die User Klasse.
 * <p>
 * Beiinhaltet alle Bestandtteile des Users.
 */
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

    /**
     * Der Empty Konstruktor
     */
    public User() {
    }

    /**
     * Der Konstuktor.
     * Wird aktuell noch nicht benötigt.
     *
     * @param userName         Der userName.
     * @param firstName        Der firstName.
     * @param lastName         Der lastName
     * @param age              Das age.
     * @param gender           Das gender.
     * @param email            Die email.
     * @param note             Die note.
     * @param placeOfResidence Der placeOfResidence.
     * @param maxGraduation    Die maxGraduation.
     */
    @SuppressWarnings("unused")
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
