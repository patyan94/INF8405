package com.example.yannd.tp2_inf8405;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.util.Set;

/**
 * Created by Gol on 2016-02-25.
 */

public class RessourceMonitor extends BroadcastReceiver {

    static private RessourceMonitor mInstance = null;
    private float lastBatteryLevel;
    private Float initialBatteryLevel;
    private Intent batteryStatus;

    static public RessourceMonitor getInstance(){
        if(mInstance == null)
            mInstance = new RessourceMonitor();
        return mInstance;
    }

    private RessourceMonitor(){
        lastBatteryLevel =0f;
        initialBatteryLevel = 0f;
        batteryStatus = new Intent();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.batteryStatus = context.registerReceiver(null, ifilter);
        if(initialBatteryLevel != null) initialBatteryLevel = GetCurrentBatteryLevel();
    }

    void SetInitialBatteryLevel(Intent batteryStatus){
        this.batteryStatus = batteryStatus;
        initialBatteryLevel = GetCurrentBatteryLevel();
    }

    public float GetCurrentBatteryLevel()
    {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = (float)level / (float)scale * 100.0f;
        return batteryPct;
    }

    public float GetTotalBatteryUsage()
    {
        return  GetCurrentBatteryLevel() - initialBatteryLevel;
    }
    public void SaveCurrentBatteryUsage()
    {
        this.lastBatteryLevel = this.GetCurrentBatteryLevel();
    }
    public float GetLastBatteryUsage()
    {
        float latestBatteryLevel = GetCurrentBatteryLevel();
        return latestBatteryLevel - lastBatteryLevel;
    }
}
