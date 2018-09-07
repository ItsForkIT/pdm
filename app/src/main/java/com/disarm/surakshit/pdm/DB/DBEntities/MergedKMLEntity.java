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
    private String title;
    private String tileName;
    private int mergedVersion;
    private int type; // point / polygon
    private String kml;
    private int audioCount;
    private int videoCount;
    private int imageCount;

    public static final int INITIAL_VERSION = 1;

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

    public int getAudioCount() {
        return audioCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAudioCount(int audioCount) {
        this.audioCount = audioCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    @Override
    public String toString() {
        return tileName + "::" + mergedVersion;
    }
}
