package com.disarm.surakshit.pdm.Merging.MergeUtil;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class KmlObject {
    private int zoom;
    private String tileName;
    private int type; // point / polygon
    private List<GeoPoint> points;
    private String message;
    private String source;
    private String tag;


    public static final int KMLOBJECT_TYPE_POLYGON = 0;
    public static final int KMLOBJECT_TYPE_MARKER = 1;

    public KmlObject() {
    }

    public KmlObject(int zoom, int type, List<GeoPoint> points, String message, String source, String tileName) {
        this.zoom = zoom;
        this.type = type;
        this.points = points;
        this.message = message;
        this.source = source;
        this.tileName = tileName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return tileName + ":" + message;
    }

    public int getZoom() {
        return zoom;
    }

    public String getTileName() {
        return tileName;
    }

    public void setTileName(String tileName) {
        this.tileName = tileName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(List<GeoPoint> points) {
        this.points = points;
    }

}
