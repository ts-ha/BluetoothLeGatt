package com.example.android.bluetoothlegatt.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.bluetoothlegatt.R;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AutoConnectService extends Service {

    private static final String TAG = "AutoConnectService";
    private BluetoothAdapter mBluetoothAdapter;

    private static final int DIAGNOSTICS_TIMEOUT = 1500; // 3 seconds
    private static final int LAUNCH_DELAY = 500; // 1.5 seconds
    private boolean mScanning;
    private ServiceConnection mVehicleServiceConnection;
    private LocalBroadcastManager mBroadcastManager;
    private PowerManager.WakeLock mWakeLock;
    private BluetoothLeScanner mBluetoothLeScanner;
    private static final long SCAN_PERIOD = 10000;

    private final Handler mHandler = new Handler();
    private boolean mIsConnected;

    public static final ParcelUuid THERM_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");
    //////JBS/////
    private int vesChangedFlag = 0;
    //////JBS/////

    @Override
    public void onCreate() {
        super.onCreate();

        acquireWakeLock();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isEnabled()) {
            mBluetoothLeScanner = btAdapter.getBluetoothLeScanner();
            scanLeDevice(true);
        } else {
            stopSelf();
        }
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(scanCallback);
                    stopSelf();

                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothLeScanner.startScan(scanCallback);
        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(scanCallback);
        }

    }


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice device = result.getDevice();
            final byte[] scanRecord = result.getScanRecord().getBytes();
            final int rssi = result.getRssi();
            final Map<ParcelUuid, byte[]> serviceData = result.getScanRecord().getServiceData();
            Log.d(TAG, "onScanResult rssi : " + rssi);
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE)//int    the device type DEVICE_TYPE_CLASSIC, DEVICE_TYPE_LE DEVICE_TYPE_DUAL. DEVICE_TYPE_UNKNOWN if it's not available
            {
                byte[] bytes = serviceData.get(THERM_SERVICE);
                if (bytes == null) {

                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults: ");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "onScanFailed: " + errorCode);
        }
    };

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "onDestroy: " + mIsConnected);
        if (!mIsConnected) {
//            Logger.getLogger(TAG).debug("getBluetoothConnectTime : " + mSettingsStore.getBluetoothConnectTime());
            Log.d(TAG, "onDestroy: ");
            setAlarmIfNeeded(getApplicationContext(), 30000);
        }

        releaseWakeLock();
        super.onDestroy();
    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//    Logger.getLogger(TAG).trace("# onStartCommand(): startId=" + startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void cancelAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = getAlarmIntent(this);
        am.cancel(operation);
    }

    // default value should be true
    private static final AtomicBoolean sIsAlarmSet = new AtomicBoolean(true);
    private static final Object sLock = new Object();

    public static void setAlarm(Context context, int delay) {
        Log.d(TAG, "setAlarm: ~~~~~~~~~~~~~~~~~~~~~~~~~~");

        synchronized (sLock) {
            sIsAlarmSet.set(true);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent operation = getAlarmIntent(context);
            long triggerAt = SystemClock.elapsedRealtime() + delay;

            if (getEndTime() < System.currentTimeMillis()) {
                Log.d(TAG, "내일 알람  ~~~~~~~~~~~~~~~~~~~~~~~~~~");
                am.set(AlarmManager.RTC_WAKEUP, setTriggerTime(), operation);
            } else {
                Log.d(TAG, "20 간격으로 스캔  ~~~~~~~~~~~~~~~~~~~~~~~~~~");
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, operation);
            }
        }
    }

    public static void setAlarmIfNeeded(Context context, int delay) {
        if (sIsAlarmSet.get()) {
            setAlarm(context, delay);
        }
    }


    private static long getEndTime() {
        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, 10);
        curTime.set(Calendar.MINUTE, 30);
        curTime.set(Calendar.SECOND, 0);
        curTime.set(Calendar.MILLISECOND, 0);
        return curTime.getTimeInMillis();
    }

    private static long setTriggerTime() {
        // current Time
        long atime = System.currentTimeMillis();
        // timepicker
        Calendar curTime = Calendar.getInstance();
        curTime.set(Calendar.HOUR_OF_DAY, 8);
        curTime.set(Calendar.MINUTE, 30);
        curTime.set(Calendar.SECOND, 0);
        curTime.set(Calendar.MILLISECOND, 0);
        long btime = curTime.getTimeInMillis();
        long triggerTime = btime;
        if (atime > btime)
            triggerTime += 1000 * 60 * 60 * 24;

        return triggerTime;
    }


    public static void cancelAlarm(Context context) {
//    Logger.getLogger(TAG).debug("# cancel auto connect alarm");

        synchronized (sLock) {
            sIsAlarmSet.set(false);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent operation = getAlarmIntent(context);
            am.cancel(operation);

            // stop!
            Intent service = new Intent(context, AutoConnectService.class);
            context.stopService(service);
        }
    }

    private static PendingIntent getAlarmIntent(Context context) {
        Intent receiver = new Intent(context, AutoConnectService.class);
        return PendingIntent.getService(context, R.id.req_wake_up_auto_connect_service,
                receiver, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
