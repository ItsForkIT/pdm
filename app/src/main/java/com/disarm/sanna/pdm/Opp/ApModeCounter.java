package com.disarm.sanna.pdm.Opp;

import android.content.Context;
import android.util.Log;

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
            if(StartService.isHotspotOn){
                Parameter.idle_time_for_AP_mode=0;
                Parameter.current_hotspot_running_time++;
                Log.w("ApModeCounter","Hotspot running time has reached over "+Parameter.current_hotspot_running_time);
                Logger.addRecordToLog("Hotspot running time has reached over "+Parameter.current_hotspot_running_time);
            }
            else{
                Parameter.idle_time_for_AP_mode++;
                if(Parameter.current_hotspot_running_time!=0)
                    Parameter.previous_hotspot_running_time = Parameter.current_hotspot_running_time;
                Parameter.current_hotspot_running_time=0;
                Log.w("ApModeCounter","AP is idel for "+Parameter.idle_time_for_AP_mode+" minutes");
            }
    }
}
