package com.disarm.sanna.pdm.DisarmConnect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.disarm.sanna.pdm.SplashActivity;
import com.disarm.sanna.pdm.Util.PrefUtils;
import com.disarm.sanna.pdm.location.LocationState;
import com.disarm.sanna.pdm.location.MLocation;

/**
 * Created by sanna on 3/29/17.
 */

public class ToggleWRTSpeed extends Service {
    private final Handler handler;
    private final Context context;
    String TAG = "ToggleWRTSpeed";
    float speed,thresholdSpeed = 2;
    private int counter = 0;

    public ToggleWRTSpeed(android.os.Handler handler, Context context) {
        this.handler = handler;
        this.context = context;

        this.handler.post(this);
    }

    @Override
    public void run() {
        if (LocationState.with(context).locationServicesEnabled()){
            if (PrefUtils.getFromPrefs(context, SplashActivity.GPS_LOC_LISTENER,"0").equals("0")){
                MLocation.subscribe(context);
                Toast.makeText(context, "Turing GPS on..", Toast.LENGTH_SHORT).show();
            }else {
                speed = getSpeedFromLoc();
            }
        }else {
            Toast.makeText(context, " GPS is OFF", Toast.LENGTH_SHORT).show();
        }

        if (speed > thresholdSpeed){
            if (counter > 4){
                Handler handler = new Handler();
                handler.removeCallbacksAndMessages(null);
                if (!ApManager.isApOn(context)){
                    ApManager.configApState(context);
                }
                counter = 0;
            }else {
                counter += 1;
            }

        }else{
            //do nothing
        }

        handler.postDelayed(this,5000);
    }

    private float getSpeedFromLoc(){
        Location l = MLocation.getLocation(context);
        if (l != null)
            return l.getSpeed();
        return 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
