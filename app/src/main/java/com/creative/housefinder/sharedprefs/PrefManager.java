package com.creative.housefinder.sharedprefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.creative.housefinder.BuildConfig;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jubayer on 6/6/2017.
 */


public class PrefManager {
    private static final String TAG = PrefManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    private static Gson GSON = new Gson();
    // Sharedpref file name
    private static final String PREF_NAME = BuildConfig.APPLICATION_ID;

    private static final String KEY_EMAIL_CACHE = "key_email_cache";
    private static final String KEY_NAME = "key_name";
    private static final String KEY_NUMBER = "key_number";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

    }

    public void setEmailCache(String obj) {
        editor = pref.edit();

        editor.putString(KEY_EMAIL_CACHE, obj);

        // commit changes
        editor.commit();
    }
    public String getEmailCache() {
        return pref.getString(KEY_EMAIL_CACHE,"");
    }


   public void setName(String obj) {
        editor = pref.edit();

        editor.putString(KEY_NAME, obj);

        // commit changes
        editor.commit();
    }
    public String getName() {
        return pref.getString(KEY_NAME,"");
    }

    public void setNumber(String obj) {
        editor = pref.edit();

        editor.putString(KEY_NUMBER, obj);

        // commit changes
        editor.commit();
    }
    public String getNumber() {
        return pref.getString(KEY_NUMBER,"");
    }

}