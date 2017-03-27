package com.disarm.sanna.pdm.Opp;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

/**
 * Created by naman on 26/3/17.
 */

public class StartService extends Service {
    private final IBinder myServiceBinder = new MyServiceBinder();
    public static WifiManager wifi;
    public static String checkWifiState="0x";
    public static int level;
    public static BatteryLevel bl;
    public static WifiScanReceiver wifiReciever;
    public static boolean isHotspotOn,c;
    public static WifiInfo wifiInfo;
    public static List<String> IpAddr;
    public static String mobileAPName = "DH";
    public static String dbAPName = "DisarmHotspotDB";
    public static String dbPass = "DisarmDB";
    public FileReader fr = null;
    public static int count=0,startwififirst = 1;
    public static Handler handler;
    public static double wifiState;
    public static int macCount = 0;
    public static String TAG1 = "Timer_Toggle";
    public static String TAG2 = "WifiConnect";
    public static String TAG3 = "Toggler";
    public static String TAG4 = "Searching DB";
    public Timer_Toggler tt;
    public SearchingDisarmDB sDDB;
    public WifiConnect wifiC;
    private final IBinder myServiceBinder = new MyServiceBinder();
    public BufferedReader br = null;
    public static String phoneVal;
    public static String presentState="wifi";
    public static List<ScanResult> wifiScanList;
    public static int bestAvailableChannel;
    @Override

    public IBinder onBind(Intent intent) {
        return myServiceBinder;
    }
    public class MyServiceBinder extends Binder {
        public StartService getService() {
            // Return this instance of SyncService so activity can call public methods
            return StartService.this;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();

    }
}
