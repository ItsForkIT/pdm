package com.disarm.sanna.pdm;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.disarm.sanna.pdm.BackgroundProcess.FileTask;
import com.disarm.sanna.pdm.Capture.AudioCapture;
import com.disarm.sanna.pdm.Capture.Photo;
import com.disarm.sanna.pdm.Capture.Video;
import com.disarm.sanna.pdm.DisarmConnect.DCService;
import com.disarm.sanna.pdm.Service.SyncService;
import com.disarm.sanna.pdm.Util.PrefUtils;
import com.disarm.sanna.pdm.Util.Reset;
import com.disarm.sanna.pdm.location.LocationState;
import com.disarm.sanna.pdm.location.MLocation;
import com.snatik.storage.Storage;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
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
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UI_Map extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static Context contextOfApplication;
    Button save,draw,cancel;
    static MapView map;
    View bottomsheet;
    ITileSource tileSource;
    CompassOverlay mCompassOverlay;
    ScaleBarOverlay mScaleBarOverlay;
    IMapController mapController;
    final int MIN_ZOOM=13,MAX_ZOOM=19,PIXEL=256;
    SyncService syncService;
    public DCService myService;
    private boolean syncServiceBound = false;
    private boolean myServiceBound = false;
    private boolean gpsService = false;
    LocationManager lm;
    LocationListener locationListener;
    ArrayList<GeoPoint> polygon_points;
    int draw_flag=1;
    final Polygon polygon = new Polygon();
    String text_description="";
    final ArrayList<Marker> all_markers = new ArrayList<>();
    final static HashMap<String,Boolean> all_kmz_overlay_map = new HashMap<>();
    final static ArrayList<Overlay> allOverlays = new ArrayList<>();
    Handler refresh = new Handler();

    @Override
    protected void onCreate(Bundle drawdInstanceState) {
        super.onCreate(drawdInstanceState);
        setContentView(R.layout.activity_ui__map);
        contextOfApplication = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fab.setVisibility(View.INVISIBLE);
                draw.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                save.setVisibility(View.VISIBLE);
                polygon_points.clear();
                removeInfo();
                removeInfoWindow(); 
            }
        });

        Storage storage = new Storage(getApplicationContext());
        storage.deleteDirectory(Environment.getExternalStoragePublicDirectory("DMS/tmpOpen").toString());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        SelectCategoryActivity.SOURCE_PHONE_NO = PrefUtils.getFromPrefs(this, SplashActivity.PHONE_NO, "NA");
        Log.d("Phone No",SelectCategoryActivity.SOURCE_PHONE_NO);

        crashLog();
        startService();
        intialize();
        setBottomsheet();
        setMapData();
        setMapClick();
        setDrawClick();
        setCancelClick(fab);
        setSaveClick(fab);
        refreshWorkingData();
    }

    @Override
    public void onBackPressed() {

        Boolean isOpen = false;
        for(Overlay overlay : allOverlays){
            if(overlay instanceof Polygon){
                if(((Polygon) overlay).getInfoWindow().isOpen()){
                    isOpen = true;
                    ((Polygon) overlay).getInfoWindow().close();
                }

            }
            else if(overlay instanceof Marker){
                if(((Marker) overlay).getInfoWindow().isOpen())
                {
                    isOpen = true;
                    ((Marker) overlay).getInfoWindow().close();
                }
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!isOpen) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setMessage("Exit Application")
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    unbindAllService();
                                    System.exit(0);
                                }
                            });
            alertDialogBuilder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindAllService();
