package de.hb_dhbw_stuttgart.tutorscout24_android.Model.Communication;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
  Created by patrick.woehnl on 26.11.2017.
 */

/**
 * Die ChatMessage Klasse.
 * <p>
 * Beinhaltet alle Teile eine ChatNachricht.
 */
public class ChatMessage {

    private String messageText;
    private UserType userType;
    private String fromUserId;
    private String toUserId;
    private Date messageTime;
    private int messageId;

    public Date getMessageTime() {
        return messageTime;
    }

    private String getToUserId() {
        return toUserId;
    }

    private String getFromUserId() {
        return fromUserId;
    }

    public int getMessageId() {
        return messageId;
    }

    String getMessageText() {

        return messageText;
    }

    UserType getUserType() {
        return userType;
    }

    /**
     * Der Konstruktor.
     *
     * @param messageId   Die messageId.
     * @param messageText Der messageText.
     * @param userType    Der userType.
     * @param messageTime Die messageTime.
     * @param fromUserId  Die fromUserId.
     * @param toUserId    Die toUserId.
     */
    public ChatMessage(int messageId, String messageText, UserType userType, Date messageTime, String fromUserId, String toUserId) {
        this.messageId = messageId;
        this.messageText = messageText;
        this.userType = userType;
        this.fromUserId = fromUserId;
        this.messageTime = messageTime;
        this.toUserId = toUserId;
    }

    /**
     * Wandelt eine Nachricht zur Lokalen Speicherung in einen String um.
     *
     * @return Die ChatMessage als String.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public String toString() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.GERMANY);

        return ("|" + System.lineSeparator()) +
                "messageId~~:~~" + getMessageId() + "~~#~~" +
                "datetime~~:~~" + f.format(getMessageTime()) + "~~#~~" +
                "fromUserId~~:~~" + getFromUserId() + "~~#~~" +
                "toUserId~~:~~" + getToUserId() + "~~#~~" +
                "messageText~~:~~" + getMessageText() + "~~#~~" +
                "userType~~:~~" + getUserType().getFieldDescription() + "~~#~~";
    }
}