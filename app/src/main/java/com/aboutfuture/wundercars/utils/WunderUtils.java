package com.aboutfuture.wundercars.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class WunderUtils {

    // Perform a state of network connectivity test and return true or false.
    public static boolean isConnected(Context context) {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo activeNetwork = null;
        if(cm != null){
            activeNetwork = cm.getActiveNetworkInfo();
        }

        // Return true if there is an active network and if the device is connected or connecting
        // to the active network, otherwise return false
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Return the width, height of the screen in dp
    public final static int[] getScreenSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        return new int[]{
                (int) (displayMetrics.widthPixels / displayMetrics.density),
                (int) (displayMetrics.heightPixels / displayMetrics.density)};
    }
}
