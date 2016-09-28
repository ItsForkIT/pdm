package com.disarm.sanna.pdm.DisarmConnect;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by hridoy on 21/8/16.
 */
public class SearchingDisarmDB implements Runnable {
    private android.os.Handler handler;
    private Context context;
    private int timerDBSearch = 5000;
    public int minDBLevel = 3;


    public SearchingDisarmDB(android.os.Handler handler, Context context)
    {
        this.handler = handler;
        this.context = context;

        this.handler.post(this);
    }
    @Override
    public void run()
    {
        Log.v(MyService.TAG4,"searching DB");
        List<ScanResult> allScanResults = MyService.wifi.getScanResults();
        if (allScanResults.toString().contains(MyService.dbAPName)) {
            // compare signal level
            int level = findDBSignalLevel(allScanResults);
            if (level < minDBLevel)
            {
                if(MyService.wifi.disconnect()) {
                    Logger.addRecordToLog("DB Disconnected as Level = " + level);
                    Log.v(MyService.TAG1,"DB Disconnected as Level = " + level);
                }
            }
            else {
                Log.v(MyService.TAG4, "Connecting DisarmDB");

              //  handler.removeCallbacksAndMessages(null); !-- Dont use it all other handler will be closed
                String ssid = MyService.dbAPName;
                WifiConfiguration wc = new WifiConfiguration();
                wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
                wc.preSharedKey = "\""+ MyService.dbPass +"\"";
                wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                //  wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                int res = MyService.wifi.addNetwork(wc);
                boolean b = MyService.wifi.enableNetwork(res, true);
                Log.v(MyService.TAG4, "Connected to DB");
            }

        }
        else {
            Log.v(MyService.TAG4,"DisarmHotspotDB not found");
        }
        handler.postDelayed(this, timerDBSearch);
    }
    public int findDBSignalLevel(List<ScanResult> allScanResults)
    {
        for (ScanResult scanResult : allScanResults) {
            if(scanResult.SSID.toString().equals(MyService.dbAPName)) {
                Log.v("SSID:",scanResult.SSID.toString());
                int level =  WifiManager.calculateSignalLevel(scanResult.level, 5);
                return level;
            }
        }
        return 0;
    }

}
