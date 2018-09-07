package com.disarm.surakshit.pdm.Util;

import android.content.Context;
import android.widget.ListView;
import android.widget.TextView;

import com.disarm.surakshit.pdm.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class MergedInfoWindow extends InfoWindow {
    private Polygon polygon;
    private Marker marker;
    private Context context;
    private TextView audioText;
    private TextView imageText;
    private TextView videoText;

    public MergedInfoWindow(int layoutResId, MapView mapView, Polygon polygon, Context context) {
        super(layoutResId, mapView);
        this.polygon = polygon;
        this.context = context;
    }

    public MergedInfoWindow(int layoutResId, MapView mapView, Marker marker, Context context) {
        super(layoutResId, mapView);
        this.marker = marker;
        this.context = context;
    }

    @Override
    public void onOpen(Object item) {
        audioText = mView.findViewById(R.id.miw_audio_count_text);
        videoText = mView.findViewById(R.id.miw_video_count_text);
        imageText = mView.findViewById(R.id.miw_image_count_text);
        String des;
        if(item instanceof Polygon)
            des = polygon.getSnippet();
        else
            des = marker.getSnippet();
        String mediaCount[] = des.split(";");
        audioText.setText(mediaCount[0]);
        imageText.setText(mediaCount[1]);
        videoText.setText(mediaCount[2]);
    }

    @Override
    public void onClose() {

    }
}
