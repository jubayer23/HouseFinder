package com.creative.housefinder.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.housefinder.MainActivity;
import com.creative.housefinder.R;
import com.creative.housefinder.Utility.CommonMethods;
import com.creative.housefinder.Utility.GpsEnableTool;
import com.creative.housefinder.Utility.LastLocationOnly;
import com.creative.housefinder.Utility.UserLastKnownLocation;
import com.creative.housefinder.appdata.MydApplication;
import com.creative.housefinder.model.House;
import com.creative.housefinder.model.Houses;
import com.creative.housefinder.service.GpsServiceUpdate;
import com.creative.housefinder.service.GpsServiceUpdate2;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by comsol on 30-Dec-17.
 */

public class HouseListFragment extends Fragment implements View.OnClickListener, GpsServiceUpdate.ServiceCallbacks {

    private static final String TAG_REQUEST_HOME_PAGE = "tag_volley_request_in_home_page";

    private List<House> houses;

    private LastLocationOnly lastLocationOnly;


    private GpsServiceUpdate gpsServiceUpdate;
    private boolean bound = false;
    private static boolean isFirstLoad = true;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_list, container,
                false);


        init(view);


        return view;

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        isFirstLoad = true;
        // CommonMethods.copyRawFileToExternalMemory2(getActivity());
        if (((MainActivity) getActivity()).getHouses() == null) {
            String json = "{ \"houses\": " + CommonMethods.read_file(getActivity()) + "}";
            houses = MydApplication.gson.fromJson(json, Houses.class).getHouses();
            ((MainActivity) getActivity()).setHouses(houses);
            Log.e("DEBUG", String.valueOf(houses.size()));

        } else {
            houses = ((MainActivity) getActivity()).getHouses();
        }

        lastLocationOnly = new LastLocationOnly(getActivity());

        if (lastLocationOnly.canGetLocation()) {

            showProgressDialog("please wait..", true, false);
            UserLastKnownLocation.LocationResult locationResult = new UserLastKnownLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {

                    refreshList(location);


                }
            };
            UserLastKnownLocation myLocation = new UserLastKnownLocation();
            myLocation.getLocation(getActivity(), locationResult);

        } else {
            GpsEnableTool gpsEnableTool = new GpsEnableTool(getActivity());
            gpsEnableTool.enableGPs();
        }

    }

    /* Defined by ServiceCallbacks interface */
    @Override
    public void refreshList(Location location) {
        //Log.d("DEBUG", "its called from service update");
        final double loc_lat = CommonMethods.roundFloatToFiveDigitAfterDecimal(location.getLatitude());
        final double loc_lng = CommonMethods.roundFloatToFiveDigitAfterDecimal(location.getLongitude());
        Log.d("DEBUG", loc_lat + "");
        Log.d("DEBUG", loc_lng + "");



        if (isFirstLoad && bound) {
            Log.d("DEBUG","gps start 1st load");
            isFirstLoad = false;
            gpsServiceUpdate.startGpsUpdate();
        }


        dismissProgressDialog();
    }


    @Override
    public void onResume() {
        super.onResume();
       // Log.d("DEBUG", "resume fragment");
        // bind to Service
        Intent intent = new Intent(getActivity(), GpsServiceUpdate.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onPause() {
        super.onPause();
       // Log.d("DEBUG", "paused fragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        //Log.d("DEBUG", "stop fragment");
        // Unbind from service
        if (bound) {
            gpsServiceUpdate.setCallbacks(null); // unregister
            getActivity().unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d("DEBUG", "desctroyed fragment");
    }

    /**
     * Callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            GpsServiceUpdate.LocalBinder binder = (GpsServiceUpdate.LocalBinder) service;
            gpsServiceUpdate = binder.getService();
            bound = true;
            gpsServiceUpdate.setCallbacks(HouseListFragment.this); // register
            if (!isFirstLoad) {
                Log.d("DEBUG","gps start 2nd load");
                gpsServiceUpdate.startGpsUpdate();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };


    private void init(View view) {


    }


    @Override
    public void onClick(View view) {

        int id = view.getId();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("DEBUG", "on activity result called in fragment");
    }

    private ProgressDialog progressDialog;

    public void showProgressDialog(String message, boolean isIntermidiate, boolean isCancelable) {
       /**/
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog.setIndeterminate(isIntermidiate);
        progressDialog.setCancelable(isCancelable);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog == null) {
            return;
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
