package com.disarm.surakshit.pdm.Fragments;

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

import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.LatLonUtil;

import org.apache.commons.io.FileUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by naman on 25/2/18.
 */

public class MapFragment extends Fragment {
    MapView map;
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
                parseKml();
                h.postDelayed(this,5000);
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

    public void parseKml(){
        map.getOverlays().removeAll(allParsed);
        File latestSource = Environment.getExternalStoragePublicDirectory("DMS/KML/Source/LatestKml");
        File latestDest = Environment.getExternalStoragePublicDirectory("DMS/KML/Dest/LatestKml");
        if(latestSource.listFiles().length > 0 ){
            for(File f : latestSource.listFiles()){
                KmlDocument kmlDocument = new KmlDocument();
                kmlDocument.parseKMLFile(f);
                FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map,null,null,kmlDocument);
                map.getOverlays().add(kmlOverlay);
                allParsed.add(kmlOverlay);
            }
        }
        if(latestDest.listFiles().length > 0 ){
            for(File f : latestDest.listFiles()){
                KmlDocument kmlDocument = new KmlDocument();
                kmlDocument.parseKMLFile(f);
                FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map,null,null,kmlDocument);
                map.getOverlays().add(kmlOverlay);
                allParsed.add(kmlOverlay);
            }
        }
    }
}
