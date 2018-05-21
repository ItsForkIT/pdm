package com.disarm.surakshit.pdm.DB.DBEntities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by naman on 27/2/18.
 * Receiver is the other one
 */
@Entity
public class Receiver {
    @Id
    long id;
    String number;
    String kml;
    int totalMsg;
    int unread;
    String lastMessage;
    Boolean isVolunteer;
    Boolean isUser;
    Boolean lastUpdated;

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Boolean getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Boolean lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getTotalMsg() {
        return totalMsg;
    }

    public void setTotalMsg(int totalMsg) {
        this.totalMsg = totalMsg;
    }

    public Receiver(){

    }
    public Receiver(String number,String kml){
        this.number = number;
        this.kml = kml;
    }

    public Boolean getVolunteer() {
        return isVolunteer;
    }

    public void setVolunteer(Boolean volunteer) {
        isVolunteer = volunteer;
    }

    public Boolean getUser() {
        return isUser;
    }

    public void setUser(Boolean user) {
        isUser = user;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getKml() {
        return kml;
    }

    public void setKml(String kml) {
        this.kml = kml;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }
}
