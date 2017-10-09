package org.strongswan.android.logic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class ManagedConfigurationService extends Service {

    private BroadcastReceiver configurationBroadcastReceiver;
    private ManagedConfigurationContract.Controller controller;

    @Override
    public void onCreate() {
        super.onCreate();
        configurationBroadcastReceiver = configurationBroadcastReceiver();
        controller = new ManagedConfigurationController();

        registerReceiver(configurationBroadcastReceiver, configurationChangedFilter());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(configurationBroadcastReceiver);
    }

    private IntentFilter configurationChangedFilter() {
        return new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);
    }

    private BroadcastReceiver configurationBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                controller.onConfigurationChange();
            }
        };
    }
}
