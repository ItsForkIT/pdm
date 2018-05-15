package com.disarm.surakshit.pdm.Fragments;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.Receiver;
import com.disarm.surakshit.pdm.DB.DBEntities.Sender;
import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.LatLonUtil;

import org.apache.commons.io.FileUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.kml.StyleMap;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.objectbox.Box;

/**
 * Created by naman on 25/2/18.
 */

public class MapFragment extends Fragment {
    public static MapView map;
    final int MIN_ZOOM=14,MAX_ZOOM=19,PIXEL=256;
    ArrayList<Overlay> allParsed = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_map , container , false);
        map = (MapView) view.findViewById(R.id.fragment_mapView);
        setMapData();
        HandlerThread ht = new HandlerThread("Map");
        ht.start();
        final Handler h = new Handler(ht.getLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                parseKml(getActivity().getApplication());
            }
        });
        return view;
    }



    public void setMapData(){
        ITileSource tileSource = new XYTileSource("tiles",MIN_ZOOM,MAX_ZOOM,PIXEL,".png",new String[]{});
        map.setTileSource(tileSource);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Context ctx = getActivity().getApplicationContext();
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
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mapController.setCenter(startPoint);
                            }
                        });
                    }
                    catch (Exception e){

                    }

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

    public static void parseKml(Application app){
        try {
            final Box<Receiver> receiverBox = ((App) app).getBoxStore().boxFor(Receiver.class);
            final Box<Sender> senderBox = ((App) app).getBoxStore().boxFor(Sender.class);
            List<Receiver> receivers = receiverBox.getAll();
            List<Sender> senders = senderBox.getAll();
            map.getOverlays().clear();
            for(Receiver receiver : receivers){
                String kmlString = receiver.getKml();
                ByteArrayInputStream is = new ByteArrayInputStream(kmlString.getBytes());
                KmlDocument kml = new KmlDocument();
                kml.parseKMLStream(is,null);
                FolderOverlay overlay = (FolderOverlay) kml.mKmlRoot.buildOverlay(map,null,null,kml);
                map.getOverlays().add(overlay);
            }
            for(Sender sender : senders){
                String kmlString = sender.getKml();
                ByteArrayInputStream is = new ByteArrayInputStream(kmlString.getBytes());
                KmlDocument kml = new KmlDocument();
                kml.parseKMLStream(is,null);
                FolderOverlay overlay = (FolderOverlay) kml.mKmlRoot.buildOverlay(map,null,null,kml);
                map.getOverlays().add(overlay);
            }
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }
}
