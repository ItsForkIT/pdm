package com.disarm.sanna.pdm.DisarmConnect;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hridoy on 21/8/16.
 */
public class WifiConnect implements Runnable {
    private android.os.Handler handler;
    private Context context;
    private FileReader fr= null;
    private BufferedReader br = null;
    public WifiConnect(android.os.Handler handler, Context context) {
        this.handler = handler;
        this.context = context;

        this.handler.post(this);
    }

    @Override
    public void run() {
        Log.v(MyService.TAG2,"Running Autoconnector");
        MyService.wifiInfo = MyService.wifi.getConnectionInfo();
        String ssidName = MyService.wifiInfo.getSSID();
        Log.v(MyService.TAG2, ssidName);
        if(ssidName.contains("DisarmHotspotDB")) {
            Log.v(MyService.TAG2,"Already Connected DB ");
            Logger.addRecordToLog("Already DB Connected");

        }
        else if(ssidName.contains("DisarmHotspot")) {
            Log.v(MyService.TAG2,"Already Connected");
            Logger.addRecordToLog("Already DH Connected");
            try {

                fr = new FileReader("/proc/net/arp");
                br = new BufferedReader(fr);
                String line;
                MyService.IpAddr = new ArrayList<String>();
                MyService.c = false;
                while ((line = br.readLine()) != null) {
                    String[] splitted = line.split(" +");
                    Log.v("Splitted:" , Arrays.deepToString(splitted));
                }
            }
            catch(Exception e)
            {}
        }

        else if(!ssidName.equals("<unknown ssid>")){
            Log.v(MyService.TAG2,"Checking For Disarm Hotspot");
            // Connecting to DisarmHotspot WIfi on Button Click

            List allScanResults = MyService.wifi.getScanResults();
            if (allScanResults.toString().contains("DisarmHotspotDB")) {
                Log.v(MyService.TAG2,"Connecting DisarmDB");

                String ssid = "DisarmHotspotDB";

                WifiConfiguration wc = new WifiConfiguration();
                wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                int res = MyService.wifi.addNetwork(wc);
                boolean b = MyService.wifi.enableNetwork(res, true);
                Log.v(MyService.TAG2, "Connected");

                Logger.addRecordToLog("DB Connected Successfully");
            }
            else if (allScanResults.toString().contains("DisarmHotspot")) {
                Log.v(MyService.TAG2,"Connecting Disarm");

                String ssid = "DisarmHotspot";
                String pass = "password123";
                WifiConfiguration wc = new WifiConfiguration();
                wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                wc.preSharedKey = "\""+ pass +"\"";
                wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                //wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                int res = MyService.wifi.addNetwork(wc);
                boolean b = MyService.wifi.enableNetwork(res, true);
                Log.v(MyService.TAG2, "Connected");

                Logger.addRecordToLog("DH Connected Successfully");
            }
            else{
                Log.v(MyService.TAG2,"Disarm Not Available");

                Logger.addRecordToLog("no DH/DB network available");

            }

        }
        handler.postDelayed(this,10000);
    }
}
