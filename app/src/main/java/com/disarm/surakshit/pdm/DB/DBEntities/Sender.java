package com.disarm.surakshit.pdm.DB.DBEntities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by naman on 27/2/18.
 * Sender is yourself
 */
@Entity
public class Sender {
    @Id
    long id;
    String number;
    String kml;
    String lastMessage;
    boolean lastUpdated, forUser, forVolunteer;
    public Sender(){

    }
    public Sender(String number,String kml){
        this.number = number;
        this.kml = kml;
    }
    public String getNumber() {
        return number;
    }

    public boolean isLastUpdated() {
        return lastUpdated;
    }

    public boolean isForUser() {
        return forUser;
    }

    public void setForUser(boolean forUser) {
        this.forUser = forUser;
    }

    public boolean isForVolunteer() {
        return forVolunteer;
    }

    public void setForVolunteer(boolean forVolunteer) {
        this.forVolunteer = forVolunteer;
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
}
