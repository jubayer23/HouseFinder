package com.creative.housefinder.fragment;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.creative.housefinder.MainActivity;
import com.creative.housefinder.R;
import com.creative.housefinder.Utility.CommonMethods;
import com.creative.housefinder.Utility.GpsEnableTool;
import com.creative.housefinder.Utility.LastLocationOnly;
import com.creative.housefinder.Utility.UserLastKnownLocation;
import com.creative.housefinder.adapter.HouseAdapter;
import com.creative.housefinder.appdata.MydApplication;
import com.creative.housefinder.model.House;
import com.creative.housefinder.model.Houses;
import com.creative.housefinder.service.GpsServiceUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by comsol on 30-Dec-17.
 */

public class HouseListFragment extends Fragment implements View.OnClickListener, GpsServiceUpdate.ServiceCallbacks {

    private static final String TAG_REQUEST_HOME_PAGE = "tag_volley_request_in_home_page";

    private List<House> houses;
    private List<House> top_ten_closest_houses = new ArrayList<>();

    private LastLocationOnly lastLocationOnly;


    private GpsServiceUpdate gpsServiceUpdate;
    private boolean bound = false;
    private static boolean isFirstLoad = true;


    private RecyclerView recyclerView;
    private HouseAdapter houseAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_list, container,
                false);


        init(view);


        initAdapter();

        return view;

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);


        // CommonMethods.copyRawFileToExternalMemory2(getActivity());
        if (((MainActivity) getActivity()).getHouses() == null) {
            String json = "{ \"houses\": " + CommonMethods.read_file(getActivity()) + "}";
            houses = MydApplication.gson.fromJson(json, Houses.class).getHouses();
            ((MainActivity) getActivity()).setHouses(houses);
        } else {
            houses = ((MainActivity) getActivity()).getHouses();
        }

        lastLocationOnly = new LastLocationOnly(getActivity());

        if (lastLocationOnly.canGetLocation()) {

            forceRefreshLocation();

        } else {
            GpsEnableTool gpsEnableTool = new GpsEnableTool(getActivity());
            gpsEnableTool.enableGPs();
        }

    }


    private void init(View view) {

        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
    }
    private void initAdapter() {


        houseAdapter = new HouseAdapter(getActivity(), top_ten_closest_houses);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(houseAdapter);


    }

    private void forceRefreshLocation(){

        if(bound){
            isFirstLoad = true;
            gpsServiceUpdate.stopGps();
        }

        showProgressDialog("please wait..", true, false);
        UserLastKnownLocation.LocationResult locationResult = new UserLastKnownLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {

                refreshList(location);


            }
        };
        UserLastKnownLocation myLocation = new UserLastKnownLocation();
        myLocation.getLocation(getActivity(), locationResult);
    }

    /* Defined by ServiceCallbacks interface */
    @Override
    public void refreshList(Location location) {
        //Log.d("DEBUG", "its called from service update");
        final double loc_lat = CommonMethods.roundFloatToSixDigitAfterDecimal(location.getLatitude());
        final double loc_lng = CommonMethods.roundFloatToSixDigitAfterDecimal(location.getLongitude());


        /*for(House house:houses){
            float[] result1 = new float[3];
            android.location.Location.distanceBetween(loc_lat, loc_lng, house.getLatitude(), house.getLongitude(), result1);
            Float distance1 = result1[0];
            Log.d("DEBUG", String.valueOf(distance1));
            count++;
            if(count == 10)break;
        }*/

        sortLocations(loc_lat,loc_lng);

        top_ten_closest_houses.clear();
        int count = 0;
        for(House house:houses){
            top_ten_closest_houses.add(house);
            count++;
            if(count == 10)break;
        }

        houseAdapter.notifyDataSetChanged();


        if (isFirstLoad && bound) {
           // Log.d("DEBUG","gps start 1st load");
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
               // Log.d("DEBUG","gps start 2nd load");
                gpsServiceUpdate.startGpsUpdate();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public List<House> sortLocations( final double myLatitude,final double myLongitude) {
        Comparator comp = new Comparator<House>() {
            @Override
            public int compare(House o, House o2) {
                float[] result1 = new float[3];
                Location.distanceBetween(myLatitude, myLongitude, o.getLatitude(), o.getLongitude(), result1);
                Float distance1 = result1[0];
                o.setDistance(distance1);

                float[] result2 = new float[3];
                Location.distanceBetween(myLatitude, myLongitude, o2.getLatitude(), o2.getLongitude(), result2);
                Float distance2 = result2[0];
                o2.setDistance(distance2);

                return distance1.compareTo(distance2);
            }
        };


        Collections.sort(houses, comp);
        return houses;
    }


    @Override
    public void onClick(View view) {

        int id = view.getId();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GpsEnableTool.REQUEST_CHECK_SETTINGS){
            lastLocationOnly = new LastLocationOnly(getActivity());

            if (lastLocationOnly.canGetLocation()) {

                forceRefreshLocation();

            }
        }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {

        switch (paramMenuItem.getItemId()) {


            case R.id.action_refresh:
                forceRefreshLocation();
                // startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.ydoodle.mymoneymanager")));
                // Toast.makeText(MainActivity.this,"Please publish your app on play store first!",Toast.LENGTH_LONG).show();
                break;
            case R.id.action_help:
                // This case is handled in MainActvity
                break;
        }

        return super.onOptionsItemSelected(paramMenuItem);
    }


}
