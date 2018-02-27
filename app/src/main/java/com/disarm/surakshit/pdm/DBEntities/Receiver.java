package com.disarm.surakshit.pdm.DBEntities;

import io.objectbox.annotation.Id;

/**
 * Created by naman on 27/2/18.
 */

public class Receiver {
    @Id
    long id;
    String number;
    String kml;
    int unread;

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public Receiver(){

    }
    public Receiver(String number,String kml, int unread){
        this.number = number;
        this.kml = kml;
        this.unread = unread;
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
}
