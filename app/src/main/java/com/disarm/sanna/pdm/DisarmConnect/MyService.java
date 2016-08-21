package com.disarm.sanna.pdm.DisarmConnect;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sanna on 16-02-2016.
 */
public class MyService extends Service {

    public static WifiManager wifi;
    public static String wifis[]={"None"}, checkWifiState="0x";
    public static int level;
    public static BatteryLevel bl;
    public static WifiScanReceiver wifiReciever;
    public static boolean isHotspotOn,c;
    public static WifiInfo wifiInfo;
    public static List<String> IpAddr;

    public FileReader fr = null;
    public static int count=0,startwififirst = 1;
    public static Handler handler;
    public static double wifiState;
    public static int macCount = 0;
    public static String TAG1 = "Timer_Toggle";
    public static String TAG2 = "WifiConnect";
    public static String TAG3 = "Toggler";
    public static String TAG4 = "Searching DB";
    Logger logger;
    public Timer_Toggler tt;
    public SearchingDisarmDB sDDB;
    public WifiConnect wifiC;
    private final IBinder myServiceBinder = new MyServiceBinder();
    public BufferedReader br = null;
    @Override
    public IBinder onBind(Intent intent) {

        return myServiceBinder;
    }
    public class MyServiceBinder extends Binder {
        public MyService getService() {
            // Return this instance of SyncService so activity can call public methods
            return MyService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();

        // WifiScanReceiver registered
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, filter);

        // Start scan for Wifi List
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();

        // Logger Initiated
        logger = new Logger();

        // Battery Level Indicator registered
        bl = new BatteryLevel();
        IntentFilter batfilter = new IntentFilter();
        batfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(bl, batfilter);

    }


    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // DisarmConnect Service started
        logger.addRecordToLog("DisarmConnect Started");

        // Acquired WakeLock
        WakeLockHelper.keepCpuAwake(getApplicationContext(), true);
        WakeLockHelper.keepWiFiOn(getApplicationContext(), true);

        // Handler started
        handler = new Handler();
        tt = new Timer_Toggler(handler,getApplicationContext());
        //handler.post(Timer_Toggle);
        wifiC = new WifiConnect(handler,getApplicationContext());
        //handler.post(WifiConnect);
        sDDB = new SearchingDisarmDB(handler,getApplicationContext());
        //  handler.post(searchingDisarmDB);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregistering receivers
        unregisterReceiver(wifiReciever);
        unregisterReceiver(bl);

        // Disabling hotspot and enabling WiFi Mode on app destroy
        isHotspotOn = ApManager.isApOn(MyService.this);
        if(isHotspotOn){
            ApManager.configApState(MyService.this);
            wifi.setWifiEnabled(true);
        }

        // Removing Callbacks from Handler
        //handler.removeCallbacks(WifiConnect);
//        handler.removeCallbacks(Timer_Toggle);
        //handler.removeCallbacks(searchingDisarmDB);

        // Release lock
        WakeLockHelper.keepCpuAwake(getApplicationContext(), false);
        WakeLockHelper.keepWiFiOn(getApplicationContext(), false);

        // Adding stop record to log
        logger.addRecordToLog("DisarmConnect Stopped");
    }




    private Runnable WifiConnect = new Runnable() {

        public void run() {

            Log.v(TAG2,"Running Autoconnector");
            wifiInfo = wifi.getConnectionInfo();
            String ssidName = wifiInfo.getSSID();
            Log.v(TAG2, ssidName);
            if(ssidName.contains("DisarmHotspotDB")) {
                Log.v(TAG2,"Already Connected DB ");
                logger.addRecordToLog("Already DB Connected");

            }
            else if(ssidName.contains("DisarmHotspot")) {
                Log.v(TAG2,"Already Connected");
                logger.addRecordToLog("Already DH Connected");
                try {

                    fr = new FileReader("/proc/net/arp");
                    br = new BufferedReader(fr);
                    String line;
                    IpAddr = new ArrayList<String>();
                    c = false;
                    while ((line = br.readLine()) != null) {
                        String[] splitted = line.split(" +");
                        Log.v("Splitted:" , Arrays.deepToString(splitted));
                    }
                }
                catch(Exception e)
                {}
            }

            else if(!ssidName.equals("<unknown ssid>")){
                Log.v(TAG2,"Checking For Disarm Hotspot");
                // Connecting to DisarmHotspot WIfi on Button Click

                List allScanResults = wifi.getScanResults();
                if (allScanResults.toString().contains("DisarmHotspotDB")) {
                    Log.v(TAG2,"Connecting DisarmDB");

                    String ssid = "DisarmHotspotDB";
                    WifiConfiguration wc = new WifiConfiguration();
                    wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int res = wifi.addNetwork(wc);
                    boolean b = wifi.enableNetwork(res, true);
                    Log.v(TAG2, "Connected");

                    logger.addRecordToLog("DB Connected Successfully");
                }
                else if (allScanResults.toString().contains("DisarmHotspot")) {
                    Log.v(TAG2,"Connecting Disarm");

                    String ssid = "DisarmHotspot";
                    WifiConfiguration wc = new WifiConfiguration();
                    wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int res = wifi.addNetwork(wc);
                    boolean b = wifi.enableNetwork(res, true);
                    Log.v(TAG2, "Connected");

                    logger.addRecordToLog("DH Connected Successfully");
                }
                else{
                    Log.v(TAG2,"Disarm Not Available");

                    logger.addRecordToLog("no DH/DB network available");

                }

            }
            handler.postDelayed(WifiConnect,10000);
        }

    };

    private Runnable searchingDisarmDB = new Runnable() {
        @Override
        public void run() {
            Log.v(TAG4,"searching DB");
            List allScanResults = wifi.getScanResults();
            if (allScanResults.toString().contains("DisarmHotspotDB")) {
                Log.v(TAG4, "Connecting DisarmDB");
                handler.removeCallbacks(WifiConnect);
                handler.removeCallbacksAndMessages(null);
                String ssid = "DisarmHotspotDB";
                WifiConfiguration wc = new WifiConfiguration();
                wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                int res = wifi.addNetwork(wc);
                boolean b = wifi.enableNetwork(res, true);
                Log.v(TAG4, "Connected to DB");
            }
            else {
                Log.v(TAG4,"DisarmHotspotDB not found");
            }
            handler.postDelayed(searchingDisarmDB,5000);
        }
    };


}