package com.disarm.sanna.pdm.Opp;

import android.content.Context;
import android.util.Log;

import static com.disarm.sanna.pdm.Service.SyncService.discoverer;

/**
 * Created by naman on 30/3/17.
 */

public class ApModeCounter implements Runnable {
    private android.os.Handler handler;
    private Context context;
    public ApModeCounter(android.os.Handler handler , Context context){
        this.context=context;
        this.handler=handler;
        this.handler.postDelayed(this,1000);
    }
    @Override
    public void run() {
            if(EnableAP.isApOn(context)){
                Parameter.idle_time_for_AP_mode=0;
                Parameter.current_hotspot_running_time++;
                Log.v("ApModeCounter","Hotspot running time has reached over "+Parameter.current_hotspot_running_time);
                Logger.addRecordToLog("Hotspot running time has reached over "+Parameter.current_hotspot_running_time);
            }
            else{
                Parameter.idle_time_for_AP_mode++;
                if(Parameter.current_hotspot_running_time!=0)
                    Parameter.previous_hotspot_running_time = Parameter.current_hotspot_running_time;
                Parameter.current_hotspot_running_time=0;
                Log.v("ApModeCounter","AP is idel for "+Parameter.idle_time_for_AP_mode+" seconds");
            }
            if(discoverer!=null){
                if(Parameter.recent_number_of_neighbours>1&&Parameter.current_number_of_neighbours>1){
                    Parameter.recent_number_of_neighbours=Parameter.current_number_of_neighbours;
                }
                Parameter.current_number_of_neighbours = discoverer.originalPeerList.size();
                Log.v("Client side msg",Parameter.current_number_of_neighbours+" neighbours available");
            }
            this.handler.postDelayed(this,1000);
    }
}
