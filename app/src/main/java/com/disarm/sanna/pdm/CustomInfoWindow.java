package com.disarm.sanna.pdm;

import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

/**
 * Created by naman on 30/10/17.
 */

public class CustomInfoWindow extends InfoWindow {

    String title_str,latlon_str;
    /**
     * @param layoutResId the id of the view resource.
     * @param mapView     the mapview on which is hooked the view
     */
    public CustomInfoWindow(int layoutResId, MapView mapView,String title_str,String latlon_str) {
        super(layoutResId, mapView);
        this.title_str = title_str;
        this.latlon_str = latlon_str;
    }

    @Override
    public void onOpen(Object item) {
        Button open = (Button) mView.findViewById(R.id.btn_iw_open);
        TextView title = (TextView) mView.findViewById(R.id.tv_iw_title);
        TextView latlon = (TextView) mView.findViewById(R.id.tv_iw_latlon);
        title.setText(title_str);
        latlon.setText(latlon_str);

    }

    @Override
    public void onClose() {

    }
}
