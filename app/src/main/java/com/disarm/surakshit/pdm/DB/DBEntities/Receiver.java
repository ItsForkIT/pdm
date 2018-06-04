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
    boolean forVolunteer;
    boolean forUser;
    boolean lastUpdated;

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(boolean lastUpdated) {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isForVolunteer() {
        return forVolunteer;
    }

    public void setForVolunteer(boolean forVolunteer) {
        this.forVolunteer = forVolunteer;
    }

    public boolean isForUser() {
        return forUser;
    }

    public void setForUser(boolean forUser) {
        this.forUser = forUser;
    }

    public boolean isLastUpdated() {
        return lastUpdated;
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
