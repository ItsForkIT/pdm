package com.disarm.surakshit.pdm;

import android.content.Context;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver_;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender_;
import com.disarm.surakshit.pdm.Util.LatLonUtil;
import com.disarm.surakshit.pdm.Util.Params;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.objectbox.Box;

public class ShowMapDataActivity extends AppCompatActivity {
    MapView map;
    final int MIN_ZOOM=14,MAX_ZOOM=19,PIXEL=256;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_show_map_data);
        map = (MapView) findViewById(R.id.map_activity_show);
        ITileSource tileSource = new XYTileSource("tiles",MIN_ZOOM,MAX_ZOOM,PIXEL,".png",new String[]{});
        map.setTileSource(tileSource);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map.setBuiltInZoomControls(true);
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
        String uniqueId = getIntent().getStringExtra("uniqueId");
        String number = getIntent().getStringExtra("number");
        String who = getIntent().getStringExtra("who");
        String kml = "";
        if(who.equals("me")){
            Box<Sender> senderBox = ((App)getApplication()).getBoxStore().boxFor(Sender.class);
            List<Sender> senders = senderBox.query().contains(Sender_.number,number).build().find();
            Sender s = senders.get(0);
            kml = s.getKml();
        }
        else{
            Box<Receiver> receiverBox = ((App)getApplication()).getBoxStore().boxFor(Receiver.class);
            List<Receiver> receivers = receiverBox.query().contains(Receiver_.number,number).build().find();
            Receiver r = receivers.get(0);
            kml = r.getKml();
        }
        KmlDocument kmlDocument = new KmlDocument();
        ByteArrayInputStream is = new ByteArrayInputStream(kml.getBytes());
        kmlDocument.parseKMLStream(is,null);
        FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map,null,null,kmlDocument);
        List<Overlay> overlays = kmlOverlay.getItems();
        for(Overlay overlay : overlays){
                if( overlay instanceof Polygon){
                    if(((Polygon) overlay).getTitle().equals(uniqueId)){
                        map.getOverlays().add(overlay);
                        mapController.setCenter(((Polygon) overlay).getPoints().get(0));
                        break;
                    }
                }
                else if( overlay instanceof Marker){
                    if(((Marker) overlay).getTitle().equals(uniqueId)){
                        map.getOverlays().add(overlay);
                        mapController.setCenter(((Marker) overlay).getPosition());
                        break;
                    }
                }
        }
    }


}
