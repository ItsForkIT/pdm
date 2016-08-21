package com.disarm.sanna.pdm.DisarmConnect;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import java.util.List;

/**
 * Created by hridoy on 21/8/16.
 */
public class SearchingDisarmDB implements Runnable {
    private android.os.Handler handler;
    private Context context;

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
        List allScanResults = MyService.wifi.getScanResults();
        if (allScanResults.toString().contains("DisarmHotspotDB")) {
            Log.v(MyService.TAG4, "Connecting DisarmDB");
            //handler.removeCallbacks(WifiConnect.class);
            handler.removeCallbacksAndMessages(null);
            String ssid = "DisarmHotspotDB";
            WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = "\"" + ssid + "\""; //IMPORTANT! This should be in Quotes!!
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            int res = MyService.wifi.addNetwork(wc);
            boolean b = MyService.wifi.enableNetwork(res, true);
            Log.v(MyService.TAG4, "Connected to DB");
        }
        else {
            Log.v(MyService.TAG4,"DisarmHotspotDB not found");
        }
        handler.postDelayed(this,5000);
    }

}
