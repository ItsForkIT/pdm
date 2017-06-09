package com.disarm.sanna.pdm.Opp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * Created by naman on 30/3/17.
 */

public class BatteryLevel extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent intent) {
        StartService.level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }
}
