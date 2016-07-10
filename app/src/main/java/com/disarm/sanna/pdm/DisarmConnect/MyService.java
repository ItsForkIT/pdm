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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.disarm.sanna.pdm.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

/**
 * Created by Sanna on 16-02-2016.
 */
public class MyService extends Service {

    WifiManager wifi;
    public static String wifis[]={"None"}, checkWifiState="0x";
    int level;
    WifiScanReceiver wifiReciever;
    boolean b,c;
    WifiInfo wifiInfo;
    List<String> IpAddr;
    BufferedReader br = null;
    FileReader fr = null;
    int count=0,startwififirst = 1;
    Handler handler;
    double wifiState;
    int macCount = 0;
    NotificationManager notificationManager;
    private static final String TAG1 = "Timer_Toggle";
    private static final String TAG2 = "WifiConnect";
    private static final String TAG3 = "Toggler";
    private static final String TAG4 = "Searching DB";
    private int addIncreasewifi = 5000,wifiIncrease=5000,hpIncrease=5000,addIncreasehp = 0;
    private final IBinder myServiceBinder = new MyServiceBinder();
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            //Log.v("BatteryLevel", String.valueOf(level) + "%");

        }
    };
    
    private class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {

            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++){
                wifis[i] = String.valueOf(wifiScanList.get(i));
                Log.v("networks", wifiScanList.get(i).SSID);
            }

        }
    }


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
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, filter);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();

        IntentFilter batfilter = new IntentFilter();
        batfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatInfoReceiver, batfilter);


    }


    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        WakeLockHelper.keepCpuAwake(getApplicationContext(), true);
        WakeLockHelper.keepWiFiOn(getApplicationContext(), true);
        handler = new Handler();
        handler.post(Timer_Toggle);
        handler.post(WifiConnect);
        handler.post(searchingDisarmDB);


        Notification n =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("DisarmConnect Service")
                        .setContentText("Service Started")
                        .setAutoCancel(false)
                        .setOngoing(true).build();

        notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReciever);
        // unregister receiver
        unregisterReceiver(this.mBatInfoReceiver);
        b = ApManager.isApOn(MyService.this);
        if(b){
            ApManager.configApState(MyService.this);
            wifi.setWifiEnabled(true);
        }
        notificationManager.cancelAll();
        handler.removeCallbacks(WifiConnect);
        handler.removeCallbacks(Timer_Toggle);
        handler.removeCallbacks(searchingDisarmDB);
        WakeLockHelper.keepCpuAwake(getApplicationContext(), false);
        WakeLockHelper.keepWiFiOn(getApplicationContext(), false);

        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    private Runnable Timer_Toggle = new Runnable() {

        public void run() {


            wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            checkWifiState = wifiInfo.getSSID();
            Log.v(TAG1, "Ticking");
            Log.v(TAG1, checkWifiState);
            count++;


            if (checkWifiState.equals("<unknown ssid>")) {
                Log.v(TAG1, "Hotspot Mode Detected");
                boolean isReachable = false;
                try {

                    fr = new FileReader("/proc/net/arp");
                    br = new BufferedReader(fr);
                    String line;
                    IpAddr = new ArrayList<String>();
                    c = false;
                    while ((line = br.readLine()) != null) {
                        String[] splitted = line.split(" +");

                        if (splitted != null) {
                            if (splitted[3].matches("..:..:..:..:..:..")) {
                                Process p1 = Runtime.getRuntime().exec("ping -c 1 -t 1 " + splitted[0]);
                                int returnVal = p1.waitFor();
                                isReachable = (returnVal == 0);

                            }
                            if (isReachable) {
                                c = true;
                                Log.v(TAG1, "C IS TRUE !!! ");

                            }

                            // Basic sanity check
                            String mac = splitted[3];
                            System.out.println("Mac : Outside If " + mac);

                            if (mac.matches("..:..:..:..:..:..")) {
                                macCount++;

                                IpAddr.add(splitted[0]);

                                System.out.println("Mac : " + mac + " IP Address : " + splitted[0]);
                                System.out.println("Mac_Count  " + macCount + " MAC_ADDRESS  " + mac);
                                Toast.makeText(MyService.this, "maccount = "+ macCount, Toast.LENGTH_SHORT).show();
                                Log.v(TAG1, "IP Address  " + splitted[0] + "   MAC_ADDRESS  " + mac);
                            }
                        }
                    }
                    if (c) {
                        Log.v(TAG1, "Connected!!! ");
                    } else {
                        Log.v(TAG1, "Not Connected!!! ");

                    }
                } catch (Exception e) {
                    Log.v(TAG1, "exception", e);
                } finally {
                    if (fr != null) {
                        try {
                            fr.close();
                            br.close();
                            IpAddr.clear();
                        } catch (IOException e) {
                            // This is unrecoverable. Just report it and move on
                            e.printStackTrace();
                        }
                    }
                }

                if (!c) {
                    toggle();
                }


            } //if Completed check

            else if(checkWifiState.contains("DisarmHotspotDB")) {
                Log.v(TAG1, "DisarmConnectedDB Not Toggling");
            }
            else if (checkWifiState.contains("DisarmHotspot")) {
                handler.post(searchingDisarmDB);
                Log.v(TAG1, "DisarmConnected Not Toggling");

            }

            else
             {
                toggle();
            }
            boolean apOn = ApManager.isApOn(MyService.this);
            if(apOn){
                handler.postDelayed(Timer_Toggle,addIncreasehp);
            }else{
                handler.postDelayed(Timer_Toggle,addIncreasewifi);
            }
        }

    };



    private Runnable WifiConnect = new Runnable() {

        public void run() {

            Log.v(TAG2,"Running Autoconnector");
            wifiInfo = wifi.getConnectionInfo();
            String ssidName = wifiInfo.getSSID();
            Log.v(TAG2, ssidName);
            if(ssidName.contains("DisarmHotspotDB")) {
                Log.v(TAG2,"Already Connected DB ");
            }
            else if(ssidName.contains("DisarmHotspot")) {
                Log.v(TAG2,"Already Connected");
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
                }
                else{
                    Log.v(TAG2,"Disarm Not Available");

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
                handler.removeCallbacks(Timer_Toggle);
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

    private void toggle(){
        Log.v(TAG3, "Toggling randomly!!!");
        //This method runs in the same thread as the UI.
        //Do something to the UI thread here
        if (startwififirst == 1){
            wifiState= 0.70;
            startwififirst = 0;
        }else{
            wifiState = Math.random()*1.0;
            Log.v(TAG3, String.valueOf(wifiState));
        }
        //wifiState = false;
        // WifiState - 1 (Is Hotspot) || 0 - (CheckHotspot)
        if(wifiState <= 0.25 && level > 25 ) {
            Log.v(TAG1,"hptoggling for " +String.valueOf(addIncreasehp));
            addIncreasehp += hpIncrease;
            wifi.setWifiEnabled(false);
            b = ApManager.isApOn(MyService.this);

            if (!b) {
                ApManager.configApState(MyService.this);
            }
            Log.v(TAG3, "Hotspot Active");
        }
        else {
            Log.v(TAG3,"wifitogging for "+ String.valueOf(addIncreasewifi));
            addIncreasewifi += wifiIncrease;
            b = ApManager.isApOn(MyService.this);
            if(b)
            {
                ApManager.configApState(MyService.this);

            }

            wifi.setWifiEnabled(true);
            Log.v(TAG3, "Wifi Active");
            wifi.startScan();

        }

        if(addIncreasewifi == 35000 ){
            addIncreasewifi = 5000;
        }else if(addIncreasehp == 35000){
            addIncreasehp = 5000;
        }

    }
}