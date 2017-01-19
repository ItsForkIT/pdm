package com.disarm.sanna.pdm.Util;


import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.Context;

import com.nextgis.maplib.api.IGeometryCache;
import com.nextgis.maplib.api.IGeometryCacheItem;
import com.nextgis.maplib.datasource.GeometryRTree;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapContentProviderHelper;
import com.nextgis.maplib.util.SettingsConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Sync GIS data available from peers
 * Created by arka on 18/1/17.
 */
public class SyncGIS {

    private ArrayList<File> gisFilesVectorA;
    private ArrayList<File> gisFilesVectorB;
    private ArrayList<File> gisFilesVectorC;
    private File path;
    private Context context;

    protected IGeometryCache cache;
    private IGeometryCache cacheVectorA;
    private IGeometryCache cacheVectorB;
    private IGeometryCache cacheVectorC;
    private File rtreeVectorA;
    private File rtreeVectorB;
    private File rtreeVectorC;

    /**
     * @param path
     *          path where incoming GIS files from peers will reside
     */
    public SyncGIS(Context context, File path) {
        this.context = context;
        this.path = path;
        this.cache = createNewCache();
        this.cacheVectorA = createNewCache();
        this.cacheVectorB = createNewCache();
        this.cacheVectorC = createNewCache();
        this.rtreeVectorA = new File(SettingsConstants.WORKING_DIR + "map/vector_a/rtree");
        this.rtreeVectorB = new File(SettingsConstants.WORKING_DIR + "map/vector_b/rtree");
        this.rtreeVectorC = new File(SettingsConstants.WORKING_DIR + "map/vector_c/rtree");
        findGisFiles();
    }

    public void setPath(File path) {
        this.path = path;
    }

    private IGeometryCache createNewCache() {
        return new GeometryRTree();
    }

    /**
     * Find all gis files collected from peers
     */
    private void findGisFiles () {
        gisFilesVectorA = new ArrayList<>();
        gisFilesVectorB = new ArrayList<>();
        gisFilesVectorC = new ArrayList<>();
        cache = createNewCache();

        for(File file: path.listFiles()) {
            if(!file.isDirectory() && file.getName().endsWith("rtree")) {
                if(file.getName().contains("vectorA")) {
                    gisFilesVectorA.add(file);
                } else if(file.getName().contains("vectorB")) {
                    gisFilesVectorB.add(file);
                } else if(file.getName().contains("vectorC")) {
                    gisFilesVectorC.add(file);
                }
            }
        }
    }

    /**
     * sync GIS information from peers
     */
    public void syncGisFiles() {
        findGisFiles();
        createNewVectorARtree();
        createNewVectorBRtree();
        createNewVectorCRtree();

        cacheVectorA.save(rtreeVectorA);
        cacheVectorB.save(rtreeVectorB);
        cacheVectorC.save(rtreeVectorC);
        // SyncAdapter Sync changes
        context.sendBroadcast(new Intent(SyncAdapter.SYNC_CHANGES));
    }

    /**
     * We extract nodes from all Gis files collected and insert them in our Gis file
     */
    private void createNewVectorARtree() {
        for(File filePath : gisFilesVectorA) {
            decodeRtree(filePath, 'A');
        }
        // save new Rtree for vector A here
    }

    private void createNewVectorBRtree() {
        for(File filePath : gisFilesVectorB) {
            decodeRtree(filePath, 'B');
        }
        // save new Rtree for vector B here
    }

    private void createNewVectorCRtree() {
        for(File filePath : gisFilesVectorC) {
            decodeRtree(filePath, 'C');
        }
        // save new Rtree for vector C here
    }

    /**
     * Get
     * @param path
     */
    private void decodeRtree(File path, char vectorType) {
        List<IGeometryCacheItem> items;

        cache.load(path);
        items = cache.getAll();
        cacheVectorA.load(rtreeVectorA);
        cacheVectorB.load(rtreeVectorB);
        cacheVectorC.load(rtreeVectorC);

        MapContentProviderHelper map = (MapContentProviderHelper) MapBase.getInstance();
        SQLiteDatabase db = map.getDatabase(false);

        // add items to corresponding file
        if(vectorType == 'A') {
            String table = "vector_a";
            long rowId = 0;
            for(IGeometryCacheItem item: items) {
                try {
                    rowId = db.rawQuery("SELECT ROWID from " + table + " order by ROWID DESC limit 1", null).getLong(0);
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    rowId = 0;
                }
                cacheVectorA.addItem(rowId + 1, item.getEnvelope());
                Log.d("YOLO : SyncGIS A", (rowId + 1) + " " + item.getEnvelope());
            }
        } else if(vectorType == 'B') {
            String table = "vector_b";
            long rowId = 0;
            for(IGeometryCacheItem item: items) {
                try {
                    rowId = db.rawQuery("SELECT ROWID from " + table + " order by ROWID DESC limit 1", null).getLong(0);
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    rowId = 0;
                }
                cacheVectorB.addItem(rowId + 1, item.getEnvelope());
                Log.d("YOLO : SyncGIS B", (rowId + 1) + " " + item.getEnvelope());
            }
        } else if(vectorType == 'C') {
            String table = "vector_c";
            long rowId = 0;
            for(IGeometryCacheItem item: items) {
                try {
                    rowId = db.rawQuery("SELECT ROWID from " + table + " order by ROWID DESC limit 1", null).getLong(0);
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    rowId = 0;
                }
                cacheVectorC.addItem(rowId + 1, item.getEnvelope());
                Log.d("YOLO : SyncGIS C", (rowId + 1) + " " + item.getEnvelope());
            }
        }
    }
}
