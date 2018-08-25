package com.disarm.surakshit.pdm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Util.LatLonUtil;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.util.Map;
import java.util.Objects;

public class GetLocationActivity extends AppCompatActivity {
    MapView map;
    final int MIN_ZOOM=14,MAX_ZOOM=19,PIXEL=256,RESULT_ERR=-1,RESULT_OK=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        map = (MapView) findViewById(R.id.GetLocationMap);
        setMapData();
        final Marker m = new Marker(map);
        Drawable iconDrawable = getResources().getDrawable(R.drawable.ic_place_green);
        m.setIcon(iconDrawable);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    m.setPosition(LatLonUtil.getBoundaryOfTiles());
                }
            });
            thread.start();
        }
        catch (Exception e){
            Toast.makeText(getBaseContext(),"Something not right!!! Can't take location",Toast.LENGTH_LONG).show();
            setResult(RESULT_ERR);
            finish();
        }
        MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(GetLocationActivity.this).
                setTitle(R.string.no_location).
                setDescription(R.string.no_location_des).
                withDialogAnimation(true).
                setPositiveText(R.string.okay).
                setStyle(Style.HEADER_WITH_TITLE).
                setHeaderColor(R.color.fbutton_color_turquoise)
                .build();
        dialog.show();
        m.setDraggable(true);
        m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                Intent data =  new Intent();
                data.putExtra("lat",marker.getPosition().getLatitude());
                data.putExtra("lon",marker.getPosition().getLongitude());
                setResult(RESULT_OK,data);
                finish();
                return false;
            }
        });
        map.getOverlays().add(m);
    }
    public void setMapData(){
        ITileSource tileSource = new XYTileSource("tiles",MIN_ZOOM,MAX_ZOOM,PIXEL,".png",new String[]{});
        map.setTileSource(tileSource);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        final IMapController mapController = map.getController();
        mapController.setZoom(15.0);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final GeoPoint startPoint = LatLonUtil.getBoundaryOfTiles();
                if(startPoint!=null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapController.setCenter(startPoint);
                        }
                    });

                }
            }
        });
        thread.start();
        CompassOverlay mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(width/2, 10);
        map.getOverlays().add(mScaleBarOverlay);
    }

}
