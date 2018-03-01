package com.creative.housefinder.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.creative.housefinder.R;
import com.creative.housefinder.Utility.CommonMethods;
import com.creative.housefinder.model.House;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.MyViewHolder> {

    public static final String KEY_EVENT = "key_event";
    private List<House> Displayedplaces;
    private List<House> Originalplaces;
    private LayoutInflater inflater;
    @SuppressWarnings("unused")
    private Activity activity;
    private String call_from;

    private PopupWindow popupwindow_obj;

    private String alarm_fire_time;

    private int lastPosition = -1;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_house_number;
        TextView tv_unit;
        TextView tv_street_number;
        TextView tv_city;
        TextView tv_distance;

        public MyViewHolder(View view) {
            super(view);
            tv_house_number = (TextView) view.findViewById(R.id.tv_house_number);
            tv_street_number = (TextView) view.findViewById(R.id.tv_street_number);
            tv_city = (TextView) view.findViewById(R.id.tv_city);
            tv_distance = (TextView) view.findViewById(R.id.tv_distance);
            tv_unit = (TextView) view.findViewById(R.id.tv_unit);
        }
    }


    public HouseAdapter(Activity activity, List<House> attendees) {
        this.activity = activity;
        this.Displayedplaces = attendees;
        this.Originalplaces = attendees;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_house_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final House event = Displayedplaces.get(position);
        holder.tv_house_number.setText("House: #"+event.getHouse());
        holder.tv_street_number.setText("Street: "+event.getStreetName());
        holder.tv_city.setText("City: "+event.getCity());
        holder.tv_distance.setText("Distance: "+ "0.00 KM");

        if(event.getUnit() != null && event.getUnit().length() > 3){
            holder.tv_unit.setVisibility(View.VISIBLE);
            holder.tv_unit.setText(event.getUnit());
        }else{
            holder.tv_unit.setVisibility(View.GONE);
        }

        if(event.getDistance() != 0){
            double distance = event.getDistance() / 1000;
            holder.tv_distance.setText("Distance: "+  CommonMethods.roundFloatToTwoDigitAfterDecimal(distance) +" KM");

        }
    }

    @Override
    public int getItemCount() {
        return Displayedplaces.size();
    }



    private ProgressDialog progressDialog;
    public void showProgressDialog(String message, boolean isIntermidiate, boolean isCancelable) {
       /**/
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
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