package com.disarm.sanna.pdm.Opp;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by naman on 27/3/17.
 */

public class Timer_Toggler implements Runnable{
    private BufferedReader br;
    private FileReader fr = null;
    private List<String> IpAddr;
    private android.os.Handler handler;
    private Context context;
    public Timer_Toggler(android.os.Handler handler, Context context)
    {
        this.handler = handler;
        this.context = context;

        this.handler.post(this);
    }
    @Override
    public void run() {
        StartService.wifi = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiInfo wifiInfo = StartService.wifi.getConnectionInfo();
        StartService.checkWifiState = wifiInfo.getSSID();
        Log.v(StartService.TAG1, "Ticking Random Function");
        Logger.addRecordToLog("Ticking");
        Log.v(StartService.TAG1, StartService.checkWifiState);
        StartService.count++;
        List<ScanResult> allScanResults = StartService.wifi.getScanResults();

        if (StartService.checkWifiState.equals("<unknown ssid>")) {
            StartService.macCount=0;
            Log.v(StartService.TAG1, "Hotspot Mode Detected");
            Logger.addRecordToLog("Hotspot Mode Detected");
            boolean isReachable = false;
            try {
                fr = new FileReader("/proc/net/arp");
                br = new BufferedReader(fr);
                String line;
                IpAddr = new ArrayList<String>();
                StartService.c = false;
                while ((line = br.readLine()) != null) {
                    String[] splitted = line.split(" +");

                    if (splitted != null) {
                        if (splitted[3].matches("..:..:..:..:..:..")) {
                            Log.v("Timer_toggle","Pinging: " + splitted[0]  );
                            Process p1 = Runtime.getRuntime().exec("ping -c 1 -t 1 " + splitted[0]);

                            int returnVal = p1.waitFor();
                            isReachable = (returnVal == 0);

                        }
                        if (isReachable) {
                            StartService.c = true;
                            Log.v(StartService.TAG1, "C IS TRUE !!! ");

                        }

                        // Basic sanity check
                        String mac = splitted[3];
                        System.out.println("Mac : Outside If " + mac);

                        if (mac.matches("..:..:..:..:..:..")) {
                            StartService.macCount++;

                            IpAddr.add(splitted[0]);

                            Log.v(StartService.TAG1, "IP Address  " + splitted[0] + "   MAC_ADDRESS  " + mac);
                            Logger.addRecordToLog("Connected Client, IP :" + splitted[0] + ",mac:" + mac);
                        }
                    }
                }
                if (StartService.c) {
                    Log.v(StartService.TAG1, "Connected!!! ");
                } else {
                    Log.v(StartService.TAG1, "Not Connected!!! ");
                }
            } catch (Exception e) {
                Log.v(StartService.TAG1, "exception", e);
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

            if (!StartService.c) {
                Toggler.toggle(context);
            }


        } //if Completed check

        else if(StartService.checkWifiState.contains("DisarmHotspotDB")) {
            //Log.v(StartService.TAG1, "DisarmHotspotDB Not Toggling");

        }
        else if (StartService.checkWifiState.contains("DH-")) {

            String connectedText = StartService.checkWifiState + " connected";
            Logger.addRecordToLog("Connected to DH");
            // Trying to search for better DH
            if(SwitchStateFinder.shouldSTAChange(Parameter.current_number_of_neighbours))
                findBetterDHAvailable(allScanResults);
        }
        else
        {
            Toggler.toggle(context);
        }
        boolean apOn = EnableAP.isApOn(context);
        if(apOn){
            //Parameter.current_ap_time=Parameter.current_ap_time+Parameter.ap_increase_time;
            this.handler.postDelayed(this,Parameter.current_ap_time);
            Log.v("AP","Ap time is increased to "+Parameter.current_ap_time);
        }else{
            //Parameter.current_wifi_time=Parameter.current_wifi_time+Parameter.increase_wifi_time;
            this.handler.postDelayed(this,Parameter.current_wifi_time);
            Log.v("WiFi","WiFi time is increased to "+Parameter.current_wifi_time);
        }
    }

    public void findBetterDHAvailable(List<ScanResult> allScanResults)
    {
        // Store all DH available in allDHAvailable
        Map allDHAvailable = new HashMap<String, Integer>();

        // Put all found DH to allDHAvailable Map
        for (ScanResult scanResult : allScanResults) {
            if(scanResult.SSID.toString().contains("DH-")) {
                allDHAvailable.put(scanResult.SSID,scanResult.level);
            }
        }
        if(allDHAvailable.size()>1){
            allDHAvailable.remove(StartService.wifiInfo.getSSID());
        }
        //Log.v("AllDH Available:", Arrays.asList(allDHAvailable).toString());
        Logger.addRecordToLog("All DH available:" + Arrays.asList(allDHAvailable).toString());
        //Find key with the maximum value from allDHAvailable
        String bestFoundSSID="";
        int maxValueInMap = 0;
        try {
            maxValueInMap = (int) Collections.max(allDHAvailable.values());  // This will return max value in the Hashmap
            Iterator it = allDHAvailable.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pair = (Map.Entry) it.next();
                if (pair.getValue() == maxValueInMap) {
                    Logger.addRecordToLog("Best Found SSID"+ ',' + pair.getKey());
                    bestFoundSSID = pair.getKey().toString();
                }
            }
        }
        catch (Exception e)
        {}
        // Connect to the best found network
        String pass = "password123";
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + bestFoundSSID + "\""; //IMPORTANT! This should be in Quotes!!
        wc.preSharedKey = "\""+ pass +"\"";
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        int res = StartService.wifi.addNetwork(wc);
        boolean b = StartService.wifi.enableNetwork(res, true);
        //Log.v(StartService.TAG2, "Connected");

    }
}
