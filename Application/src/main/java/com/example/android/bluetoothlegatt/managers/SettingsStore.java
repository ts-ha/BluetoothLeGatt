package com.example.android.bluetoothlegatt.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsStore {

    private static final String PREF_INTRODUCED = "introduced";
    private static final String PREF_ID = "id";
    private static final String PREF_PW = "pw";



    private static SettingsStore mInstance;

    private final SharedPreferences mPrefs;

//    private final Context mContext;

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new SettingsStore(context);
        }
    }

    public static SettingsStore getInstance() {
        return mInstance;
    }

    private SettingsStore(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        mContext = context.getApplicationContext();
    }

    public SharedPreferences getPreferences() {
        return mPrefs;
    }

    public void reset() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.clear();
        editor.commit();
    }

    public static void commit() {
        if (mInstance != null) {
            boolean result = mInstance.mPrefs.edit().commit();
        }
    }


    public void putIntroduced(boolean introduced) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(PREF_INTRODUCED, introduced);
        editor.apply();
    }

    public boolean isIntroduced() {
        return mPrefs.getBoolean(PREF_INTRODUCED, false);
    }



    public void putId(String id) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_ID, id);
        editor.apply();
    }

    public String getId() {
        return mPrefs.getString(PREF_ID, null);
    }

    public void putPw(String introduced) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_PW, introduced);
        editor.apply();
    }

    public String getPw() {
        return mPrefs.getString(PREF_PW, null);
    }
}
