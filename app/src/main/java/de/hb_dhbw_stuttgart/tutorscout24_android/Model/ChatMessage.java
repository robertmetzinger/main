package de.hb_dhbw_stuttgart.tutorscout24_android.Model;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hb_dhbw_stuttgart.tutorscout24_android.Model.UserType;

/**
 * Created by patrick.woehnl on 26.11.2017.
 */
public class ChatMessage {

    private String messageText;
    private UserType userType;
    private String fromUserId;
    public String toUserId;
    private Date messageTime;
    private int messageId;




    public Date getMessageTime() {        return messageTime;
    }

    public void setMessageTime(Date messageTime) {
        this.messageTime = messageTime;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public int getMessageId() { return messageId; }

    public void sentMessageId(int messageId) {
        this.messageId = messageId;
    }




    public String getMessageText() {

        return messageText;
    }

    public UserType getUserType() {
        return userType;
    }

    public ChatMessage(int messageId, String messageText, UserType userType,Date messageTime, String fromUserId, String toUserId){
        this.messageId = messageId;
        this.messageText = messageText;
        this.userType = userType;
        this.fromUserId = fromUserId;
        this.messageTime = messageTime;
        this.toUserId = toUserId;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String toString(){
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        StringBuilder chatMessage = new StringBuilder();
        chatMessage.append("|" + System.lineSeparator());
        chatMessage.append("messageId~~:~~"+ getMessageId() + "~~#~~");
        chatMessage.append("datetime~~:~~"+ f.format(getMessageTime()) + "~~#~~");
        chatMessage.append("fromUserId~~:~~"+ getFromUserId() + "~~#~~");
        chatMessage.append("toUserId~~:~~"+ getToUserId() + "~~#~~");
        chatMessage.append("messageText~~:~~"+ getMessageText() + "~~#~~");
        chatMessage.append("userType~~:~~"+ getUserType().getFieldDescription() + "~~#~~");

        return chatMessage.toString();
    }
}