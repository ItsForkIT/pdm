package com.disarm.surakshit.pdm.DB.DBEntities;

import org.osmdroid.util.GeoPoint;

import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class MergedKMLEntity {
    @Id
    private long id;
    private int zoom;
    private String tileName;
    private int mergedVersion;
    private int type; // point / polygon
    private String kml;
    private boolean manual;

    public static final int INITIAL_VERSION = 0;

    public int getMergedVersion() {
        return mergedVersion;
    }

    public void setMergedVersion(int mergedVersion) {
        this.mergedVersion = mergedVersion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public static final int KMLOBJECT_TYPE_POLYGON = 0;
    public static final int KMLOBJECT_TYPE_MARKER = 1;

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public String getTileName() {
        return tileName;
    }

    public void setTileName(String tileName) {
        this.tileName = tileName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKml() {
        return kml;
    }

    public void setKml(String kml) {
        this.kml = kml;
    }

    @Override
    public String toString() {
        return tileName + "::" + mergedVersion;
    }
}
