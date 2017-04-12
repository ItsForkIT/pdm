package com.disarm.sanna.pdm.Opp;

import android.content.Context;

/**
 * Created by naman on 31/3/17.
 */

public class ApSwitch implements Runnable {
    private android.os.Handler handler;
    private Context context;
    public ApSwitch(android.os.Handler handler , Context context){
        this.context=context;
        this.handler=handler;
        this.handler.postDelayed(this,120000);
    }
    @Override
    public void run() {
        if(EnableAP.isApOn(context) && SwitchStateFinder.shouldAPChangeToIdel(StartService.macCount,Parameter.current_hotspot_running_time)){
            Toggler.toggle(context);
        }
        this.handler.postDelayed(this,120000);
    }
}
