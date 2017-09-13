package com.disarm.sanna.pdm;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.disarm.sanna.pdm.DisarmConnect.DCService;
import com.disarm.sanna.pdm.Service.SyncService;
import com.disarm.sanna.pdm.Util.PrefUtils;
import com.disarm.sanna.pdm.location.LocationState;
import com.disarm.sanna.pdm.location.MLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


public class FrontActivity extends AppCompatActivity {
    RecyclerView rv;
    public static FileAdapter f;
    String name,source,no,type,path,time;
    List<FileRecord> lfr;
    Handler handleApater;
    Handler handleStatus;
    Switch serviceSwitch;
    ImageView ivStatus;
    Button btnCollectData;
    ImageButton ibMap;
    SyncService syncService;
    public DCService myService;
    private boolean syncServiceBound = false;
    private boolean myServiceBound = false;
    private boolean gpsService = false;
    LocationManager lm;
    LocationListener locationListener;
    int psync_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        SelectCategoryActivity.SOURCE_PHONE_NO = PrefUtils.getFromPrefs(this, SplashActivity.PHONE_NO, "NA");
        handleApater = new Handler();
        handleStatus = new Handler();
        rv = (RecyclerView) findViewById(R.id.rv);
        serviceSwitch = (Switch) findViewById(R.id.switchService);
        ivStatus = (ImageView) findViewById(R.id.ivStatus);
        btnCollectData = (Button) findViewById(R.id.btnCollectData);
        ibMap = (ImageButton) findViewById(R.id.ibMap);
        rv.addItemDecoration(new ItemDecoration(this, LinearLayoutManager.VERTICAL));

