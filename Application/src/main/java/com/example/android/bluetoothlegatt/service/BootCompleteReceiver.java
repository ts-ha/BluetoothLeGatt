package com.example.android.bluetoothlegatt.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "boot-complete-receiver";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        final int delay = 30000;

        AutoConnectService.setAlarm(context, delay);

    }

}
