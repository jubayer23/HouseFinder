package com.creative.housefinder.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.housefinder.Utility.CommonMethods;

import java.util.HashMap;
import java.util.Map;


public class GpsServiceUpdate2 extends Service {
    // Connection detector class
    private static final String TAG_GPS_UPDATE_SERVICE = "tag_gps_update_service";

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private Context _context;

    private PowerManager.WakeLock mWakeLock;

    private static long lastUpdateForGpsInterval = 0;

    private static Location previousBestLocation = null;

    public LocationManager locationManager;

    public MyLocationListener listener;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 10000; // 1 minute

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        this._context = this;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        if (listener == null) {
            listener = new MyLocationListener();
        }
        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);

        if (gpsProvider != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return START_STICKY;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    listener
            );
        }


        return START_STICKY;
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
             Log.d("DEBUG", "change");

            final double loc_lat = CommonMethods.roundFloatToFiveDigitAfterDecimal(location.getLatitude());
            final double loc_lng = CommonMethods.roundFloatToFiveDigitAfterDecimal(location.getLongitude());
            location.setLatitude(loc_lat);
            location.setLongitude(loc_lng);

            if (isBetterLocation(location, previousBestLocation) &&
                    isBetterLocationCustom(location, previousBestLocation)) {

                if (location == null) {
                    // Log.d("DEBUG_ALERT", "location become null");
                    return;
                }


                String user_lat = String.valueOf(loc_lat);
                String user_lang = String.valueOf(loc_lng);

                /*Save User Location In Shared Pref*/
                // AppController.getInstance().getPrefManger().setUserLastKnownLat(user_lat);
                // AppController.getInstance().getPrefManger().setUserLastKnownLang(user_lang);
                /*Save User Location In Database*/
                // AppController.getsqliteDbInstance().addLocation(location);


                previousBestLocation = location;
            }


        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        //Log.d("DEBUG", "onBind");
        return null;
    }


    private boolean isBetterLocationCustom(Location currentLocation, Location previousLocation) {
        if (previousLocation == null) return true;

        else {
            double current_lat = currentLocation.getLatitude();
            double current_lang = currentLocation.getLongitude();
            if ((currentLocation.getLatitude() == previousLocation.getLatitude()) && (currentLocation.getLongitude()
                    == previousLocation.getLongitude())) {

                return false;
            } else if ((current_lat == current_lang) || current_lat == 0 || current_lang == 0) {
                return false;
            } else {
                double distance = 0;

                distance = currentLocation.distanceTo(previousLocation);
                if (distance < 12) return false;

            }
            return true;
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        //Log.v("STOP_SERVICE", "DONE");

        if (mWakeLock != null) {
            mWakeLock.release();
        }

        if (ContextCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
            locationManager = null;
        }
    }

}
