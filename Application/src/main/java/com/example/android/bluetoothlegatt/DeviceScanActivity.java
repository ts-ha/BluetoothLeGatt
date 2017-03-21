/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.managers.SettingsStore;
import com.example.android.bluetoothlegatt.service.AutoConnectService;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DeviceScanActivity extends AppCompatActivity {
    //    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mBluetoothLeScanner;
    public static final ParcelUuid THERM_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");
    private static final String INTENT_ACTION = "arabiannight.tistory.com.alarmmanager";
    private String TAG = "DeviceScanActivity";


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActionBar().setTitle(R.string.title_devices);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        mHandler = new Handler();

        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        Log.d(TAG, "onCreate getAddress() " + mBluetoothAdapter.getAddress());

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();


        String macAddress = android.provider.Settings.Secure.getString(getContentResolver(), "bluetooth_address");
        Log.d(TAG, "onCreate macAddress : " + macAddress);

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//
//        Intent Intent = new Intent(INTENT_ACTION);
//        PendingIntent pIntent = PendingIntent.getActivity(this, 0, Intent, 0);
//
//        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, pIntent);
        AutoConnectService.cancelAlarm(this);

        final TextInputLayout layoutUserName = (TextInputLayout) findViewById(R.id.layout_user_name);
        final TextInputLayout layoutPassword = (TextInputLayout) findViewById(R.id.layout_user_password);
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                layoutUserName.setError("Lorem ipsum dolor sit amet, consectetur adipisicing elit");
//                layoutPassword.setError("sed do eiusmod tempor incididunt ut labore et dolore magna aliqua");
                SettingsStore instance = SettingsStore.getInstance();
                instance.putPw(layoutUserName.getEditText().toString());
                instance.putId(layoutPassword.getEditText().toString());

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        AutoConnectService.setAlarm(this, 30000);
        super.onPause();

    }

}