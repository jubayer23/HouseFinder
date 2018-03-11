package com.creative.housefinder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by comsol on 03-Jun-17.
 */
public class MyMmsSentReceiver extends BroadcastReceiver {
    private static final String TAG = "MyMmsSentReceiver";



    @Override
    public void onReceive(final Context context, final Intent intent) {


        if (intent.getExtras() != null) {
            Log.d("DEBUG","mms sent");
        }
    }
}