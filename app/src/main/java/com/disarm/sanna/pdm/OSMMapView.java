package com.disarm.sanna.pdm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;


public class OSMMapView extends AppCompatActivity {
    View bottomsheet;
    ListView maplv;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        bottomsheet = findViewById(R.id.rlbottomsheet);
        maplv = (ListView) findViewById(R.id.map_list_view);
        maplv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        GetLatLongAsync g = new GetLatLongAsync();
        g.execute();
    }



    public class GetLatLongAsync extends AsyncTask{

        URL url;
        HttpURLConnection httpCon;
        CompassOverlay mCompassOverlay;
        ScaleBarOverlay mScaleBarOverlay;
        MapView map;
        ITileSource tileSource;
        InputStream is;
        MyMarkerCluster rmc;
        @Override
        protected Object doInBackground(Object[] params) {
            try{
                BufferedReader reader = getReader("http://127.0.0.1:8080/getGIS/allLogs.txt");
                String data="";
                while((data = reader.readLine())!=null){
                    Pattern p = Pattern.compile(",");
                    String[] array = p.split(data);
                    if(array.length>2){
                        GeoPoint g = new GeoPoint(Double.parseDouble(array[0]),Double.parseDouble(array[1]));
                        Marker m = new Marker(map);
                        m.setPosition(g);
                        rmc.add(m);
                    }
                }
                is.close();
            }
            catch (Exception ex){
                Log.e("--I/O Exception--",ex.toString());
            }
            httpCon.disconnect();


            try{
                BufferedReader reader = getReader("http://127.0.0.1:8080/getGIS/allGIS.txt");
                String data="";
                while((data = reader.readLine())!=null){
                    Pattern p = Pattern.compile("_");
                    String[] array = p.split(data);
                    if(array.length>2){
                        GeoPoint g = new GeoPoint(Double.parseDouble(array[5]),Double.parseDouble(array[6]));
                        Marker m = new Marker(map);
                        m.setPosition(g);
                        rmc.addMarker(m,data);
                    }
                }
                is.close();
            }
            catch (Exception ex){
                Log.e("--I/O Exception--",ex.toString());
            }
            httpCon.disconnect();
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
            map = (MapView) findViewById(R.id.map);
            setTouchOnMap(map);
            map.setBuiltInZoomControls(true);
            map.setMultiTouchControls(true);
            IMapController mapController = map.getController();
            mapController.setZoom(15);
            rmc = new MyMarkerCluster(ctx,maplv);
            rmc.setMaxClusteringZoomLevel(20);
            rmc.fileAddress = new HashMap<Marker, String>();
            String[] s = {"http://127.0.0.1:8080/getTile/"};
            tileSource = new MyOSMTileSource(
                    "DISARM MAP SOURCE", 13, 20, 256, ".png", s);
            mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
            mCompassOverlay.enableCompass();
            map.getOverlays().add(mCompassOverlay);
            mScaleBarOverlay = new ScaleBarOverlay(map);
            mScaleBarOverlay.setCentred(true);
            mScaleBarOverlay.setScaleBarOffset(width/2, 10);
            map.getOverlays().add(mScaleBarOverlay);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                map.getController().setCenter(rmc.getItems().get(0).getPosition());
            }
            catch (Exception ex){

            }
            map.getOverlays().add(rmc);
            map.setTileSource(tileSource);
        }

        Polygon pointsAsCircle(GeoPoint center, double radiusInMeters){
            Polygon polygon = new Polygon(getBaseContext());
            ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>(360/6);
            for (int f = 0; f < 360; f += 6){
                GeoPoint onCircle = center.destinationPoint(radiusInMeters, f);
                circlePoints.add(onCircle);
            }
            polygon.setPoints(circlePoints);
            polygon.setFillColor(Color.TRANSPARENT);
            polygon.setStrokeColor(Color.BLUE);
            polygon.setStrokeWidth(2);
            return polygon;
        }

        private void setTouchOnMap(MapView map){
            MapEventsReceiver mReceive = new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    Toast.makeText(getBaseContext(),p.getLatitude() + " - "+p.getLongitude(),Toast.LENGTH_LONG).show();
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }
            };


            MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
            map.getOverlays().add(OverlayEvents);

        }
        private BufferedReader getReader(String urlString){
            BufferedReader reader = null;
            try {
                url = new URL(urlString);
            }
            catch (Exception ex){
                Log.e("-- URL Exception --",ex.toString());
            }
            try {
                httpCon = (HttpURLConnection) url.openConnection();
            }
            catch (Exception ex) {
                Log.e("-- HTTP Exception --",ex.toString());
            }
            try{
                is = httpCon.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            }
            catch (Exception ex){
                Log.e("--IO Exception--",ex.toString());
            }
            return reader;
        }
    }



}
