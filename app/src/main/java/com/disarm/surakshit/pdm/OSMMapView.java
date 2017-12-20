package com.disarm.surakshit.pdm;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
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
        final BottomSheetBehavior behave = BottomSheetBehavior.from(bottomsheet);
        bottomsheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behave.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        behave.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_SETTLING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_HIDDEN");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i("BottomSheetCallback", "slideOffset: " + slideOffset);
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
        GeoPoint startPoint;
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
            String[] s = {"http://127.0.0.1:8080/getTile/"};
            tileSource = new MyOSMTileSource(
                    "Mapnik", 13, 19, 256, ".png", s);
            map.setTileSource(tileSource);
            map.setBuiltInZoomControls(true);
            map.setMultiTouchControls(true);
            IMapController mapController = map.getController();
            MapEventsReceiver mReceive = new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    Toast.makeText(getBaseContext(),p.getLatitude() + " - "+p.getLongitude(), Toast.LENGTH_LONG).show();

                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }
            };


            MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
            map.getOverlays().add(OverlayEvents);
            mapController.setZoom(15);

            startPoint = new GeoPoint(23.5497305,87.2886851 );
            mapController.setCenter(startPoint);

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

        }

        public Polygon pointsAsCircle(GeoPoint center, double radiusInMeters){
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
