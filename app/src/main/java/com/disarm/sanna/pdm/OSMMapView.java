package com.disarm.sanna.pdm;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import android.widget.ProgressBar;

public class OSMMapView extends AppCompatActivity {
    public static enum string {

        // tile sources
        mapnik, cyclemap, public_transport, base, topo, hills, cloudmade_small, cloudmade_standard, mapquest_osm, mapquest_aerial, bing,

        // overlays
        fiets_nl, base_nl, roads_nl,

        // other stuff
        unknown, format_distance_meters, format_distance_kilometers, format_distance_miles, format_distance_nautical_miles, format_distance_feet, online_mode, offline_mode, my_location, compass, map_mode,

    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_map_view);
        MapView map = (MapView) findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(14);
        GeoPoint startPoint = new GeoPoint(23.5500612,87.2912049);
        mapController.setCenter(startPoint);
        String[] s = {"http://127.0.0.1:8080/getTile/"};
        final ITileSource tileSource = new MyOSMTileSource(
                "DISARM MAP SOURCE", 1, 18, 256, ".png", s);
        map.setTileSource(tileSource);

    }
}
