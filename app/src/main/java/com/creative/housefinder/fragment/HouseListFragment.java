package com.creative.housefinder.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.housefinder.MainActivity;
import com.creative.housefinder.R;
import com.creative.housefinder.Utility.CommonMethods;
import com.creative.housefinder.Utility.GpsEnableTool;
import com.creative.housefinder.Utility.LastLocationOnly;
import com.creative.housefinder.Utility.RunnTimePermissions;
import com.creative.housefinder.Utility.UserLastKnownLocation;
import com.creative.housefinder.adapter.HouseAdapter;
import com.creative.housefinder.alertbanner.AlertDialogForAnything;
import com.creative.housefinder.appdata.GlobalAppAccess;
import com.creative.housefinder.appdata.MydApplication;
import com.creative.housefinder.customView.RecyclerItemClickListener;
import com.creative.housefinder.helperActivity.OpenCameraToTakePic;
import com.creative.housefinder.model.House;
import com.creative.housefinder.model.Houses;
import com.creative.housefinder.service.GpsServiceUpdate;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.File;
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

    private static final int CAMERA_ACTIVITY_REQUEST = 1000;

    private static House selectedHouse;
    private static String selectedTag;

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
        if (MydApplication.getInstance().getPrefManger().getHouses().isEmpty()) {
            String json = "{ \"houses\": " + CommonMethods.read_file(getActivity()) + "}";
            houses = MydApplication.gson.fromJson(json, Houses.class).getHouses();
            //((MainActivity) getActivity()).setHouses(houses);
            MydApplication.getInstance().getPrefManger().setHouses(houses);
        } else {
            //houses = ((MainActivity) getActivity()).getHouses();
            houses = MydApplication.getInstance().getPrefManger().getHouses();
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

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    private void initAdapter() {


        houseAdapter = new HouseAdapter(getActivity(), top_ten_closest_houses);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(houseAdapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        selectedHouse = top_ten_closest_houses.get(position);


                        showSendingOptionsDialog(selectedHouse);


                    }
                })
        );


    }

    private void forceRefreshLocation() {

        if (bound) {
            isFirstLoad = true;
            gpsServiceUpdate.stopGps();
        }

        showProgressDialog("please wait..", true, false);
        UserLastKnownLocation.LocationResult locationResult = new UserLastKnownLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {

                GpsServiceUpdate.user_lat = location.getLatitude();
                GpsServiceUpdate.user_lang = location.getLongitude();

                refreshList(location);


            }
        };
        UserLastKnownLocation myLocation = new UserLastKnownLocation();
        myLocation.getLocation(getActivity(), locationResult);
    }

    /* Defined by ServiceCallbacks interface */
    @Override
    public void refreshList(Location location) {
        showProgressDialog("List is refreshing..", true, false);
        //Log.d("DEBUG", "its called from service update");
        final double loc_lat = CommonMethods.roundFloatToSixDigitAfterDecimal(location.getLatitude());
        final double loc_lng = CommonMethods.roundFloatToSixDigitAfterDecimal(location.getLongitude());


        /*for(House house:houses){
            Log.d("DEBUG", String.valueOf(house.getDistance()));
        }*/

        // sortLocations(49.45812,-119.578193);

        calculateDistance(loc_lat, loc_lng);

        Collections.sort(houses);

        /*for(int i = 0; i<houses.size(); i++){
            Log.d("DEBUG_3", String.valueOf(houses.get(i).getDistance()));
        }*/
       /* int count2 = 0;
        for(int i = houses.size() - 1; count2 < 150; i--, count2++){
            Log.e("DEBUG_4", String.valueOf(houses.get(i).getDistance()));
        }*/


        top_ten_closest_houses.clear();
        int count = 0;
        for (House house : houses) {
            top_ten_closest_houses.add(house);
            count++;
            if (count == 10) break;
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


    private void calculateDistance(final double myLatitude, final double myLongitude) {
        for (House house : houses) {
            Location startPoint = new Location("locationA");
            startPoint.setLatitude(myLatitude);
            startPoint.setLongitude(myLongitude);

            Location endPoint = new Location("locationB");
            endPoint.setLatitude(house.getLatitude());
            endPoint.setLongitude(house.getLongitude());

            Float distance = startPoint.distanceTo(endPoint);
            // Log.d("DEBUG_1",String.valueOf(distance1));

            house.setDistance(distance);
        }
    }

    public void sortLocations(final double myLatitude, final double myLongitude) {
        Comparator comp = new Comparator<House>() {
            @Override
            public int compare(House o, House o2) {


                Location startPoint = new Location("locationA");
                startPoint.setLatitude(myLatitude);
                startPoint.setLongitude(myLongitude);

                Location endPoint = new Location("locationB");
                endPoint.setLatitude(o.getLatitude());
                endPoint.setLongitude(o.getLongitude());

                double distance1 = startPoint.distanceTo(endPoint);
                // Log.d("DEBUG_1",String.valueOf(distance1));

                o.setDistance(distance1);

                Location endPoint2 = new Location("locationC");
                endPoint.setLatitude(o2.getLatitude());
                endPoint.setLongitude(o2.getLongitude());

                double distance2 = startPoint.distanceTo(endPoint2);
                //Log.d("DEBUG_2",String.valueOf(distance1));

                o2.setDistance(distance2);


                if (distance1 > distance2) {
                    return 1;
                } else if (distance1 < distance2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };


        Collections.sort(houses, comp);
    }


    @Override
    public void onClick(View view) {

        int id = view.getId();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GpsEnableTool.REQUEST_CHECK_SETTINGS) {
            lastLocationOnly = new LastLocationOnly(getActivity());

            if (lastLocationOnly.canGetLocation()) {

                forceRefreshLocation();

            }
        }

        if (requestCode == CAMERA_ACTIVITY_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String filePath = data.getStringExtra(OpenCameraToTakePic.KEY_FILE_URL);
                Log.d("DEBUG", filePath);
                String mapLink = GlobalAppAccess.URL_GOOGLE_MAP + "(" + String.valueOf(GpsServiceUpdate.user_lat) + "," + String.valueOf(GpsServiceUpdate.user_lang) + ")";

                String body = selectedHouse.getAddressText() + ";\n\n" + selectedTag + ";\n\n" + "Driver name: " + MydApplication.getInstance().getPrefManger().getName() + ";\n" + "Driver location: " + mapLink;


               Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);
               // picMessageIntent.setType("image/jpeg");
               // Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                //smsIntent.setType("vnd.android-dir/mms-sms");
                picMessageIntent.setType("image/jpeg");
                picMessageIntent.putExtra("address", MydApplication.getInstance().getPrefManger().getNumber());
                picMessageIntent.putExtra("sms_body", body);
                picMessageIntent.putExtra(Intent.EXTRA_STREAM, getOutputMediaFileUri(filePath));
                if(dialog_start != null && dialog_start.isShowing()){
                    dialog_start.dismiss();
                }
                startActivity(picMessageIntent);

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                String error_message = data.getStringExtra(OpenCameraToTakePic.KEY_ERROR);

                if (error_message.equalsIgnoreCase(OpenCameraToTakePic.CRUSH)) {
                    AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Error", "Crop functionality does not work on your phone!", false);
                }

            }

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

    public void importHouses(){
        houses.clear();
        houses.addAll(MydApplication.getInstance().getPrefManger().getHouses());
        forceRefreshLocation();
    }

    Dialog dialog_start;
    private void showSendingOptionsDialog(final House selectedHouse) {
        if(dialog_start != null && dialog_start.isShowing()){
            dialog_start.dismiss();
        }
        dialog_start = new Dialog(getActivity(),
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog_start.setCancelable(true);
        dialog_start.setContentView(R.layout.dialog_sending_options);

        /*From which button this activity is come from is available on call_from variable
        * for example :
        *
        * if (call_from.equals(MainActivity.TAG_QUICK_SEND){
        *    //Its from quick send button
        * }else if(call_from.equals(MainActivity.TAG_SHARE_CARD){
        *    //Its from share card button
        * }else if(call_from.equals(MainActivity.TAG_MY_CARD_LISTS){
        *    //Its from My card button
        * }*/
        TextView tv_house_number = dialog_start.findViewById(R.id.tv_house_number);
        tv_house_number.setText("House no: " + selectedHouse.getHouse());
        TextView tv_street_number = dialog_start.findViewById(R.id.tv_street_number);
        tv_street_number.setText(selectedHouse.getStreetName());

        Button btn_no_bag = (Button) dialog_start.findViewById(R.id.btn_no_bag);
        Button btn_tag_contaminated = (Button) dialog_start.findViewById(R.id.btn_tag_contaminated);
        Button btn_tag_overweight = (Button) dialog_start.findViewById(R.id.btn_tag_overweight);
        Button btn_attach_pic = (Button) dialog_start.findViewById(R.id.btn_attach_pic);
        ImageView img_close_dialog = (ImageView) dialog_start.findViewById(R.id.img_close_dialog);
        img_close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_start.dismiss();
            }
        });

        btn_no_bag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;

                sendSms(selectedHouse.getAddressText(), btn.getText().toString(), selectedHouse.getLatitude(), selectedHouse.getLongitude());

            }
        });

        btn_tag_contaminated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;

                sendSms(selectedHouse.getAddressText(), btn.getText().toString(), selectedHouse.getLatitude(), selectedHouse.getLongitude());
            }
        });

        btn_tag_overweight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button) v;

                sendSms(selectedHouse.getAddressText(), btn.getText().toString(), selectedHouse.getLatitude(), selectedHouse.getLongitude());

            }
        });

        btn_attach_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!RunnTimePermissions.requestForAllRuntimePermissions(getActivity())) return;
                Button btn = (Button) view;
                selectedTag = btn.getText().toString();
                Intent intent = new Intent(getActivity(), OpenCameraToTakePic.class);
                startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST);
            }
        });


        dialog_start.show();

    }



    private void sendSms(String address, String tag, double lat, double lang) {
        String mapLink = GlobalAppAccess.URL_GOOGLE_MAP + "(" + String.valueOf(GpsServiceUpdate.user_lat) + "," + String.valueOf(GpsServiceUpdate.user_lang) + ")";


        String driverName = MydApplication.getInstance().getPrefManger().getName();
        String phoneNo = MydApplication.getInstance().getPrefManger().getNumber();

        if (phoneNo.isEmpty()) {
            Toast.makeText(getActivity(), "Please first set the phone number from the settings.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (driverName.isEmpty()) {
            Toast.makeText(getActivity(), "Please first set your name from the settings.",
                    Toast.LENGTH_LONG).show();
            return;
        }


        String body = address + ";\n\n" + tag + ";\n\n" + "Driver name: " + driverName + ";\n" + "Driver location: " + mapLink;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, body, null, null);
            if(dialog_start != null && dialog_start.isShowing()){
                dialog_start.dismiss();
            }
            AlertDialogForAnything.showNotifyDialog(getActivity(), AlertDialogForAnything.ALERT_TYPE_SUCCESS);
        } catch (Exception ex) {
            AlertDialogForAnything.showNotifyDialog(getActivity(), AlertDialogForAnything.ALERT_TYPE_ERROR);
            Toast.makeText(getActivity(), ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    private ProgressDialog progressDialog;

    public void showProgressDialog(String message, boolean isIntermidiate, boolean isCancelable) {
       /**/
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        if (progressDialog.isShowing()) {
            return;
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

    public Uri getOutputMediaFileUri(String path) {
        File file = new File(path);
        //return Uri.fromFile(AccessDirectory.getOutputMediaFile());

        return FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", file);

        //return Uri.fromFile( AccessDirectory.getOutputMediaFile());
    }

}
