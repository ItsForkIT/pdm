package com.disarm.surakshit.pdm.Util;

import android.content.Context;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;


public class CustomInfoWindow extends InfoWindow {
    Polygon polygon;
    Context context;
    public CustomInfoWindow(int layoutResId, MapView mapView, Polygon polygon, Context context) {
        super(layoutResId, mapView);
        this.polygon = polygon;
        this.context = context;
    }

    @Override
    public void onOpen(Object item) {

    }

    @Override
    public void onClose() {

    }
}
