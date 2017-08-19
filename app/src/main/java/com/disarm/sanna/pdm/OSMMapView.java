package com.disarm.sanna.pdm;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        GetLatLongAsync g = new GetLatLongAsync();
        g.execute();
    }



    public class GetLatLongAsync extends AsyncTask{

        URL url;
        HttpURLConnection httpCon;
        CompassOverlay mCompassOverlay;
        ScaleBarOverlay mScaleBarOverlay;
        MapView map;
        ArrayList<OverlayItem> anotherOverlayItemArray;
        ArrayList<OverlayItem> localOverlayItemArray;
        ITileSource tileSource;
        GeoPoint startPoint;
        InputStream is;
        @Override
        protected Object doInBackground(Object[] params) {
            anotherOverlayItemArray = new ArrayList<OverlayItem>();
            localOverlayItemArray = new ArrayList<OverlayItem>();
            try{
                BufferedReader reader = getReader("http://127.0.0.1:8080/getGIS/allLogs.txt");
                String data="";
                while((data = reader.readLine())!=null){
                    Pattern p = Pattern.compile(",");
                    String[] array = p.split(data);
                    if(array.length>2){
                        GeoPoint g = new GeoPoint(Double.parseDouble(array[0]),Double.parseDouble(array[1]));
                        anotherOverlayItemArray.add(new OverlayItem("","",g));
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
                        localOverlayItemArray.add(new OverlayItem("","",g));
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
            map.setBuiltInZoomControls(true);
            map.setMultiTouchControls(true);
            IMapController mapController = map.getController();
            mapController.setZoom(15);
            startPoint = new GeoPoint(23.5500612,87.2912049);
            mapController.setCenter(startPoint);
            String[] s = {"http://127.0.0.1:8080/getTile/"};
            tileSource = new MyOSMTileSource(
                    "DISARM MAP SOURCE", 13, 16, 256, ".png", s);
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
            ItemizedIconOverlay<OverlayItem> markerFromAllLogs = new ItemizedIconOverlay<OverlayItem>(anotherOverlayItemArray,getResources().getDrawable(R.drawable.map_marker),null,getBaseContext());
            map.getOverlays().add(markerFromAllLogs);
            ItemizedIconOverlay<OverlayItem> markerFromGIS = new ItemizedIconOverlay<OverlayItem>(localOverlayItemArray,getResources().getDrawable(R.drawable.marker_default),null,getBaseContext());
            map.getOverlays().add(markerFromGIS);
            map.setTileSource(tileSource);

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