//        Storage storage = new Storage(getApplicationContext());
//        storage.deleteDirectory(Environment.getExternalStoragePublicDirectory("DMS/tmpOpen").toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ui__map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void intialize(){
        map = (MapView) findViewById(R.id.ui_map);
        String[] s = {"http://127.0.0.1:8080/getTile/"};
        tileSource = new MyOSMTileSource(
                "Mapnik", MIN_ZOOM, MAX_ZOOM, PIXEL, ".png", s);
        map.setTileSource(tileSource);
        draw = (Button) findViewById(R.id.btn_map_draw);
        cancel = (Button) findViewById(R.id.btn_map_cancel);
        save = (Button) findViewById(R.id.btn_map_save);
        bottomsheet = findViewById(R.id.map_bottomsheet);
        polygon_points = new ArrayList<>();
    }

    private void setBottomsheet(){

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
    }

    private void setMapData(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);

        GeoPoint startPoint = new GeoPoint(23.548512,87.2894873);
        mapController.setCenter(startPoint);
        mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);
        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(width/2, 10);
        map.getOverlays().add(mScaleBarOverlay);
        setWorkingData();
    }

    private void startService(){
        final Intent syncServiceIntent = new Intent(getBaseContext(), SyncService.class);
        bindService(syncServiceIntent, syncServiceConnection, Context.BIND_AUTO_CREATE);
        startService(syncServiceIntent);

        final Intent myServiceIntent = new Intent(getBaseContext(), DCService.class);
        bindService(myServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
        startService(myServiceIntent);

        if (!LocationState.with(UI_Map.this).locationServicesEnabled()){
            enableGPS();
        }
        MLocation.subscribe(UI_Map.this);
    }

    private void unbindAllService() {
        final Intent syncServiceIntent = new Intent(getBaseContext(), SyncService.class);
        if (syncServiceBound) {
            unbindService(syncServiceConnection);
        }
        syncServiceBound = false;
        stopService(syncServiceIntent);

        final Intent myServiceIntent = new Intent(getBaseContext(), DCService.class);
        if (myServiceBound) {
            unbindService(myServiceConnection);
        }
        myServiceBound = false;
        stopService(myServiceIntent);

        if (gpsService) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.removeUpdates(locationListener);
            gpsService = false;
        }
    }
    private ServiceConnection syncServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SyncService.SyncServiceBinder binder = (SyncService.SyncServiceBinder) service;
            syncService = binder.getService();
            syncServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            syncServiceBound = false;
        }
    };

    private ServiceConnection myServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DCService.MyServiceBinder binder = (DCService.MyServiceBinder) service;
            myService = binder.getService();
            myServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            myServiceBound = false;
        }
    };

    public void enableGPS() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(R.string.gps_msg)
                .setCancelable(false)
                .setTitle("Turn on Location")
                .setPositiveButton(R.string.enable_gps,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(callGPSSettingIntent, 5);
                            }
                        });
        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5 && resultCode == 0) {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (provider != null) {
                switch (provider.length()) {
                    case 0:
                        //GPS still not enabled..
                        Toast.makeText(UI_Map.this, "Please enable GPS!!!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        MLocation.subscribe(UI_Map.this);
                        Toast.makeText(this, R.string.enabled_gps, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        } else {
            //the user did not enable his GPS
            enableGPS();
        }
    }

    private void crashLog(){
        // draw crash logs in a file every time the application crashes
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                File crashLogFile =new File (SplashActivity.DMS_PATH+"PDM_CrashLog" );
                if (!crashLogFile.exists()){
                    crashLogFile.mkdir();
                }
                String filename = crashLogFile + "/" + sdf.format(cal.getTime())+".txt";

                PrintStream writer;
                try {
                    writer = new PrintStream(filename, "UTF-8");
                    writer.println(e.getClass() + ": " + e.getMessage());
                    for (int i = 0; i < e.getStackTrace().length; i++) {
                        writer.println(e.getStackTrace()[i].toString());
                    }
                    System.exit(1);
                } catch (FileNotFoundException | UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }


    private void setMapClick(){
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                if(draw.getVisibility()==View.VISIBLE){

                    polygon_points.add(p);
                    final Marker marker = new Marker(map);
                    marker.setPosition(p);
                    marker.setDraggable(true);
                    final GeoPoint g = new GeoPoint(p);
                    marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            marker.getInfoWindow().close();
                            return false;
                        }
                    });
                    marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDrag(Marker new_marker) {
                            int index = polygon_points.indexOf(g);
                            polygon_points.set(index,new_marker.getPosition());
                            map.getOverlays().remove(polygon);
                            polygon.getPoints().clear();
                            polygon.setPoints(polygon_points);
                            map.getOverlays().add(polygon);
                            g.setLatitude(new_marker.getPosition().getLatitude());
                            g.setLongitude(new_marker.getPosition().getLongitude());
                            g.setAltitude(new_marker.getPosition().getAltitude());
                        }

                        @Override
                        public void onMarkerDragEnd(Marker new_marker) {
                            int index = polygon_points.indexOf(g);
                            polygon_points.set(index,new_marker.getPosition());
                            map.getOverlays().remove(polygon);
                            polygon.getPoints().clear();
                            polygon.setPoints(polygon_points);
                            map.getOverlays().add(polygon);
                            g.setLatitude(new_marker.getPosition().getLatitude());
                            g.setLongitude(new_marker.getPosition().getLongitude());
                            g.setAltitude(new_marker.getPosition().getAltitude());
                        }

                        @Override
                        public void onMarkerDragStart(Marker marker) {

                        }
                    });
                    marker.setTitle(marker.getPosition().toString());
                    map.getOverlays().add(marker);
                    all_markers.add(marker);
                    return true;
                }
                else{
                    removeInfoWindow();
                    removeInfo();

                }
                return false;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return true;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);
    }

    private void setDrawClick(){
        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                polygon.setPoints(polygon_points);
                map.getOverlays().add(polygon);
                map.invalidate();
                removeInfoWindow();
                removeInfo();
            }
        });

    }

    private void setCancelClick(final FloatingActionButton fab){
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setVisibility(View.VISIBLE);
                draw.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                save.setVisibility(View.GONE);
                draw_flag=1;
                map.getOverlays().remove(polygon);
                polygon_points.clear();
                removeInfoWindow();
                removeInfo();
                for(int i=0;i<all_markers.size();i++){
                    map.getOverlays().remove(all_markers.get(i));
                }
                map.invalidate();
            }
        });
    }

    private void setSaveClick(final FloatingActionButton fab){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(Marker m : all_markers){
                    m.getInfoWindow().close();
                }
                createTextDialog();
                map.getOverlays().remove(polygon);
                for(int i=0;i<all_markers.size();i++){
                    map.getOverlays().remove(all_markers.get(i));
                }
                map.invalidate();
                draw.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                save.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        });
    }

    private void removeInfoWindow(){
        for(Marker m : all_markers){
            m.getInfoWindow().close();
        }
    }
    private void createTextDialog(){
        text_description="";
        View dialog_view = getLayoutInflater().inflate(R.layout.dialog_text,null);
        final EditText textmsg;
        Button save,add,cancel;
        textmsg = (EditText) dialog_view.findViewById(R.id.dialog_text_description);
        save = (Button) dialog_view.findViewById(R.id.dialog_button_save);
        add = (Button) dialog_view.findViewById(R.id.dialog_button_add);
        cancel = (Button) dialog_view.findViewById(R.id.dialog_button_cancel);

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(UI_Map.this);
        dialog_builder.setTitle("Please describe the situation in less than 50 words !!!");
        dialog_builder.setView(dialog_view);
        final AlertDialog dialog = dialog_builder.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textmsg.getText().toString().length()>0){
                    text_description = textmsg.getText().toString();
                    createDialog();
                }
                else{
                    Toast.makeText(getBaseContext(),"No info found to be saved!!! Please describe the situation there",Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textmsg.getText().toString().length()>0){
                    KmlDocument kml = new KmlDocument();
                    if(polygon_points.size()==1){
                        Marker marker = new Marker(map);
                        marker.setPosition(polygon_points.get(0));
                        marker.setSnippet(textmsg.getText().toString());
                        KmlPlacemark placemark = new KmlPlacemark(marker);
                        String latlng = FileTask.getloc(getContextOfApplication());
                        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        String file_name = "TXT_50_data_"+
                                SelectCategoryActivity.SOURCE_PHONE_NO+
                                "_defaultMcs_"
                                +latlng
                                +"_"+timeStamp+
                                "_1.kml";
                        kml.mKmlRoot.add(placemark);
                        kml.mKmlRoot.setExtendedData("Media Type","TXT");
                        kml.mKmlRoot.setExtendedData("Group Type","data");
                        kml.mKmlRoot.setExtendedData("Time Stamp",timeStamp);
                        kml.mKmlRoot.setExtendedData("Source",SelectCategoryActivity.SOURCE_PHONE_NO);
                        kml.mKmlRoot.setExtendedData("Destination","defaultMcs");
                        kml.mKmlRoot.setExtendedData("Lat Long",latlng);
                        kml.mKmlRoot.setExtendedData("Group ID","1");
                        kml.mKmlRoot.setExtendedData("Priority","50");
                        kml.mKmlRoot.setExtendedData("KML Type","Point");

                        File file = Environment.getExternalStoragePublicDirectory("DMS/Working/"+file_name);
                        kml.saveAsKML(file);
                    }
                    else if(polygon_points.size()>1){
                        Polygon polygon = new Polygon();
                        polygon_points.add(polygon_points.get(0));
                        polygon.setPoints(polygon_points);
                        polygon.setSnippet(textmsg.getText().toString());
                        String latlng = FileTask.getloc(getContextOfApplication());
                        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        String file_name = "TXT_50_data_"+
                                SelectCategoryActivity.SOURCE_PHONE_NO+
                                "_defaultMcs_"
                                +latlng
                                +"_"+timeStamp+
                                "_1.kml";
                        kml.mKmlRoot.setExtendedData("Media Type","TXT");
                        kml.mKmlRoot.setExtendedData("Group Type","data");
                        kml.mKmlRoot.setExtendedData("Time Stamp",timeStamp);
                        kml.mKmlRoot.setExtendedData("Source",SelectCategoryActivity.SOURCE_PHONE_NO);
                        kml.mKmlRoot.setExtendedData("Destination","defaultMcs");
                        kml.mKmlRoot.setExtendedData("Lat Long",latlng);
                        kml.mKmlRoot.setExtendedData("Group ID","1");
                        kml.mKmlRoot.setExtendedData("Priority","50");
                        kml.mKmlRoot.setExtendedData("KML Type","Polygon");
                        kml.mKmlRoot.addOverlay(polygon,kml);
                        File file = Environment.getExternalStoragePublicDirectory("DMS/Working/"+file_name);
                        kml.saveAsKML(file);
                    }
                    setWorkingData();
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(getBaseContext(),"No info found to be saved!!! Please describe the situation there",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }


            }
        });
    }
    private void createDialog(){
        View dialog_view = getLayoutInflater().inflate(R.layout.dialog_list_type,null);
        final TextView img,vid,aud;
        Button submit,discard;
        final EditText importance,destination;
        dialog_view.setPadding(10,10,10,10);

        img = (TextView) dialog_view.findViewById(R.id.dialog_tv_image);
        vid = (TextView) dialog_view.findViewById(R.id.dialog_tv_video);
        aud = (TextView) dialog_view.findViewById(R.id.dialog_tv_audio);
        submit = (Button) dialog_view.findViewById(R.id.dialog_save);
        discard = (Button) dialog_view.findViewById(R.id.dialog_discard);
        importance = (EditText) dialog_view.findViewById(R.id.dialog_importance);
        destination = (EditText) dialog_view.findViewById(R.id.dialog_destination);

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(UI_Map.this);
        dialog_builder.setTitle("Please select the media type which describes the situition best !!!");
        dialog_builder.setView(dialog_view);
        final AlertDialog dialog = dialog_builder.create();
        dialog.show();

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UI_Map.this, Photo.class);
                intent.putExtra("Intent type","Data");
                startActivity(intent);
            }
        });

        vid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UI_Map.this, Video.class);
                intent.putExtra("Intent type","Data");
                startActivity(intent);
            }
        });

        aud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UI_Map.this, AudioCapture.class);
                intent.putExtra("Intent type","Data");
                startActivity(intent);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dest = destination.getText().toString().trim();
                String imp = importance.getText().toString().trim();

                if(dest.isEmpty() || dest.length() == 0 || dest.equals("") || dest == null) {
                    dest = "defaultMcs";
                }

                if(imp.isEmpty() || img.length() ==0 || imp.equals("") || imp == null){
                    imp = "50";
                }

                new FileTask().execute(imp,dest,polygon_points,map,text_description);
                dialog.dismiss();
            }
        });

        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UI_Map.this);
                alertDialogBuilder
                        .setMessage(R.string.files_discard_msg)
                        .setCancelable(false)
                        .setPositiveButton(R.string.discard_files,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        File dir = new File(Environment.getExternalStorageDirectory() + "/DMS/tmp");
                                        if (Reset.deleteContents(dir)) {
                                            Toast.makeText(UI_Map.this, R.string.files_discarded, Toast.LENGTH_SHORT).show();
                                        }
                                        dialog.dismiss();
                                    }
                                });
                alertDialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();

            }
        });
    }



    public static void setWorkingData(){
        File working = Environment.getExternalStoragePublicDirectory("DMS/Working");
        File[] files = working.listFiles();
        for(final File file : files){
            if(file.getName().contains("MapDisarm")){
                continue;
            }
            if(all_kmz_overlay_map.containsKey(file.getName())){
                continue;
            }
            all_kmz_overlay_map.put(file.getName(),true);
            final KmlDocument kml = new KmlDocument();
            if(file.getName().contains("kmz")){
                kml.parseKMZFile(file);
            }
            else{
                kml.parseKMLFile(file);
            }
            final FolderOverlay kmlOverlay = (FolderOverlay)kml.mKmlRoot.buildOverlay(map, null, null, kml);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0;i<kmlOverlay.getItems().size();i++){
                        if(kmlOverlay.getItems().get(i) instanceof Polygon){
                              String desciption = ((Polygon) kmlOverlay.getItems().get(i)).getSnippet();
                              String latlon = kml.mKmlRoot.getExtendedData("Lat Long");
                              ((Polygon) kmlOverlay.getItems().get(i)).setInfoWindow(new CustomInfoWindow(R.layout.custom_info_window,map,desciption,latlon,file.getName()));
                              allOverlays.add(((Polygon) kmlOverlay.getItems().get(i)));
                        }
                        else if(kmlOverlay.getItems().get(i) instanceof Marker){
                            String description = ((Marker) kmlOverlay.getItems().get(i)).getSnippet();
                            String latlon = kml.mKmlRoot.getExtendedData("Lat Long");
                            ((Marker) kmlOverlay.getItems().get(i)).setInfoWindow(new CustomInfoWindow(R.layout.custom_info_window,map,description,latlon,file.getName()));
                            allOverlays.add(((Marker) kmlOverlay.getItems().get(i)));
                        }
                    }
                }
            });
            t.start();
            map.getOverlays().add(kmlOverlay);
        }
    }
    private void refreshWorkingData(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                setWorkingData();
                refresh.postDelayed(this,10000);
            }
        };
        refresh.postDelayed(r,10000);
    }
    private void removeInfo(){
        Boolean isOpen = false;
        for(Overlay overlay : allOverlays){
            if(overlay instanceof Polygon){
                if(((Polygon) overlay).getInfoWindow().isOpen()){
                    isOpen = true;
                    ((Polygon) overlay).getInfoWindow().close();
                }

            }
            else if(overlay instanceof Marker){
                if(((Marker) overlay).getInfoWindow().isOpen())
                {
                    isOpen = true;
                    ((Marker) overlay).getInfoWindow().close();
                }
            }
        }
    }
    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
}