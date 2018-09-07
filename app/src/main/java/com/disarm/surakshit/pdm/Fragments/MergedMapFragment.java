package com.disarm.surakshit.pdm.Fragments;


import android.app.Application;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.disarm.surakshit.pdm.Chat.ChatActivity;
import com.disarm.surakshit.pdm.DB.DBEntities.App;
import com.disarm.surakshit.pdm.DB.DBEntities.MergedKMLEntity;
import com.disarm.surakshit.pdm.DB.DBEntities.MergedKMLEntity_;
import com.disarm.surakshit.pdm.DB.DBEntities.VersionEntity;
import com.disarm.surakshit.pdm.DB.DBEntities.VersionEntity_;
import com.disarm.surakshit.pdm.Merging.GISMerger;
import com.disarm.surakshit.pdm.Merging.MergeConstants;
import com.disarm.surakshit.pdm.Merging.MergeUtil.KmlObject;
import com.disarm.surakshit.pdm.Merging.MergeUtil.MergeDecisionPolicy;
import com.disarm.surakshit.pdm.R;
import com.disarm.surakshit.pdm.Util.CustomInfoWindow;
import com.disarm.surakshit.pdm.Util.LatLonUtil;
import com.disarm.surakshit.pdm.Util.MergedInfoWindow;
import com.disarm.surakshit.pdm.location.MLocation;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
    public FloatingActionButton versionDBButton;
    Application app;
    private ListView listView;
    private Drawable iconDrawable;
    public static final long ANY_VERSION = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merged_map, container, false);
        map = view.findViewById(R.id.fragment_merged_mapView);
        mergeButton = view.findViewById(R.id.fragment_merged_fab);
        versionDBButton = view.findViewById(R.id.fragment_merged_db_fab);
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
        app = getActivity().getApplication();
        showMergedFilesFromDB(true, ANY_VERSION);
        return view;
    }

    private void showVersionDBDialogHelper() {
        Box<VersionEntity> versionEntityBox = ((App) app).getBoxStore().boxFor(VersionEntity.class);
        if (versionEntityBox.getAll().size() == 0) {
            Toast.makeText(app, "No db found", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = versionEntityBox.getAll().size();
        String prev = "";
        String cur;
        final String[] versionArray = new String[count];
        final HashMap<String, Integer> map = new HashMap<>();
        int i = 0;
        for (VersionEntity versionEntity : versionEntityBox.getAll()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            long entityTime = versionEntity.getTimeStamp();
            int version = versionEntity.getVersion();
            if (prev.equals("")) {
                versionArray[i] = "Initial Database";
                prev = dateFormat.format(new Date(entityTime));
                map.put(versionArray[i], version);
                i++;
                continue;
            }
            cur = dateFormat.format(new Date(entityTime));
            versionArray[i] = prev + "-" + cur;
            map.put(versionArray[i], version);
            prev = cur;
            i++;
        }
        View dialog = getLayoutInflater().inflate(R.layout.dialog_dbversion, null);
        final MaterialStyledDialog materialStyledDialog = new MaterialStyledDialog.Builder(getActivity())
                .setTitle("Change Merge Setting")
                .setCustomView(dialog, 10, 10, 10, 10)
                .setScrollable(true)
                .withDialogAnimation(true, Duration.FAST)
                .setCancelable(true)
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .build();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, versionArray);
        listView = dialog.findViewById(R.id.dialog_dbversion_listview);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                removeOverlay();
                allPlotted.clear();
                showMergedFilesFromDB(false, map.get(versionArray[pos]));
                materialStyledDialog.dismiss();
            }
        });
        materialStyledDialog.show();
    }

    private void showMergedFilesFromDB(boolean latest, long dbVersion) {
        Box<MergedKMLEntity> mergedKMLEntityBox = ((App) app).getBoxStore().boxFor(MergedKMLEntity.class);
        Box<VersionEntity> versionEntityBox = ((App) app).getBoxStore().boxFor(VersionEntity.class);
        long count = versionEntityBox.count();
        if (count == 0)
            return;
        if (latest)
            dbVersion = versionEntityBox.query().build().property(VersionEntity_.version).max();
        Log.d("obdb", "version in merged map:" + dbVersion);
        List<MergedKMLEntity> kmlEntities = mergedKMLEntityBox.query().equal(MergedKMLEntity_.mergedVersion, dbVersion).build().find();
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
                    String snippet = entity.getAudioCount() + ";" + entity.getImageCount() + ";" + entity.getVideoCount();
                    ((Polygon) overlay).setSnippet(snippet);
                    MergedInfoWindow ciw = new MergedInfoWindow(R.layout.merged_info_window, map, (Polygon) overlay, getActivity());
                    ((Polygon) overlay).setInfoWindow(ciw);
                    map.getOverlays().add(overlay);
                    allPlotted.add(overlay);
                }
            }
        }
        for (MergedKMLEntity entity : kmlEntities) {
            Log.d("obdb", entity.getTitle() + ":" + entity.getAudioCount() + ":" + entity.getImageCount() + ":" + entity.getVideoCount());
        }
        versionEntityBox.closeThreadResources();
        mergedKMLEntityBox.closeThreadResources();
    }

    private void setFab() {
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HandlerThread handlerThread = new HandlerThread("MergeKML");
                handlerThread.start();

                Handler h = new Handler(handlerThread.getLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        MergeDecisionPolicy decisionPolicy = new MergeDecisionPolicy(MergeDecisionPolicy.DISTANCE_THRESHOLD_POLICY, 50, 0);
                        GISMerger.mergeGIS(getActivity().getApplication(), map, decisionPolicy, true);
                    }
                });
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeOverlay();
                        allPlotted.clear();
                        showMergedFilesFromDB(true, ANY_VERSION);
                    }
                }, 2000);
            }
        });

        versionDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVersionDBDialogHelper();
            }
        });
    }


    private void removeOverlay() {
        for (Overlay overlay : map.getOverlays()) {
            if (!(overlay instanceof MapEventsOverlay)) {
                if (overlay instanceof Marker) {
                    if (overlay != marker) {
                        map.getOverlays().remove(overlay);
                    }
                } else if (overlay instanceof Polygon)
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
        mapController.setZoom(17.0);
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