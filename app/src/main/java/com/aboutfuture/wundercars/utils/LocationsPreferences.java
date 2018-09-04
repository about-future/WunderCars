package com.aboutfuture.wundercars.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aboutfuture.wundercars.R;

public class LocationsPreferences {

    // Return true if locations list was downloaded before or false if was never downloaded
    public static boolean getLocationsStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.pref_locations_status_key), false);
    }

    // Set locations downloading status to true the first time the app is run
    public static void setLocationsStatus(Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.pref_locations_status_key), status);
        editor.apply();
    }
}
