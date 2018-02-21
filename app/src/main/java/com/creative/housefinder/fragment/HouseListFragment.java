package com.creative.housefinder.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
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
import com.creative.housefinder.appdata.MydApplication;
import com.creative.housefinder.model.House;
import com.creative.housefinder.model.Houses;

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

public class HouseListFragment extends Fragment implements View.OnClickListener {

    private static final String TAG_REQUEST_HOME_PAGE = "tag_volley_request_in_home_page";

    private List<House> houses;

    private LastLocationOnly lastLocationOnly;



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_list, container,
                false);


        init(view);


        return view;

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

       // CommonMethods.copyRawFileToExternalMemory2(getActivity());
        if(((MainActivity)getActivity()).getHouses() == null){
            String json= "{ \"houses\": "+ CommonMethods.read_file(getActivity()) + "}";
            houses = MydApplication.gson.fromJson(json, Houses.class).getHouses();
            ((MainActivity)getActivity()).setHouses(houses);
            Log.e("DEBUG", String.valueOf(houses.size()));

        }else{
            houses = ((MainActivity)getActivity()).getHouses();
        }

        lastLocationOnly = new LastLocationOnly(getActivity());

        if(lastLocationOnly.canGetLocation()){

        }else{
            GpsEnableTool gpsEnableTool = new GpsEnableTool(getActivity());
            gpsEnableTool.enableGPs();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void init(View view) {





    }


    @Override
    public void onClick(View view) {

        int id = view.getId();



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("DEBUG","its called");
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
