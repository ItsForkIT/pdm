package com.disarm.sanna.pdm.Opp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by naman on 26/3/17.
 */

public class WifiScanReceiver extends BroadcastReceiver {
    public static String wifis[]={"None"};
    public void onReceive(Context c, Intent intent) {

        StartService.wifiScanList = StartService.wifi.getScanResults();
        wifis = new String[StartService.wifiScanList.size()];

        for(int i = 0; i < StartService.wifiScanList.size(); i++){
            wifis[i] = String.valueOf(StartService.wifiScanList.get(i));
            //Log.v("Available Networks : ", StartService.wifiScanList.get(i).SSID);
        }

    }
}
