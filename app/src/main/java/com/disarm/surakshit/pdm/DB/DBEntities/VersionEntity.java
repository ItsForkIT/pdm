package com.disarm.surakshit.pdm.DB.DBEntities;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class VersionEntity {
    @Id
    private long id;
    private long timeStamp;
    private int version;
    private boolean manual;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    @Override
    public String toString() {
        return timeStamp + " : " + version + " : " + manual;
    }
}
