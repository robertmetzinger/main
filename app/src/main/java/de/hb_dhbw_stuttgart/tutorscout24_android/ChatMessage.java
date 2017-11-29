package de.hb_dhbw_stuttgart.tutorscout24_android;

import java.util.Date;

/**
 * Created by patrick.woehnl on 26.11.2017.
 */
public class ChatMessage {

    private String messageText;
    private UserType userType;
    public Date datetime;
    public String fromUserId;
    public String toUserId;
    private long messageTime;




    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }


    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }


    public String getMessageText() {

        return messageText;
    }

    public UserType getUserType() {
        return userType;
    }

}