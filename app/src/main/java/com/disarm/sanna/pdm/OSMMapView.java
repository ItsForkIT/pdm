package com.disarm.sanna.pdm;

import android.content.Context;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
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
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

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
    CompassOverlay mCompassOverlay;
    ScaleBarOverlay mScaleBarOverlay;
    RotationGestureOverlay mRotationGestureOverlay;
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
        mapController.setZoom(15);
        GeoPoint startPoint = new GeoPoint(23.5500612,87.2912049);
        mapController.setCenter(startPoint);
        String[] s = {"http://127.0.0.1:8080/getTile/"};
        final ITileSource tileSource = new MyOSMTileSource(
                "DISARM MAP SOURCE", 13, 16, 256, ".png", s);
        this.mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        this.mCompassOverlay.enableCompass();
        map.getOverlays().add(this.mCompassOverlay);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        mRotationGestureOverlay = new RotationGestureOverlay(ctx, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(this.mRotationGestureOverlay);
        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(width/2, 10);
        map.getOverlays().add(this.mScaleBarOverlay);

        map.setTileSource(tileSource);

    }
}
