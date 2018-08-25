package com.disarm.surakshit.pdm.Fragments;


import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.MergedKMLEntity;
import com.disarm.surakshit.pdm.DB.DBEntities.MergedKMLEntity_;
import com.disarm.surakshit.pdm.Merging.GISMerger;
import com.disarm.surakshit.pdm.Merging.MergeConstants;
import com.disarm.surakshit.pdm.Merging.MergeUtil.KmlObject;
import com.disarm.surakshit.pdm.Merging.MergeUtil.MergeDecisionPolicy;
import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.LatLonUtil;
import com.disarm.surakshit.pdm.location.MLocation;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

/**
 * A simple {@link Fragment} subclass.
 */
public class MergedMapFragment extends Fragment {
    public MapView map;
    public static Marker marker;
    final int MIN_ZOOM = 14, MAX_ZOOM = 19, PIXEL = 256;
    public static List<Overlay> allPlotted = new ArrayList<>();
    public FloatingActionButton mergeButton;
    Application app;
    int currentDBVersion;
    private Drawable iconDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merged_map, container, false);
        map = view.findViewById(R.id.fragment_merged_mapView);
        mergeButton = view.findViewById(R.id.fragment_merged_fab);
        iconDrawable = getResources().getDrawable(R.drawable.ic_place_green);
        setMapData();
        HandlerThread ht = new HandlerThread("Map");
        ht.start();
        final Handler locHandler = new Handler(ht.getLooper());
        locHandler.post(new Runnable() {
            @Override
            public void run() {
                Location l = MLocation.getLocation(getContext().getApplicationContext());
                if (l == null) {
                    locHandler.postDelayed(this, 1000);
                } else {
                    GeoPoint g = new GeoPoint(l.getLatitude(), l.getLongitude());
                    if (marker == null) {
                        marker = new Marker(map);
                        marker.setIcon(iconDrawable);
                        marker.setPosition(g);
                        marker.setSnippet("You are here");
                        map.getOverlays().add(marker);
                    } else {
                        map.getOverlays().remove(marker);
                        marker.setIcon(iconDrawable);
                        marker.setPosition(g);
                        map.getOverlays().add(marker);
                    }
                }
            }
        });
        setFab();
//        showMergedFiles(getContext());
        app = getActivity().getApplication();
        showMergedFilesDB(app);
        return view;
    }

    private void showMergedFilesDB(Application app) {
        Box<MergedKMLEntity> mergedKMLEntityBox = ((App) app).getBoxStore().boxFor(MergedKMLEntity.class);
        long count = mergedKMLEntityBox.count();
        if (count == 0)
            return;
        MergedKMLEntity kmlEntity = mergedKMLEntityBox.get(count);
        currentDBVersion = kmlEntity.getMergedVersion();
        List<MergedKMLEntity> kmlEntities = mergedKMLEntityBox.query().equal(MergedKMLEntity_.mergedVersion, currentDBVersion).build().find();
        for (MergedKMLEntity entity :kmlEntities)
            Log.d("MergedFiles", entity.toString());
        for (MergedKMLEntity entity : kmlEntities) {
            Log.d("MergedEntity", "Info:" + entity.getId() + " " + entity.getTileName());
            String kmlString = entity.getKml();
            ByteArrayInputStream is = new ByteArrayInputStream(kmlString.getBytes());
            KmlDocument kml = new KmlDocument();
            kml.parseKMLStream(is, null);
            FolderOverlay folderOverlay = (FolderOverlay) kml.mKmlRoot.buildOverlay(map, null, null, kml);
            for (Overlay overlay : folderOverlay.getItems()) {
                if (overlay instanceof Polygon) {
                    ((Polygon) overlay).setStrokeColor(Color.parseColor("#CDE74C3C"));
                    map.getOverlays().add(overlay);
                    allPlotted.add(overlay);
                }
            }
        }
        mergedKMLEntityBox.closeThreadResources();
    }

    private void showMergedFiles(Context context) {
        File mergedFile = Environment.getExternalStoragePublicDirectory(MergeConstants.DMS_MERGED_KML);
        File allFiles[] = mergedFile.listFiles();
        for (File file : allFiles) {
            KmlDocument kml = new KmlDocument();
            kml.parseKMLFile(file);
            FolderOverlay kmlOverlay = (FolderOverlay) kml.mKmlRoot.buildOverlay(map, null, null, kml);
            for (Overlay overlay : kmlOverlay.getItems()) {
                if (overlay instanceof Polygon) {
                    ((Polygon) overlay).setStrokeColor(R.color.green);
                    map.getOverlays().add(overlay);
                    allPlotted.add(overlay);
                } else if (overlay instanceof Marker) {
                    Drawable d = context.getDrawable(R.drawable.marker_blue);
                    ((Marker) overlay).setImage(d);
                    map.getOverlays().add(overlay);
                    allPlotted.add(overlay);
                }
            }
        }
    }

    private void setFab() {
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                call get kml objects
                MergeDecisionPolicy decisionPolicy = new MergeDecisionPolicy(MergeDecisionPolicy.DISTANCE_THRESHOLD_POLICY, 50, 0);
                GISMerger.mergeGIS(getActivity().getApplication(), map, decisionPolicy, true);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeOverlay();
                        allPlotted.clear();
                        showMergedFilesDB(app);
                    }
                }, 2000);
            }
        });
    }

    private void removeOverlay() {
        for (Overlay overlay : map.getOverlays()) {
            if (!(overlay instanceof MapEventsOverlay)) {
                map.getOverlays().remove(overlay);
            }
        }
    }

    public void setMapData() {
        ITileSource tileSource = new XYTileSource("tiles", MIN_ZOOM, MAX_ZOOM, PIXEL, ".png", new String[]{});
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
        mapController.setZoom(16.0);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final GeoPoint startPoint = LatLonUtil.getBoundaryOfTiles();
                if (startPoint != null) {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mapController.setCenter(startPoint);
                            }
                        });
                    } catch (Exception e) {

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
        mScaleBarOverlay.setScaleBarOffset(width / 2, 10);
        map.getOverlays().add(mScaleBarOverlay);
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                for (Overlay overlay : allPlotted) {
                    if (overlay instanceof Polygon) {
                        if (((Polygon) overlay).isInfoWindowOpen())
                            ((Polygon) overlay).closeInfoWindow();
                    } else if (overlay instanceof Marker) {
                        if (((Marker) overlay).isInfoWindowOpen())
                            ((Marker) overlay).closeInfoWindow();
                    }
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        map.getOverlays().add(mapEventsOverlay);
    }

}