        setListeners();
        setFirstTime();
        callHandler();
        crashLog();



    }
    @Override
    public void onBackPressed() {
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindAllService();
    }

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
                                // startActivity(callGPSSettingIntent);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 5 && resultCode == 0){
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(provider != null){
                switch(provider.length()){
                    case 0:
                        //GPS still not enabled..
                        ivStatus.setImageResource(R.drawable.notrunning);
                        Toast.makeText(FrontActivity.this,"Please enable GPS!!!",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        MLocation.subscribe(FrontActivity.this);
                        Toast.makeText(this, R.string.enabled_gps, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
        else{
            //the user did not enable his GPS
            enableGPS();
        }
    }

    private String extractDate(String input){
        String time="";
        for(int i=0;i<4;i++){
            time=time+input.charAt(i);
        }
        time=time+"-";
        for(int i=4;i<6;i++){
            time=time+input.charAt(i);
        }
        time=time+"-";
        for(int i=6;i<8;i++){
            time=time+input.charAt(i);
        }
        String t="";
        time = time + " , ";
        for(int i=8;i<10;i++){
            t=t+input.charAt(i);
        }
        t = t + ":";
        for(int i=10;i<12;i++){
            t=t+input.charAt(i);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
        Date d = new Date();
        try {
            d = sdf.parse(t);
        }
        catch (Exception ex){
            Toast.makeText(FrontActivity.this,"Time parsing error",Toast.LENGTH_SHORT).show();
        }
        time = time + (new SimpleDateFormat("hh:mm a").format(d));
        return time;
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

    private void openFile(File file) {
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "*/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri data = FileProvider.getUriForFile(FrontActivity.this,FrontActivity.this.getApplicationContext().getPackageName()+".provider",file);

        intent.setDataAndType(data, type);
        try{
            startActivity(intent);
        }
        catch (Exception e){
            Toast.makeText(this,"Sorry !!! Can't open file",Toast.LENGTH_LONG).show();
        }
    }

    private void callHandler(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                lfr.clear();
                File dir = Environment.getExternalStoragePublicDirectory("DMS/Working");
                File[] files = dir.listFiles();
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.compare(f1.lastModified(), f2.lastModified());
                    }
                });
                for (File f : files) {
                    if (f.isFile())
                        name = f.getName();
                    else
                        continue;
                    path = "/DMS/Working/"+name;
                    Pattern pattern = Pattern.compile("_");
                    String[] result = pattern.split(name);
                    try {
                        type=result[0];
                        no=result[3];
                        source=result[5]+" , "+ result[6];
                        time = extractDate(result[7]);
                        lfr.add(new FileRecord(no, time, source, type, path));
                    }
                    catch (Exception e){

                    }

                }
                if(lfr!=null) {
                    Collections.reverse(lfr);
                    f.notifyDataSetChanged();
                }
                handleApater.postDelayed(this,15000);
            }
        };
        handleApater.postDelayed(r,15000);

        Runnable statusRun = new Runnable() {
            @Override
            public void run() {
                if(serviceSwitch.isChecked()){
                    if(MLocation.isGPS){
                        ivStatus.setImageResource(R.drawable.running);
                    }
                    else{
                        ivStatus.setImageResource(R.drawable.waiting);
                    }
                }
                else{
                    ivStatus.setImageResource(R.drawable.notrunning);
                }
                handleStatus.postDelayed(this,1000);
            }

        };
        handleStatus.postDelayed(statusRun,1000);
    }

    private void setFirstTime(){
        lfr = new ArrayList<>();
        File dir = Environment.getExternalStoragePublicDirectory("DMS/Working");
        File[] files = dir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });
        for (File f : files) {
            try {
                if (f.isFile())
                    name = f.getName();
                path = "/DMS/Working/"+name;
                Pattern pattern = Pattern.compile("_");
                String[] result = pattern.split(name);
                type=result[0];
                no=result[3];
                source=result[5]+" , "+ result[6];
                time = extractDate(result[7]);
                lfr.add(new FileRecord(no, time, source, type, path));
            }
            catch (Exception e){

            }

        }
        if(lfr!=null) {
            Collections.reverse(lfr);
            f = new FileAdapter(lfr);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            rv.setLayoutManager(mLayoutManager);
            rv.setAdapter(f);
        }
    }

    private void setListeners(){
        rv.addOnItemTouchListener(
                new RecyclerTouchListenerForFile(this, new RecyclerTouchListenerForFile.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        FileRecord file = lfr.get(position);
                        StringBuilder path = new StringBuilder(file.getPath());
                        path.deleteCharAt(0);
                        File f = Environment.getExternalStoragePublicDirectory(path.toString());
                        openFile(f);
                    }
                })
        );


        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    final DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i == -1){
                                psync_type=0;
                            }
                            else if(i == -2){
                                psync_type=1;
                            }
                            else{
                                psync_type=2;
                            }
                            final Intent syncServiceIntent = new Intent(getBaseContext(), SyncService.class);
                            syncServiceIntent.putExtra("PSync",psync_type+"");
                            bindService(syncServiceIntent, syncServiceConnection, Context.BIND_AUTO_CREATE);
                            startService(syncServiceIntent);
                        }
                    };
                    AlertDialog.Builder sd = new AlertDialog.Builder(FrontActivity.this);
                    sd.setTitle("Mode of Sync Service");
                    sd.setMessage("Which type of service do you want to run?");
                    sd.setPositiveButton("Random", dialogClick);
                    sd.setNegativeButton("Based on Priority", dialogClick);
                    sd.setNeutralButton("Based on Importance", dialogClick);
                    sd.show();

                    final Intent myServiceIntent = new Intent(getBaseContext(), DCService.class);
                    bindService(myServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
                    startService(myServiceIntent);


                    if (!LocationState.with(FrontActivity.this).locationServicesEnabled()){
                        enableGPS();
                    }
                    MLocation.subscribe(FrontActivity.this);
                }
                else{
                    MLocation.unsubscribe(FrontActivity.this);
                    final Intent myServiceIntent = new Intent(getBaseContext(), DCService.class);
                    if (myServiceBound) {
                        unbindService(myServiceConnection);
                        myServiceBound = false;
                        stopService(myServiceIntent);
                    }
                    final Intent syncServiceIntent = new Intent(getBaseContext(), SyncService.class);
                    if (syncServiceBound) {
                        unbindService(syncServiceConnection);
                    }
                    syncServiceBound = false;
                    stopService(syncServiceIntent);
                }
            }
        });

        btnCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDisasterManagement = new Intent(FrontActivity.this, ActivityList.class);
                intentDisasterManagement.putExtra("IntentType", "Data");
                startActivity(intentDisasterManagement);
            }
        });
        ibMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serviceSwitch.isChecked()){
                Intent it = new Intent(FrontActivity.this,OSMMapView.class);
                startActivity(it);
                }
                else{
                    Toast.makeText(getBaseContext(),"Please enable service!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void crashLog(){
        // Save crash logs in a file every time the application crashes
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


}
