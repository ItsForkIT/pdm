package com.disarm.sanna.pdm.Opp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by naman on 27/3/17.
 */

public class Toggler extends Activity {
    public static ArrayList<Integer> allFrequency = new ArrayList<>();
    public static boolean wifi;
    public static boolean AP;

    // Set hotspot creation minimum battery level
    private static double minimumBatteryLevel = 10;

    public static int convertFrequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }

    public static int findChannelWeight()
    {
        // Get frequency of all the channel available
        for ( int i = 0 ; i < StartService.wifiScanList.size();i++) {

            allFrequency.add(convertFrequencyToChannel(StartService.wifiScanList.get(i).frequency));
            //Log.v("Channel  + AP: ", String.valueOf(convertFrequencyToChannel(StartService.wifiScanList.get(i).frequency)) + "->" + String.valueOf(StartService.wifiScanList.get(i).SSID));
        }
        Log.v("All frequency:",allFrequency.toString());

        double channelArray[] = new double[13];

        // Fill all the elements with 0
        Arrays.fill(channelArray,0);

        double factor = 0.20;

        for (int band:allFrequency)
        {
            for (int i=band-5 ;i<= band+6 ;i++)
            {
                if (i<1 || i>13)
                    continue;
                channelArray[i-1] = (channelArray[i-1] + (6-Math.abs(i-band))*factor);
            }
        }

        // Minimum value of channel array
        int bestFoundChannel;

        // Find minimum value from the channel array
        double small = channelArray[0];
        int index = 0;
        for (int i = 0; i < channelArray.length; i++) {
            if (channelArray[i] < small) {
                small = channelArray[i];
                index = i;
            }
        }

        // Find in channel array the elements with minimum channel value
        ArrayList<Integer> bestFoundAvailableChannels = new ArrayList();
        for(int items = 0;items < channelArray.length;items++)
        {
            if(channelArray[items] == small)
            {
                bestFoundAvailableChannels.add(items);
            }
        }
        if(channelArray[6] < channelArray[0] && channelArray[6] <channelArray[11] )
            return  6;
        if(channelArray[11] < channelArray[0] && channelArray[11] < channelArray[6])
            return 11;

        return 1;
    }
    public static void toggle(Context c){
        Log.v(StartService.TAG3, "Toggling randomly!!!");

        // Start wifi for the first time and then randomly toggle
        if (Parameter.first_time == 1){
            wifi=true;
            Parameter.first_time = 0;
        }
        else{
            AP=SwitchStateFinder.shouldIBecomeAP(Parameter.recent_number_of_neighbours,Parameter.idle_time_for_AP_mode);
        }
        Log.v("Battery Level:", String.valueOf(StartService.level));
        Log.v("Present State:", StartService.presentState);

        if(AP && StartService.level > minimumBatteryLevel ) {
            // Present State
            StartService.presentState = "hotspot";
            String apHotspotName = "DH" + StartService.phoneVal;
            // Find channel weight of all Wifis
            StartService.bestAvailableChannel = findChannelWeight();
            Parameter.current_ap_time = Parameter.current_ap_time + Parameter.ap_increase_time;

            // Disabling Wifi and Enabling Hotspot
            StartService.wifi.setWifiEnabled(false);
            StartService.isHotspotOn = EnableAP.isApOn(c);

            if (!StartService.isHotspotOn) {
                EnableAP.configApState(c);
            }
            Log.v(StartService.TAG3, "Hotspot Active");

        }
        else {
            StartService.presentState = "wifi";

            // Wifi Mode Activated
            Parameter.current_wifi_time+=Parameter.increase_wifi_time;
            // Disabling hotspot and enable Wifi
            StartService.isHotspotOn = EnableAP.isApOn(c);

            if(StartService.isHotspotOn)
            {
                EnableAP.configApState(c);
            }
            StartService.wifi.setWifiEnabled(true);
            Log.v(StartService.TAG3, "Wifi Active");

            StartService.wifi.startScan();

        }

        if(Parameter.current_wifi_time == Parameter.maximum_wifi_time){
            Parameter.current_wifi_time = 10000;
        }
        else if(Parameter.current_ap_time == Parameter.maximum_ap_increase_time){
            Parameter.current_ap_time = 10000;
        }

    }
}
